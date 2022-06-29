package com.nowcoder.community2.component;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community2.entity.DiscussPost;
import com.nowcoder.community2.entity.NoticeEvent;
import com.nowcoder.community2.entity.Message;
import com.nowcoder.community2.service.ElasticsearchService;
import com.nowcoder.community2.service.MessageService;
import com.nowcoder.community2.utils.Const;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class EventConsumer {

    @Autowired
    private MessageService messageService;
    @Autowired
    private ElasticsearchService elasticsearchService;

    @KafkaListener(topics = {Const.TOPIC_LIKE,Const.TOPIC_COMMENT,Const.TOPIC_FOLLOW})
    public void handleEvent(ConsumerRecord record){
        NoticeEvent noticeEvent = JSONObject.parseObject(record.value().toString(), NoticeEvent.class);
        Message message = new Message();
        message.setFromId(Const.SYSTEM_USER_ID); //系统通知
        message.setToId(noticeEvent.getToUserId());
        message.setConversationId(noticeEvent.getTopic());
        message.setContent(record.value().toString()); // content 保存 event 所有信息
        message.setStatus(0);
        message.setCreateTime(new Date());

        messageService.saveMessage(message);

    }

    @KafkaListener(topics = {Const.TOPIC_POST})
    public void handlePostEvent(ConsumerRecord record){
        elasticsearchService
                .saveDiscussPost(JSONObject.parseObject(record.value().toString(), DiscussPost.class));
        System.out.println("=============");
    }

}
