package com.nowcoder.community2.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 生成 redis key 的工具类
 */
public class RedisKeyUtil {

    // 分隔符号
    private static final String SPILT = ":";


    // 前缀(赞)
    private static final String PREFIX_ENTITY_LIKE = "like:entity"; // 某个实体所获得的赞的集合 以点赞用户id为元素
    private static final String PREFIX_USER_LIKE = "like:user";  //某个用户所获得的赞的总数

    //关注
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_FOLLOWEE = "followee";


    //某个实体的赞
    // like:entity:entityType:entityId
    // entityType：帖子为 1 ，评论为 2
    //  数据类型 zset
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE + SPILT + entityType + SPILT + entityId;
    }

    //某个用户的获赞数量
    //数据类型 string
    public static  String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPILT + userId;
    }

    //关注
    // userId关注的人 follower:userId   userId的跟随者 followee:userId
    // 数据类型 zset
    public static  String getFollowerKey(int userId){
        return PREFIX_FOLLOWER + SPILT + userId;
    }
    public static  String getFolloweeKey(int userId){
        return PREFIX_FOLLOWEE + SPILT + userId;
    }

}
