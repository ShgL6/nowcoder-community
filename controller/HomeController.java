package com.nowcoder.community2.controller;

import com.nowcoder.community2.entity.DiscussPost;
import com.nowcoder.community2.entity.Page;
import com.nowcoder.community2.entity.User;
import com.nowcoder.community2.service.DiscussPostService;
import com.nowcoder.community2.service.LikeService;
import com.nowcoder.community2.service.MessageService;
import com.nowcoder.community2.service.UserService;
import com.nowcoder.community2.utils.HostHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;



    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page) {


        // 方法调用前,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                map.put("likeCount",likeService.getEntityLikeCount(1,post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        User user = hostHolder.get();
        int userId = 0;
        if(user != null){
            userId = hostHolder.get().getId();
        }
        int letterCount = messageService.findLetterCount(userId,null, 0);
        model.addAttribute("letterCount",letterCount);

        return "index";
    }

    @GetMapping("/error/500")
    public String error500(){
        return "/error/500";
    }


}
