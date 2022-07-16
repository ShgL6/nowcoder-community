package com.nowcoder.community2.dao;


import com.nowcoder.community2.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // @Param注解用于给参数取别名,
    // 如果只有一个参数,并且在<if>里使用,则必须加别名.
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    @Select({"select * from discuss_post where id = #{id} and status != 2"})
    DiscussPost selectDiscussPostById(int id);

    @Update({"update discuss_post set comment_count = #{count} where id = #{id}"})
    int updateCommentCount(int id,int count);

    @Update({"update discuss_post set type = #{type} where id = #{postId}"})
    void updateType(int postId, int type);

    @Update({"update discuss_post set status = #{status} where id = #{postId}"})
    void updateStatus(int postId, int status);
}
