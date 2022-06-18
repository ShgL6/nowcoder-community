package com.nowcoder.community2.utils;

/**
 * 生成 key 的工具类
 */
public class RedisKeyUtil {

    // 分隔符号
    private static final String SPILT = ":";
    // 前缀(赞)
    private static final String PREFIX_ENTITY_LIKE = "like:entity"; // 某个实体所获得的赞的集合 以点赞用户id为元素
    private static final String PREFIX_USER_LIKE = "like:user";  //某个用户所获得的赞的总数


    //某个实体的赞
    // like:entity:entityType:entityId
    // entityType：帖子为 1 ，评论为 2
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE + SPILT + entityType + SPILT + entityId;
    }

    //某个用户的获赞数量
    public static  String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPILT + userId;
    }

}
