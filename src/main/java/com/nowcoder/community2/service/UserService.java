package com.nowcoder.community2.service;

import com.nowcoder.community2.dao.UserMapper;
import com.nowcoder.community2.entity.User;
import com.nowcoder.community2.utils.CommonUtils;
import com.nowcoder.community2.utils.Notice;
import com.nowcoder.community2.utils.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.*;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private HostHolder hostHolder;

    @Value("${domain}")
    String domain;
    @Value("${spring.mail.username}")
    String from;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public User findUserByEmail(String email){
        return userMapper.selectByEmail(email);
    }

    public User findUserByName(String userName) {
        return userMapper.selectByName(userName);
    }

    public void saveUser(User user){
        userMapper.insertUser(user);
    };


    public Map<String,Object> register(User user){

        HashMap<String, Object> map = new HashMap<>();

        //1. 参数校验
        if(user == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg", Notice.USERNAME_EMPTY.getInfo());
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg", Notice.PASSWORD_EMPTY.getInfo());
            return map;
        }

        // 2.1 验证用户是否已存在
        User userByName = userMapper.selectByName(user.getUsername());
        if(userByName != null){
            map.put("userMsg", Notice.USER_ALREADY_EXISTS.getInfo());
            return map;
        }
        // 2.2 验证邮箱是否被注册
        User userByEmail = userMapper.selectByEmail(user.getEmail());
        if(userByEmail != null){
            map.put("emailMsg", Notice.USER_ALREADY_EXISTS.getInfo());
            return map;
        }

        // 3.注册
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setEmail(user.getEmail());
        newUser.setCreateTime(new Date());
        // 3.1 生成随机头像 http://images.nowcoder.com/head/160t.png
        String headerUrl = String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(100)) ;
        newUser.setHeaderUrl(headerUrl);
        // 3.2 生成激活码
        String activationCode = CommonUtils.getUUID();
        newUser.setActivationCode(activationCode);
        // 3.3 生成 salt -> 密码加密, 生成密码加密值
        String salt = CommonUtils.getSalt();
        newUser.setSalt(salt);
        newUser.setPassword(CommonUtils.md5(user.getPassword() + salt));
        newUser.setStatus(0);
//        newUser.setType();
        userMapper.insertUser(newUser);

        // 4. 生成激活链接并发送邮件
        String activationUrl = domain + "register/" + String.valueOf(newUser.getId()) + "/" +activationCode;
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setFrom(from);
            helper.setSubject(Notice.WELCOME.getInfo());
            helper.setTo(user.getEmail());

            Context context = new Context();
            context.setVariable("username",user.getUsername());
            context.setVariable("activateUrl",activationUrl);
            String process = templateEngine.process("/mail/activation", context);

            helper.setText(process,true);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {

            e.printStackTrace();
            log.error("邮件发送失败 。。。");
        }

        return map;
    }


    public Notice activate(String id, String activationCode){
        User userById = userMapper.selectById(Integer.valueOf(id));
        if(userById == null || !userById.getActivationCode().equals(activationCode)){
            return Notice.ACTIVATE_FAIL;
        }
        if(userById.getStatus() == 1){
            return Notice.ACTIVATE_ALREADY;
        }

        userMapper.updateStatus(userById.getId(),1);

        return Notice.ACTIVATE_SUCCESS;
    }



    public Map<String,Object> login(
            String username,
            String password,
            String verifyCode,
            String code
            ){

        HashMap<String, Object> map = new HashMap<>();

        // 1.为提高效率 先校验验证码,再校验 username 和 password
        if(StringUtils.isBlank(verifyCode) || StringUtils.isBlank(code) || !StringUtils.equalsIgnoreCase(code,verifyCode)){
            map.put("codeMsg", Notice.CODE_ERROR.getInfo());
            return map;
        }
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg", Notice.USERNAME_EMPTY.getInfo());
            return map;
        }
        if(StringUtils.isBlank(username)){
            map.put("passwordMsg", Notice.PASSWORD_EMPTY.getInfo());
            return map;
        }

        // 2.查询用户是否存在，密码是否正确
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg", Notice.USER_NOT_EXIST.getInfo());
            return map;
        }
        String passwordInput = CommonUtils.md5(password + user.getSalt());
        if(! StringUtils.equals(passwordInput,user.getPassword()) ){
            map.put("passwordMsg", Notice.PASSWORD_ERROR.getInfo());
            return map;
        }

        // 3. 登录成功,生成凭证
        String ticket = CommonUtils.getUUID();
        map.put("ticket",ticket);
        map.put("user",user);

        return map;


    }


    public int modifyUserHeader(int id,String headerUrl){
        return userMapper.updateHeader(id, headerUrl);
    }


    public Map<String,Object> changePassword(String oldPassword,String newPassword,String confirmPassword){
        Map<String,Object> map = new HashMap<>();
        // 检验新密码
        if(StringUtils.isBlank(newPassword)){
            map.put("newMsg", Notice.PASSWORD_EMPTY.getInfo());
            return map;
        }
        if(newPassword.length() < 8){
            map.put("newMsg", Notice.PASSWORD_INVALID.getInfo());
            return map;
        }
        // 检验两次输入的密码是否一致
        if(! StringUtils.equals(newPassword,confirmPassword)){
            map.put("confMsg", Notice.PASSWORD_NOT_CONSISTENT.getInfo());
            return map;
        }

        User user = hostHolder.get();
        String password = CommonUtils.md5(oldPassword + user.getSalt());
        if(! StringUtils.equals(user.getPassword(),password)){
            map.put("oldMsg", Notice.PASSWORD_ERROR.getInfo());
            return map;
        }

        userMapper.updatePassword(user.getId(),password);
        return map;
    }


    public List<User> findUserByIds(List<Integer> userIds) {
        return userMapper.selectByIds(userIds);
    }
}
