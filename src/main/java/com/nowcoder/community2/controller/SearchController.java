package com.nowcoder.community2.controller;

import com.nowcoder.community2.entity.DiscussPost;
import com.nowcoder.community2.entity.Page;
import com.nowcoder.community2.service.*;
import com.nowcoder.community2.utils.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;

    /**
     *  get 方法用于获取 page 参数 current
     *  post 方法用于获取 keyword
     * @param keyword
     * @param page
     * @param model
     * @return
     */
    @PostMapping
    @GetMapping
    public String getPosts(String keyword, Page page, Model model){

        page.setPath("/search");

        List<DiscussPost> posts = elasticsearchService.findDiscussPostByKeyword(keyword, page);
        List<Map<String,Object>> postVos = new ArrayList<>();
        for (DiscussPost post : posts) {
            Map<String,Object> map = new HashMap<>();
            map.put("user",userService.findUserById(post.getUserId()));
            map.put("likeCount",likeService.getEntityLikeCount(Const.LIKE_POST,post.getId()));
            map.put("post",post);

            postVos.add(map);
        }

        model.addAttribute("posts",postVos);

        return "/site/search";

    }
}
