package com.nowcoder.community2.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community2.entity.LoginTicket;
import com.nowcoder.community2.entity.User;
import com.nowcoder.community2.service.LoginTicketService;
import com.nowcoder.community2.service.UserService;
import com.nowcoder.community2.utils.CookieUtil;
import com.nowcoder.community2.utils.HostHolder;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class TicketInterceptor implements HandlerInterceptor {

    @Autowired
    private LoginTicketService ticketService;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 验证 Ticket
        String ticketKey = CookieUtil.getValue(request, "ticket");
        if(!StringUtils.isBlank(ticketKey)){
             /**
              * LoginTicket loginTicket = ticketService.findByTicket(ticket);
              * if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
              *** User user = userService.findUserById(loginTicket.getUserId());
              *** // 存入 ThreadLocal
              *** hostHolder.set(user);
              * }
              **/
            String userJSONString = (String) redisTemplate.opsForValue().get(ticketKey);
            if(!StringUtils.isBlank(userJSONString)){
                User user = JSONObject.parseObject(userJSONString, User.class);
                hostHolder.set(user);

                // 授权，存入 SecurityContext，以便 Security 作权限控制
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user,user.getPassword(),userService.getAuthorities(user.getId())
                );
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }

        }

        // 放行
        return true;
    }

    // 在渲染模板之前被调用
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.get();
        if(user != null && modelAndView != null){
            modelAndView.addObject("loginUser",user);
        }
    }

    // 最后释放 ThreadLocal 中的 user,防止内存泄漏
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
