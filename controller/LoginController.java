package com.nowcoder.community2.controller;


import com.google.code.kaptcha.Producer;
import com.nowcoder.community2.annotation.LoginRequired;
import com.nowcoder.community2.entity.LoginTicket;
import com.nowcoder.community2.entity.User;
import com.nowcoder.community2.service.LoginTicketService;
import com.nowcoder.community2.service.UserService;

import com.nowcoder.community2.utils.Const;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

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
    public String logout(@CookieValue(value = "ticket",required = false) String ticket){
        ticketService.modifyStatus(ticket,1);
        return "redirect:/login";
    }

    @GetMapping("/login/captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response){
        // 随机生成 验证码 及 图片
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 验证码存入 session
        HttpSession session = request.getSession();
        session.setAttribute("kaptcha",text);
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
            String verifyCode, // session 中的 kaptcha
            boolean rememberMe,
            HttpSession session,
            HttpServletResponse response,
            Model model
    ){

        String code = (String) session.getAttribute("kaptcha");
        Map<String, Object> map = userService.login(username,password,verifyCode,code);

        // 成功登录
        if(map.containsKey("ticket")){
            User user = (User) map.get("user");
            String ticket = (String)map.get("ticket");
            LoginTicket loginTicket = new LoginTicket();
            loginTicket.setUserId(user.getId());
            loginTicket.setStatus(0);
            loginTicket.setTicket(ticket);
            if(! rememberMe) loginTicket.setExpired(new Date(System.currentTimeMillis() + DEFAULT_EXPIRE));
            else loginTicket.setExpired(new Date(System.currentTimeMillis() + REM_EXPIRE));

            ticketService.saveLoginTicket(loginTicket);

            //
            Cookie cookie = new Cookie("ticket",ticket);
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
                model.addAttribute("msg",Const.ACTIVATE_FAIL.getInfo());
                model.addAttribute("target","/index");
                break;
            }
            case ACTIVATE_ALREADY: {
                model.addAttribute("msg",Const.ACTIVATE_ALREADY.getInfo());
                model.addAttribute("target","/index");
                break;
            }
            case ACTIVATE_SUCCESS:{
                model.addAttribute("msg",Const.ACTIVATE_SUCCESS.getInfo());
                model.addAttribute("target","/login");
                break;
            }
        }

        return "site/operate-result";
    }
}
