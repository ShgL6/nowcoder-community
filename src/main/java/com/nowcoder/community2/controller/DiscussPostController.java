package com.nowcoder.community2.controller;

import com.nowcoder.community2.entity.Comment;
import com.nowcoder.community2.entity.DiscussPost;
import com.nowcoder.community2.entity.Page;
import com.nowcoder.community2.entity.User;
import com.nowcoder.community2.service.CommentService;
import com.nowcoder.community2.service.DiscussPostService;
import com.nowcoder.community2.service.LikeService;
import com.nowcoder.community2.service.UserService;
import com.nowcoder.community2.utils.CommonUtils;
import com.nowcoder.community2.utils.Const;
import com.nowcoder.community2.utils.HostHolder;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/discussPost")
public class DiscussPostController {

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @PostMapping("/post")
    @ResponseBody
    public String post(String title,String content){
        User user = hostHolder.get();
        if(user == null){
            return CommonUtils.getJSONString(403, Const.USER_NOT_LOGIN.getInfo());
        }
        if(StringUtils.isBlank(title)){
            return CommonUtils.getJSONString(100,Const.TITLE_EMPTY.getInfo());
        }

        discussPostService.saveDiscussPost(user.getId(), title, content);

        return CommonUtils.getJSONString(0,Const.PUBLISH_SUCCESS.getInfo());

    }

    @GetMapping("/detail/{id}")
    public String details(@PathVariable("id") int id, Page page, Model model){

        DiscussPost post = discussPostService.findDiscussPostById(id);
        // 分页
        page.setPath("/discussPost/detail/"+id);
        page.setLimit(5);
        page.setRows(post.getCommentCount());

        User user = userService.findUserById(post.getUserId());
        List<Comment> comments = commentService.findComments(1, post.getId(),page.getOffset(),page.getLimit());

        User host = hostHolder.get();

        // 评论
        List<Map<String,Object>> commentVos = new ArrayList<>();
        for(Comment comment : comments){
            Map<String,Object> cvo = new HashMap<>();

            // 回复
            int commentId = comment.getId();
            List<Comment> replies = commentService.findComments(2, commentId,0,Integer.MAX_VALUE);
            List<Map<String,Object>> replyVos = new ArrayList<>();
            for(Comment reply : replies){
                Map<String,Object> rvo = new HashMap<>();
                rvo.put("reply",reply);
                rvo.put("user",userService.findUserById(reply.getUserId()));
                rvo.put("target",userService.findUserById(reply.getTargetId()));
                // 回复的likeCount
                rvo.put("likeCount",likeService.getEntityLikeCount(2,reply.getId()));
                // likeStatus
                if(host != null){
                    rvo.put("likeStatus",likeService.getEntityLikeStatus(2,reply.getId(),host.getId()));
                }else{
                    rvo.put("likeStatus",null);
                }
                replyVos.add(rvo);
            }


            // 评论的likeCount
            cvo.put("likeCount",likeService.getEntityLikeCount(2,comment.getId()));
            // likeStatus
            if(host != null){
                cvo.put("likeStatus",likeService.getEntityLikeStatus(2,comment.getId(),host.getId()));
            }else{
                cvo.put("likeStatus",null);
            }

            cvo.put("replies",replyVos);
            cvo.put("replyCounts",replyVos.size());
            User commentUser = userService.findUserById(comment.getUserId());
            cvo.put("user",commentUser);

            cvo.put("comment",comment);
            commentVos.add(cvo);
        }

        //likeCount
        model.addAttribute("likeCount",likeService.getEntityLikeCount(1,post.getId()));
        //likeStatus
        if(host != null){
            model.addAttribute("likeStatus",likeService.getEntityLikeStatus(1,post.getId(),host.getId()));
        }else{
            model.addAttribute("likeStatus",null);
        }
        model.addAttribute("post",post);
        model.addAttribute("user",user);
        model.addAttribute("commentCounts",post.getCommentCount());
        model.addAttribute("comments",commentVos);


        return "site/discuss-detail";

    }




}
