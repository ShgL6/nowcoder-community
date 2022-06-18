package com.nowcoder.community2.dao;

import com.nowcoder.community2.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface MessageMapper {


    // 关于用户的会话数
    int selectConversationCount(int userId);

    // 用户的所有会话（或参与的会话）的conversation_id
    List<String> selectConversationIds(int userId,int offset,int limit);

    // 用户某会话的未读消息数 或 用户所有未读消息数 （动态sql）
    int selectLetterCount(int userId,String conversationId,int status);

    // 会话详情消息
    List<Message> selectLetters(String conversationId,int offset,int limit);

    // 更改消息状态 为 已读/删除
    int updateStatus(List<Integer> ids, int status);

    // 插入消息
    int insertMessage(Message message);


}
