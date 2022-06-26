package com.nowcoder.community2.controller;

import com.nowcoder.community2.annotation.LoginRequired;
import com.nowcoder.community2.component.EventProducer;
import com.nowcoder.community2.entity.Comment;
import com.nowcoder.community2.service.CommentService;
import com.nowcoder.community2.utils.CommonUtils;
import com.nowcoder.community2.utils.Const;
import com.nowcoder.community2.utils.HostHolder;
import com.nowcoder.community2.utils.SensitiveFilter;
import com.sun.deploy.net.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDate;
import java.util.Date;

@Controller
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Autowired
    private EventProducer eventProducer;

    /**
     * 评论-须先登录
     * @param postId
     * @param comment
     * @return
     */
    @LoginRequired
    @PostMapping("/comment/{postId}")
    public String comment(@PathVariable("postId") int postId, @RequestParam(value = "targetUserId", defaultValue = "-1") int targetUserId, Comment comment){
        // Comment 属性中的 entityId entityType targetId content 由前端传入

        // 过滤敏感词
        comment.setContent(
                sensitiveFilter.replaceSensitiveWords(HtmlUtils.htmlEscape(comment.getContent()))
        );
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        comment.setUserId(hostHolder.get().getId());

        // 同时更新 discuss_post 中的 comment_count,加事务处理
        commentService.saveComment(comment,postId);

        if(comment.getEntityType() == Const.COM_POST){
            eventProducer.send(
                    Const.TOPIC_COMMENT,
                    postId,
                    comment.getUserId(),
                    targetUserId
            );
        }


        return "redirect:/discussPost/detail/" + postId;
    }

}
