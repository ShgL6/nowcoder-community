package com.nowcoder.community2.controller;

import com.alibaba.fastjson.JSON;
import com.nowcoder.community2.annotation.LoginRequired;
import com.nowcoder.community2.entity.User;
import com.nowcoder.community2.service.LikeService;
import com.nowcoder.community2.utils.CommonUtils;
import com.nowcoder.community2.utils.Const;
import com.nowcoder.community2.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @ResponseBody
    @PostMapping("/like")
    public String like(int entityType,int entityId,int targetId){
        User user = hostHolder.get();
        int userId = user.getId();

        likeService.like(entityType,entityId,userId,targetId);

        long likeCount = likeService.getEntityLikeCount(entityType,entityId);
        int likeStatus = likeService.getEntityLikeStatus(entityType,entityId,userId);

        Map<String,Object> map = new HashMap<>();
        map.put("likeStatus",likeStatus);
        map.put("likeCount",likeCount);
        String msg = likeStatus == 0 ? "已取消点赞！" : "点赞成功！";
        return CommonUtils.getJSONString(0, msg, map);
    }

}
