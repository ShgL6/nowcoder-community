package com.nowcoder.community2.service;

import com.nowcoder.community2.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community2.entity.DiscussPost;
import com.nowcoder.community2.entity.Page;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository repository;
    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    public List<DiscussPost> findDiscussPostByKeyword(String keyword, Page page){

        List<DiscussPost> results = new ArrayList<>();

        NativeSearchQuery countQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.multiMatchQuery(keyword, "content", "title")).build();
        long count = restTemplate.count(countQuery, DiscussPost.class);
        page.setRows(count);

        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.multiMatchQuery(keyword, "content", "title"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSorts(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSorts(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(page.getCurrent() - 1, page.getLimit()))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        SearchHits<DiscussPost> hits = restTemplate.search(query, DiscussPost.class);


        for(SearchHit hit : hits){
            DiscussPost post = (DiscussPost) hit.getContent();
            if(!hit.getHighlightField("content").isEmpty())
                post.setContent((String) hit.getHighlightField("content").get(0));
            if(!hit.getHighlightField("title").isEmpty())
                post.setTitle((String) hit.getHighlightField("title").get(0));
            results.add(post);
        }

        return results;

    }

    public boolean contains(DiscussPost post){
        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchQuery("id", post.getId())).build();
        SearchHits<DiscussPost> hits = restTemplate.search(query, DiscussPost.class);
        if(hits != null && !hits.isEmpty()){
            return true;
        }
        return false;
    }

    public void saveDiscussPost(DiscussPost post){
        repository.save(post);
    }

    public void changeDiscussPostType(int id, int type) {

        Document document = Document.create();
        document.put("type",type);
        UpdateQuery query = UpdateQuery.builder(String.valueOf(id)).withDocument(document).build();
        restTemplate.update(query, IndexCoordinates.of("discusspost"));

    }

    public void changeDiscussPostStatus(int id, int status) {

        Document document = Document.create();
        document.put("status",status);
        UpdateQuery query = UpdateQuery.builder(String.valueOf(id)).withDocument(document).build();
        restTemplate.update(query, IndexCoordinates.of("discusspost"));

    }

    public void removeDiscussPostById(int id) {
        restTemplate.delete(String.valueOf(id),DiscussPost.class);
    }
}
