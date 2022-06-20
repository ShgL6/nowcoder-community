package com.nowcoder.community2;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;


@SpringBootTest
public class KafkaTest {


    @Autowired
    KafkaProducer producer;
    @Autowired
    KafkaConsumer consumer;

    @Test
    public void test() throws InterruptedException {
        producer.send("test","hello world");
        producer.send("test","you can do it");
        Thread.sleep(10000);
    }



}

@Component
class KafkaProducer{

    @Autowired
    KafkaTemplate kafkaTemplate;

    public void send(String topic,String content){
        kafkaTemplate.send(topic,content);
        System.out.println("producer: i send message");
    }
}

@Component
class KafkaConsumer{

    @KafkaListener(topics = {"test"})
    public void handleMessage(ConsumerRecord record){

        System.out.println("consumer got: " + record.value());
    }
}