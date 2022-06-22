package com.nowcoder.community2.component;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community2.entity.Event;
import com.nowcoder.community2.entity.Message;
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

    @KafkaListener(topics = {Const.TOPIC_LIKE,Const.TOPIC_COMMENT,Const.TOPIC_FOLLOW})
    public void handleEvent(ConsumerRecord record){
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        Message message = new Message();
        message.setFromId(Const.SYSTEM_USER_ID); //系统通知
        message.setToId(event.getToUserId());
        message.setConversationId(event.getTopic());
        message.setContent(record.value().toString()); // content 保存 event 所有信息
        message.setStatus(0);
        message.setCreateTime(new Date());

        messageService.saveMessage(message);

    }

}
