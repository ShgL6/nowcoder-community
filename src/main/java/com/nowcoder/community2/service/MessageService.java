package com.nowcoder.community2.service;

import com.nowcoder.community2.dao.MessageMapper;
import com.nowcoder.community2.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Repository
@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;


    /**
     * 查询该用户的会话数
     * @param userId
     * @return
     */
    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    /**
     * 查询该用户的所有会话 id
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<String> findConversationIds(int userId, int offset, int limit) {
        return messageMapper.selectConversationIds(userId, offset, limit);
    }

    /**
     * 私信中一条会话的消息数（未读/已读）
     * @param userId
     * @param conversationId
     * @param status
     * @return
     */
    public int findLetterCount(int userId, String conversationId, int status) {
        return messageMapper.selectLetterCount(userId, conversationId, status);
    }


    /**
     * 私信中一个会话的消息查看
     * @param conversationId
     * @param offset
     * @param limit
     * @return
     */
    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    /**
     * 通知中一条会话的消息数
     * @param userId
     * @param conversationId
     * @param status
     * @return
     */
    public int findNoticeLetterCount(int userId,String conversationId,int status){
        return messageMapper.selectNoticeLetterCount(userId, conversationId, status);
    }

    /**
     * 通知中一条会话中的消息查看
     * @param userId
     * @param conversationId
     * @param offset
     * @param limit
     * @return
     */
    public List<Message> findNoticeLetters(int userId, String conversationId,int offset,int limit){
        return messageMapper.selectNoticeLetters(userId, conversationId, offset, limit);
    }


    /**
     * 读消息
     * @param ids
     * @return
     */
    public int read(List<Integer> ids) {
        if(ids == null || ids.size() == 0){
            return 0;
        }
        return  messageMapper.updateStatus(ids,1);
    }

    /**
     * 新建消息
     * @param message
     * @return
     */
    public int saveMessage(Message message) {
        return messageMapper.insertMessage(message);
    }
}
