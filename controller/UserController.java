package com.nowcoder.community2.controller;

import com.nowcoder.community2.annotation.LoginRequired;
import com.nowcoder.community2.entity.User;
import com.nowcoder.community2.service.LikeService;
import com.nowcoder.community2.service.UserService;
import com.nowcoder.community2.utils.CommonUtils;
import com.nowcoder.community2.utils.Const;
import com.nowcoder.community2.utils.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
import java.util.Map;

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
    private HostHolder hostHolder;

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
    @PostMapping(value = "/header/upload")
    public String uploadHeader(MultipartFile multipartFile, Model model){

        // 是否为空文件
        if(multipartFile == null){
            model.addAttribute("error", Const.FILE_EMPTY.getInfo());
            return "/site/setting";
        }
        // 判断图片格式(后缀名)
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error", Const.FILE_FORMAT_ERROR.getInfo());
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

        model.addAttribute("user",user);
        model.addAttribute("likeCount",likeService.getUserLikeCount(userId));

        return "/site/profile";
    }


}
