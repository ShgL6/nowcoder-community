package com.nowcoder.community2.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community2.dao.UserMapper;
import com.nowcoder.community2.entity.User;
import com.nowcoder.community2.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${domain}")
    String domain;
    @Value("${spring.mail.username}")
    String from;

    public User findUserById(int id) {
        return findUser(id,null);
    }

    public User findUserByName(String userName) {
        return findUser(-1,userName);
    }



    /**
     * 用于缓存用户的一组方法
     */
     private void initCache(User user){
         String userKey = RedisKeyUtil.getUserKey(user.getId());
         String userByNameKey = RedisKeyUtil.getUserNameKey(user.getUsername());

         redisTemplate.opsForValue().set(userKey, JSON.toJSONString(user));
         redisTemplate.opsForValue().set(userByNameKey,JSON.toJSONString(user));

         redisTemplate.expire(userKey,2, TimeUnit.DAYS);
         redisTemplate.expire(userByNameKey,2, TimeUnit.DAYS);
     }
     private User findUser(int userId, String userName){

         String userKey;
         if(userId < 0 && !StringUtils.isBlank(userName)){
            userKey = RedisKeyUtil.getUserNameKey(userName);
         }else{
             userKey = RedisKeyUtil.getUserKey(userId);
         }

         String userJSONString = (String) redisTemplate.opsForValue().get(userKey);

         if(StringUtils.isBlank(userJSONString)){
             User user = userMapper.selectById(userId);
             initCache(user);
             return user;
         }else{
             User user = JSONObject.parseObject(userJSONString, User.class);
             return user;
         }
     }
     private void flushCache(User user){
         if(user.getId() > 0){
             String userKey = RedisKeyUtil.getUserKey(user.getId());
             redisTemplate.delete(userKey);
         }
         if(!StringUtils.isBlank(user.getUsername())){
             String userByNameKey = RedisKeyUtil.getUserNameKey(user.getUsername());
             redisTemplate.delete(userByNameKey);
         }
     }



    /**
     * 注册
     * @param user
     * @return
     */
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
        initCache(newUser);

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

    /**
     * 激活
     * @param id
     * @param activationCode
     * @return
     */
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


    /**
     * 登录
     * @param username
     * @param password
     * @param verifyCode
     * @param code
     * @return
     */
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
        // User user = userMapper.selectByName(username);
        User user = findUserByName(username);
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


    /**
     * 更新头像
     * @param id
     * @param headerUrl
     * @return
     */
    public int modifyUserHeader(int id,String headerUrl){
        int result = userMapper.updateHeader(id, headerUrl);

        // 刷新缓存
        User user = new User();
        user.setId(id);
        flushCache(user);

        return result;
    }


    /**
     * 更改密码
     * @param oldPassword
     * @param newPassword
     * @param confirmPassword
     * @return
     */
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
        // 刷新缓存
        flushCache(user);

        return map;
    }

    /**
     * 查询用户
     * @param userIds
     * @return
     */
    public List<User> findUserByIds(List<Integer> userIds) {
        List<User> users = userMapper.selectByIds(userIds);

        // 初始化缓存
        for(User user : users){
            initCache(user);
        }

        return users;
    }

    /**
     * 查询用户权限
     * @param userId
     * @return
     */
     public Collection<? extends GrantedAuthority> getAuthorities(int userId){
         User user = this.findUserById(userId);
         List<GrantedAuthority> authorities = new ArrayList<>();
         authorities.add(new GrantedAuthority() {
             @Override
             public String getAuthority() {
                 switch (user.getType()){
                     case 1 :
                         return Const.AUTHORITY_ADMIN;
                     case 2 :
                         return Const.AUTHORITY_MODERATOR;
                     default:
                         return Const.AUTHORITY_USER;
                 }

             }
         });

         return  authorities;
     }

    /**
     * 退出登录
     * @param ticketKey
     */
    public void logout(String ticketKey) {
        redisTemplate.delete(ticketKey);
    }
}
