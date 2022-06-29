package com.nowcoder.community2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community2.dao.*;
import com.nowcoder.community2.entity.*;
import com.nowcoder.community2.utils.CommonUtils;
import com.nowcoder.community2.utils.SensitiveFilter;
import javafx.beans.binding.ObjectExpression;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.DefaultTypedTuple;
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
import javax.swing.text.html.parser.Entity;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

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

    /**
     * 测试opsForSet 遍历顺序——按插入顺序
     */
    @Test
    void tq(){

        String key = "k";
        redisTemplate.opsForHash().put(key,"k1",new Date());
        redisTemplate.opsForHash().put(key,"k2",new Date());
        redisTemplate.opsForHash().put(key,"k3",new Date());
        redisTemplate.opsForHash().put(key,"k4",new Date());
        redisTemplate.opsForHash().put(key,"k5",new Date());


        Map entries = redisTemplate.opsForHash().entries(key);
        Set<Map.Entry> set = entries.entrySet();

        // 降序
        TreeSet<Map.Entry> treeSet = new TreeSet<Map.Entry>((e1,e2)->{
            Date d1 = (Date) e1.getValue();
            Date d2 = (Date) e2.getValue();
            return d1.after(d2) ? -1 : 1;
        });

        treeSet.addAll(set);
        for(Map.Entry e : treeSet){
            System.out.println(e.getKey().toString() + "=====" + e.getValue().toString());
        }
    }


    /**
     * 测试 userMapper
     */
    @Test
    void tr(){

        List<Integer> integers = Arrays.asList(110, 111, 112, 113, 114, 115);
        List<User> users = userMapper.selectByIds(integers);
        for (User user : users) {
            System.out.println(user);
        }
    }


    /**
     * 测试 hashMap
     */
    @Test
    void te(){
        Map<String,Object> map = new HashMap<>();
        map.put("1",new Date());
        map.put("2",1);

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



    @Test
    void ty(){
        /**
            System.out.println("所有未读："+ messageMapper.selectNoticeLetterCount(155,null,0));
            System.out.println("like 未读："+ messageMapper.selectNoticeLetterCount(155,"like",0));
            System.out.println("like 所有："+messageMapper.selectNoticeLetterCount(155,"like",-1));
            System.out.println("comment 未读："+ messageMapper.selectNoticeLetterCount(155,"comment",0));
            System.out.println("comment 所有："+messageMapper.selectNoticeLetterCount(155,"like",-1));
        **/
        List<Message> like = messageMapper.selectNoticeLetters(155, "like", 0, 5);
        for (Message m :
                like) {
            System.out.println(m.getId());
        }
    }


    @Test
    void tw(){
        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle("kkk");
        discussPost.setContent("mmmm");
        discussPost.setCreateTime(new Date());
        discussPost.setCommentCount(0);
        discussPost.setStatus(0);
        discussPost.setType(0);
        discussPost.setScore(0);
        discussPost.setUserId(111);

        discussPostMapper.insertDiscussPost(discussPost);
        System.out.println(discussPost.getId());

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