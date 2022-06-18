package com.nowcoder.community2.service;

import com.nowcoder.community2.dao.CommentMapper;
import com.nowcoder.community2.dao.DiscussPostMapper;
import com.nowcoder.community2.entity.Comment;
import com.nowcoder.community2.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;


    public int findCommentCounts(int entityType, int entityId){
        return commentMapper.selectCommentCounts(entityType, entityId);
    }

    public List<Comment> findComments(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectComments(entityType, entityId,offset,limit);
    }

    @Transactional
    public int saveComment(Comment comment,int postId) {

        int i = commentMapper.insertComment(comment);
        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(postId);
        if(comment.getEntityType() == 1){
            discussPostMapper.updateCommentCount(discussPost.getId(),discussPost.getCommentCount() + 1);
        }
        return i;
    }
}
