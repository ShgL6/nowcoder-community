package com.nowcoder.community2.entity;

/**
 * 系统通知消息
 * 事件驱动
 */
public class Event {

    /**
     * 该事件属于何种类型/主题
     * "like"——点赞 "comment"——帖子评论 "follow"——关注
     */
    private String topic;

    /**
     * 事件对象的 id
     */
    private int entityId;

    /**
     * 事件发起者 id
     */
    private int fromUserId;

    /**
     * 事件接收者 id
     */
    private int toUserId;

    public Event() {
    }

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getFromUserId() {
        return fromUserId;
    }

    public Event setFromUserId(int fromUserId) {
        this.fromUserId = fromUserId;
        return this;
    }

    public int getToUserId() {
        return toUserId;
    }

    public Event setToUserId(int toUserId) {
        this.toUserId = toUserId;
        return this;
    }
}
