package com.nowcoder.community2.service;

import com.nowcoder.community2.component.EventProducer;
import com.nowcoder.community2.dao.CommentMapper;
import com.nowcoder.community2.dao.DiscussPostMapper;
import com.nowcoder.community2.entity.Comment;
import com.nowcoder.community2.entity.DiscussPost;
import com.nowcoder.community2.utils.Const;
import com.nowcoder.community2.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Date;
import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter filter;


    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public DiscussPost saveDiscussPost(int userId,String title,String content){
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(userId);
        discussPost.setCreateTime(new Date());
        discussPost.setCommentCount(0);
        discussPost.setStatus(0);
        discussPost.setType(0);
        discussPost.setScore(0);

        // 转义HTML标记 过滤敏感词
        discussPost.setTitle(filter.replaceSensitiveWords(HtmlUtils.htmlEscape(title)));
        discussPost.setContent(filter.replaceSensitiveWords(HtmlUtils.htmlEscape(content)));
        discussPostMapper.insertDiscussPost(discussPost);

        return discussPost;
    }


    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }


    public void changeType(int postId, int type) {
        discussPostMapper.updateType(postId,type);
    }

    public void changeStatus(int postId, int status) {
        discussPostMapper.updateStatus(postId,status);
    }
}
