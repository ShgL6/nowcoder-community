package com.nowcoder.community2.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.code.kaptcha.Producer;
import com.nowcoder.community2.annotation.LoginRequired;
import com.nowcoder.community2.entity.LoginTicket;
import com.nowcoder.community2.entity.User;
import com.nowcoder.community2.service.LoginTicketService;
import com.nowcoder.community2.service.UserService;

import com.nowcoder.community2.utils.CommonUtils;
import com.nowcoder.community2.utils.CookieUtil;
import com.nowcoder.community2.utils.Notice;
import com.nowcoder.community2.utils.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Controller
@Slf4j
public class LoginController {

    private static final long DEFAULT_EXPIRE = 24 * 60 * 60 * 1000;
    private static final long REM_EXPIRE = 3 * 24 * 60 * 60 * 1000;
    private static final int TIME_DEF = 8 * 60 * 60;  //时区差
    private static final String CONTEXT_PATH = "/community";


    @Autowired
    private UserService userService;
    @Autowired
    private LoginTicketService ticketService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;


    @GetMapping("/register")
    public String toRegister(){
        return "site/register";
    }

    @GetMapping("/login")
    public String toLogin(){
        return "site/login";
    }

    @LoginRequired
    @GetMapping("/logout")
    public String logout(@CookieValue(value = "ticket",required = false) String ticketKey){

        userService.logout(ticketKey);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    @GetMapping("/login/captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response){
        // 随机生成 验证码 及 图片
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        /** 验证码存入 session
         * HttpSession session = request.getSession();
         * session.setAttribute("kaptcha",text);
        **/

        // 验证码存入 redis，将独特的 key 存入 Cookie 在客户端保存
        String captchaKey = RedisKeyUtil.getCaptchaKey();
        redisTemplate.opsForValue().set(captchaKey,text);
        redisTemplate.expire(captchaKey,60, TimeUnit.SECONDS);

        Cookie cookie = new Cookie("captchaKey",captchaKey);
        cookie.setMaxAge(60);
        cookie.setPath("/community/login");
        response.addCookie(cookie);


        // 返回图片
        response.setContentType("image/png");
        try {
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("响应验证码失败 。。。");
        }

    }

    @PostMapping("/login")
    public String login(
            String username,
            String password,
            String verifyCode,
            boolean rememberMe,
            HttpServletResponse response,
            @CookieValue("captchaKey") String captchaKey,
            Model model
    ){

        /**
        * String code = (String) session.getAttribute("kaptcha");
        **/
        if(StringUtils.isBlank(captchaKey)){
            model.addAttribute("codeMsg",Notice.CODE_EXPIRED.getInfo());
            return "site/login";
        }
        String code = (String) redisTemplate.opsForValue().get(captchaKey);

        Map<String, Object> map = userService.login(username,password,verifyCode,code);

        // 成功登录
        if(map.containsKey("ticket")){
            User user = (User) map.get("user");
            /**
                String ticket = (String)map.get("ticket");
                LoginTicket loginTicket = new LoginTicket();
                loginTicket.setUserId(user.getId());
                loginTicket.setStatus(0);
                loginTicket.setTicket(ticket);
                if(! rememberMe) loginTicket.setExpired(new Date(System.currentTimeMillis() + DEFAULT_EXPIRE));
                else loginTicket.setExpired(new Date(System.currentTimeMillis() + REM_EXPIRE));

                ticketService.saveLoginTicket(loginTicket);
            **/

            String ticket = (String)map.get("ticket");
            String ticketKey = RedisKeyUtil.getTicketKey(ticket);
            redisTemplate.opsForValue().set(ticketKey, JSON.toJSONString(user));

            if(rememberMe){
                redisTemplate.expire(ticketKey,REM_EXPIRE/1000,TimeUnit.SECONDS);
            }else {
                redisTemplate.expire(ticketKey,DEFAULT_EXPIRE/1000,TimeUnit.SECONDS);
            }

            Cookie cookie = new Cookie("ticket",ticketKey);
            cookie.setPath(CONTEXT_PATH);
            cookie.setMaxAge(rememberMe ? (int)(REM_EXPIRE/1000 + TIME_DEF) : (int)(DEFAULT_EXPIRE/1000.0 + TIME_DEF));
            response.addCookie(cookie);
            
            return "redirect:/index";
        }else {
        // 登录失败
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("codeMsg",map.get("codeMsg"));

            return  "site/login";
        }

    }


    @PostMapping("/register")
    public String register(Model model, User user){

        Map<String, Object> map = userService.register(user);
        if(map == null || map.isEmpty()){
            model.addAttribute("msg","注册成功，我们已向你的邮箱发送了激活邮件，请尽快激活！");
            model.addAttribute("target","/index");
            return "site/operate-result";
        }else{

            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("userMsg",map.get("userMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));

            return "site/register";
        }

    }


    @GetMapping("/register/{id}/{activationCode}")
    public String activate(Model model,@PathVariable("id") String id,@PathVariable("activationCode") String activationCode){

        switch (userService.activate(id, activationCode)){
            case ACTIVATE_FAIL: {
                model.addAttribute("msg", Notice.ACTIVATE_FAIL.getInfo());
                model.addAttribute("target","/index");
                break;
            }
            case ACTIVATE_ALREADY: {
                model.addAttribute("msg", Notice.ACTIVATE_ALREADY.getInfo());
                model.addAttribute("target","/index");
                break;
            }
            case ACTIVATE_SUCCESS:{
                model.addAttribute("msg", Notice.ACTIVATE_SUCCESS.getInfo());
                model.addAttribute("target","/login");
                break;
            }
        }

        return "site/operate-result";
    }
}
