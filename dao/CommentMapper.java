package com.nowcoder.community2.dao;

import com.nowcoder.community2.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface CommentMapper {

    int selectCommentCounts(int entityType,int entityId);

    List<Comment> selectComments(int entityType,int entityId,int offset,int limit);

    int insertComment(Comment comment);
}
