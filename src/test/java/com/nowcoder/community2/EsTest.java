package com.nowcoder.community2;

import com.nowcoder.community2.dao.DiscussPostMapper;
import com.nowcoder.community2.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community2.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class EsTest {

    @Autowired
    private DiscussPostMapper postMapper;

    @Autowired
    private DiscussPostRepository repository;
    @Autowired
    private ElasticsearchRestTemplate template;

    @Test
    void test(){


            repository.saveAll(postMapper.selectDiscussPosts(103,0,15));



    }

    @Test
    void tr(){

        String s = "互联网";
        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.multiMatchQuery(s, "content", "title"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSorts(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSorts(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(1, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        SearchHits<DiscussPost> search = template.search(query, DiscussPost.class);
        for(SearchHit hit : search){

            DiscussPost post = (DiscussPost) hit.getContent();
            post.setContent((String) hit.getHighlightField("content").get(0));
            post.setTitle((String) hit.getHighlightField("title").get(0));
            System.out.println(post);
        }
//        NativeSearchQuery countQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.multiMatchQuery("主义", "content", "title")).build();
//        long count =template.count(countQuery, DiscussPost.class);
//
//        System.out.println(count);

    }


    @Test
    void tu(){
        Document document = Document.create();
        document.put("type",0);
        UpdateQuery query = UpdateQuery.builder("286").withDocument(document).build();
        template.update(query, IndexCoordinates.of("discusspost"));

        NativeSearchQuery q = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchQuery("id", "286")).build();
        SearchHits<DiscussPost> hits = template.search(q, DiscussPost.class);
        for(SearchHit<DiscussPost> hit : hits){
            System.out.println("====================");
            System.out.println(hit.getContent().getType());
            System.out.println("====================");
        }
    }

}
