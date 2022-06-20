package com.nowcoder.community2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community2.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Test {


    public static void main(String[] args) throws InterruptedException {

//        System.out.println("1...");
//        Thread.sleep(30000);
//        byte[] array = new byte[1024*1024];
//        System.out.println("2...");
//        Thread.sleep(30000);
//        array = null;
//        System.gc();
//        System.out.println("3...");

        System.out.println("Hello World");

    }


    @org.junit.jupiter.api.Test
    void  ts(){
//        Map<String,Object> map = new HashMap<>();
//        map.put("likeStatus",1);
//        map.put("likeCount",1);
//        String msg = "点赞成功！";
//        System.out.println(CommonUtils.getJSONString(0, msg, map));

        T1 t = new T1();
        String s = null;
        System.out.println(s = JSON.toJSONString(t));

        JSONObject.parseObject(s,T1.class).m();



    }

}

/**
 * 被测试类
 */
class T1{

    public int i = 0;
    public int j = 1;

    public void m(){
        System.out.println("say something...");
    }

}