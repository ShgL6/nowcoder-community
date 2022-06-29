package com.nowcoder.community2.utils;

/**
 * 常量
 */
public class Const {

    /**
     * 系统 id
     */
    public final static int SYSTEM_USER_ID = 0;


    /**
     * 消息队列 topic
     */
    public final static String TOPIC_LIKE = "like";
    public final static String TOPIC_COMMENT = "comment";
    public final static String TOPIC_FOLLOW = "follow";
    public final static String TOPIC_POST = "post";

    /**
     * Message Status
     */
    public final static int MESSAGE_UNLIMITED = -1;
    public final static int MESSAGE_UNCHECKED = 0;
    public final static int MESSAGE_CHECKED = 1;
    public final static int MESSAGE_DELETED = 2;

    /**
     * 赞的类型
     */
    public final static int LIKE_POST = 1;
    public final static int LIKE_COMMENT = 2;

    /**
     * like status
     */
    public final static int UNLIKE_STATUS = 0;
    public final static int LIKE_STATUS = 1;

    /**
     * 评论的类型
     */
    public final static int COM_POST = 1;
    public final static int COM_COMMENT = 2;


    /**
     * follow status
     */
    public final static int UNFOLLOW_STATUS = 0;
    public final static int FOLLOW_STATUS = 1;

    /**
     * follow entityId
     */
    public final static int FOLLOW_ENTITY_ID = 0;

}
