package com.nowcoder.community2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community2.dao.*;
import com.nowcoder.community2.entity.Comment;
import com.nowcoder.community2.entity.DiscussPost;
import com.nowcoder.community2.entity.LoginTicket;
import com.nowcoder.community2.utils.CommonUtils;
import com.nowcoder.community2.utils.SensitiveFilter;
import javafx.beans.binding.ObjectExpression;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

@SpringBootTest
class Community2ApplicationTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private DataSourceTransactionManager manager;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LoginTicketMapper ticketMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SensitiveFilter filter;
    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;



    @Value("${spring.mail.username}")
    private String from;

    private String to = "3100750364@qq.com";

    @Test
    void contextLoads() throws SQLException {

//        DataSource dataSource = manager.getDataSource();
//        Connection connection = dataSource.getConnection();
//        Statement statement = connection.createStatement();
//        boolean execute = statement.execute("select count(*) from user");
//
//        System.out.println("====================");
//        System.out.println(execute);
//        System.out.println("====================");
        LoginTicket loginTicket = ticketMapper.selectByTicket("123");
        System.out.println(loginTicket);

    }

    @Test
    void t1(){
        Context context = new Context();
        context.setVariable("username","Biggy");
        String process = templateEngine.process("/mail/activation", context);
        System.out.println(process);
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
            helper.setFrom(from);
            helper.setSubject("welcome mxfx");
            helper.setText(process ,true);
            helper.setTo(to);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    @Test
    void test1(){
//        System.out.println(userMapper.selectByEmail("nowcoder1@sina.com"));
//        System.out.println(CommonUtils.md5("11111111" + "40b4f"));


//        for(char c : s.toCharArray()){
//            System.out.println(c +",");
//        }
//        System.out.println(filter.replaceSensitiveWords(s));

//        List<String> strings = messageMapper.selectConversationIds(111, 0, 4);
//        for(String s: strings){
//            System.out.println(s);
//        }

/**
       redisTemplate.setKeySerializer(RedisSerializer.string());
       redisTemplate.setValueSerializer(RedisSerializer.json());
       redisTemplate.opsForValue().set("22",new U("hh","hh"));
       U u = (U)redisTemplate.opsForValue().get("22");
       System.out.println(u.getId());
**/

        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(RedisSerializer.java());
        redisTemplate.opsForValue().set("1",String.valueOf(1));

        Integer i = Integer.valueOf((String) redisTemplate.opsForValue().get("1")) ;
    }


}


class U{
    String name;
    String id;

    public U() {

    }

    public U(String hh, String hh1) {
        this.name = hh;
        this.id = hh1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "U{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}