package com.nowcoder.community2.component;

import com.alibaba.fastjson.JSON;
import com.nowcoder.community2.entity.DiscussPost;
import com.nowcoder.community2.entity.NoticeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void send(String topic,int fromUserId,int toUserId){
        send(topic,0, fromUserId, toUserId);
    }

    public void send(String topic,int entityId,int fromUserId,int toUserId){

        NoticeEvent noticeEvent = new NoticeEvent();
        noticeEvent.setTopic(topic)
                .setFromUserId(fromUserId)
                .setToUserId(toUserId)
                .setEntityId(entityId);
        kafkaTemplate.send(topic, JSON.toJSONString(noticeEvent));

    }

    public void send(String topic, DiscussPost post){
        kafkaTemplate.send(topic, JSON.toJSONString(post));
    }

}
