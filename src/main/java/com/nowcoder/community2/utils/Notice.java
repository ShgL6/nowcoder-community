package com.nowcoder.community2.utils;

public enum Notice {

    USERNAME_EMPTY("用户名不能为空！"),
    PASSWORD_EMPTY("密码不能为空 ！"),
    PASSWORD_ERROR("密码错误 ！"),
    PASSWORD_NOT_CONSISTENT("两次输入的密码不一致!"),
    PASSWORD_INVALID("密码长度不能小于8位 !"),
    USER_ALREADY_EXISTS("用户已存在 ！"),
    USER_NOT_EXIST("用户不存在!"),
    USER_NOT_LOGIN("请先登录哦！"),
    EMAIL_ALREADY_REGISTER("邮箱已被注册 ！"),
    WELCOME("Welcome to Nowcoder Community !"),
    TITLE_EMPTY("标题不能为空！"),
    CODE_ERROR("验证码错误 !"),
    CODE_EXPIRED("验证码过期！"),
    ACTIVATE_SUCCESS("账号激活成功 !"),
    ACTIVATE_FAIL("账号激活失败 !"),
    ACTIVATE_ALREADY("账号已经激活，不可重复激活 !"),
    FILE_FORMAT_ERROR("文件格式错误 !"),
    FILE_EMPTY("文件为空 !"),
    PUBLISH_SUCCESS("发布成功！"),
    TO_EMPTY("收件人不能为空！"),
    SEND_SUCCESS("发送成功！"),
    SERVER_INTERN_ERROR("服务器内部错误！"),
    CANT_FOLLOW_SELF("不能关注自己！"),
    TEXT_COMMENT("评论了你的帖子"),
    TEXT_LIKE("点赞了你的帖子"),
    TEXT_FOLLOW("关注了你"),
    LIKE_SUCCESS("点赞成功！"),
    NOT_HAVE_AUTHORITY("权限不足！"),
    POST_NOT_EXIST("帖子不存在！"),
    TOP_SUCCESS("置顶成功！"),
    WONDER_SUCCESS("加精成功！"),
    DELETE_SUCCESS("删除成功！");


    private String info;

    Notice(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

}
