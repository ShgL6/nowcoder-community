package com.nowcoder.community2.controller;

import com.nowcoder.community2.annotation.LoginRequired;
import com.nowcoder.community2.component.EventProducer;
import com.nowcoder.community2.entity.Page;
import com.nowcoder.community2.entity.User;
import com.nowcoder.community2.service.FollowService;
import com.nowcoder.community2.service.LikeService;
import com.nowcoder.community2.service.UserService;
import com.nowcoder.community2.utils.CommonUtils;
import com.nowcoder.community2.utils.Const;
import com.nowcoder.community2.utils.Notice;
import com.nowcoder.community2.utils.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Value("${domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String filePath;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @LoginRequired
    @GetMapping(value = "/setting")
    public String getSettingPage(){
        return "/site/setting";
    }

    /**
     * 上传头像
     * 由于 MultipartFile 为 SpringMVC 的对象，所以将处理逻辑放在 Controller 层
     * @return
     */
    @LoginRequired
    @PostMapping(value = "/header/upload")
    public String uploadHeader(MultipartFile multipartFile, Model model){

        // 是否为空文件
        if(multipartFile == null){
            model.addAttribute("error", Notice.FILE_EMPTY.getInfo());
            return "/site/setting";
        }
        // 判断图片格式(后缀名)
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error", Notice.FILE_FORMAT_ERROR.getInfo());
            return "/site/setting";
        }

        // 服务器端存储 图片
        String filename = CommonUtils.getUUID() + suffix;
        try {
            multipartFile.transferTo(new File(filePath +filename));
        } catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("文件存储失败，服务器内部错误 ！");
        }
        // 数据库更新 头像url
        String headerUrl = domain + "user/header/"+ filename;
        User user = hostHolder.get();
        userService.modifyUserHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }


    /**
     * 返回用户头像
     * @param headerUrl
     * @param response
     */
    @GetMapping("/header/{headerUrl}")
    public void getHeader(@PathVariable("headerUrl") String headerUrl, HttpServletResponse response) {

        String filename = headerUrl.substring(headerUrl.lastIndexOf("/") + 1);
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);

        try(
                FileInputStream in = new FileInputStream(filePath + filename);
                ServletOutputStream out = response.getOutputStream();
        ) {

            response.setContentType("image/" + suffix);
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = in.read(buffer)) > 0){
                out.write(buffer,0,len);
            }

        }catch (IOException e){
            log.error(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("文件获取失败，服务器内部异常！");
        }
    }


    /**
     * 重置密码
     * @param oldPassword
     * @param newPassword
     * @param confirmPassword
     * @param model
     * @return
     */
    @LoginRequired
    @PostMapping("/password")
    public String changePassword(String oldPassword,String newPassword,String confirmPassword,Model model){

        Map<String, Object> map = userService.changePassword(oldPassword, newPassword, confirmPassword);
        if(!map.isEmpty()){
            model.addAttribute("oldMsg",map.get("oldMsg"));
            model.addAttribute("newMsg",map.get("newMsg"));
            model.addAttribute("confMsg",map.get("confMsg"));
            return "/site/setting";
        }

        return "redirect:/index";

    }


    /**
     * 个人详情页面
     * @param userId
     * @param model
     * @return
     */
    @GetMapping("/profile/{userId}")
    public String getProfile(@PathVariable("userId") int userId,Model model){

        User user = userService.findUserById(userId);

        if(hostHolder.get() != null && hostHolder.get().getId() == userId){
                model.addAttribute("isMe",true);
                model.addAttribute("who","我");
        } else{
            model.addAttribute("isMe",false);
            model.addAttribute("who","TA");
        }

        if(hostHolder.get() != null){
            model.addAttribute("hasFollowed",followService.hasFollowed(hostHolder.get().getId(),userId));
        }else{
            model.addAttribute("hasFollowed",false);
        }

        model.addAttribute("user",user);
        model.addAttribute("likeCount",likeService.getUserLikeCount(userId));
        model.addAttribute("followersNum",followService.followersNum(userId));
        model.addAttribute("followeesNum",followService.followeesNum(userId));




        return "/site/profile";
    }

    /**
     * 关注
     * @param followeeId 被关注者的 id
     * @return
     */
    @PostMapping("/follow")
    @LoginRequired
    @ResponseBody
    public String follow(int followeeId){

        User user = hostHolder.get();
        if(user.getId() == followeeId){
            return CommonUtils.getJSONString(-1,Notice.CANT_FOLLOW_SELF.getInfo());
        }
        int status = followService.follow(user.getId(), followeeId);

        if(status == Const.FOLLOW_STATUS){
            eventProducer.send(
                    Const.TOPIC_FOLLOW,
                    user.getId(),
                    followeeId
            );
        }

        Map<String,Object> map = new HashMap<>();
        map.put("status",status);

        return CommonUtils.getJSONString(0,null,map);
    }

    /**
     * 页面：关注的人
     * @param followerId
     * @param model
     * @param page
     * @return
     */
    @GetMapping("/followees/{followerId}")
    public String followees(@PathVariable("followerId") int followerId, Model model, Page page){

        // 分页
        page.setPath("/user/followers/" + followerId);

        User user = userService.findUserById(followerId);
        model.addAttribute("user",user);

        List<Map<String, Object>> items = getItems(0, followerId, page);
        if(items != null && !items.isEmpty()){
            model.addAttribute("items",items);
        }

        return "/site/followee";
    }

    /**
     * 页面：追随者
     * @param followeeId
     * @param model
     * @param page
     * @return
     */
    @GetMapping("/followers/{followeeId}")
    public String followers(@PathVariable("followeeId") int followeeId, Model model, Page page){

        // 分页
        page.setPath("/user/followers/" + followeeId);

        User user = userService.findUserById(followeeId);
        model.addAttribute("user",user);

        List<Map<String, Object>> items = getItems(1, followeeId, page);
        if(items != null && !items.isEmpty()){
            model.addAttribute("items",items);
        }

        return "/site/follower";
    }

    /**
     * 复用代码 getItems
     * @param flag
     * @param userId
     * @param page
     * @return
     */
    private List<Map<String,Object>> getItems(int flag, int userId, Page page){

        List<Map.Entry> followItems = followService.getItems(flag,userId,page);

        if(followItems != null && followItems.size() > 0){

            List<Integer> userIds = new ArrayList<>();

            for (Map.Entry entry : followItems) {
                userIds.add((Integer) entry.getKey());
            }

            List<User> userByIds = userService.findUserByIds(userIds);
            if(userByIds != null && userByIds.size() > 0){
                List<Map<String,Object>> items = new ArrayList<>();
                for(int i = 0; i < userByIds.size(); i ++){
                    Map<String,Object> map = new HashMap<>();
                    map.put("user",userByIds.get(i));
                    map.put("date",followItems.get(i).getValue());
                    if(hostHolder.get() != null){
                        map.put("btnExists",hostHolder.get().getId() != userByIds.get(i).getId());
                        map.put("hasFollowed",followService.hasFollowed(hostHolder.get().getId(),userByIds.get(i).getId()));
                    }else{
                        map.put("btnExists",true);
                        map.put("hasFollowed",false);
                    }
                    items.add(map);
                }
                return items;
            }
        }

        return null;
    }

}
