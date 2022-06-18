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


    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    public List<String> findConversationIds(int userId, int offset, int limit) {
        return messageMapper.selectConversationIds(userId, offset, limit);
    }

    public int findLetterCount(int userId, String conversationId, int status) {
        return messageMapper.selectLetterCount(userId, conversationId, status);
    }


    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    public int read(List<Integer> ids) {
        if(ids == null || ids.size() == 0){
            return 0;
        }
        return  messageMapper.updateStatus(ids,1);
    }

    public int saveMessage(Message message) {
        return messageMapper.insertMessage(message);
    }
}
