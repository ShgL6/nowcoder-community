package com.nowcoder.community2.interceptor;

import com.nowcoder.community2.entity.LoginTicket;
import com.nowcoder.community2.entity.User;
import com.nowcoder.community2.service.LoginTicketService;
import com.nowcoder.community2.service.UserService;
import com.nowcoder.community2.utils.CookieUtil;
import com.nowcoder.community2.utils.HostHolder;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 验证 Ticket
        String ticket = CookieUtil.getValue(request, "ticket");
        if(!StringUtils.isBlank(ticket)){
            LoginTicket loginTicket = ticketService.findByTicket(ticket);
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                User user = userService.findUserById(loginTicket.getUserId());
                // 存入 ThreadLocal
                hostHolder.set(user);
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

    // 最后释放 ThreadLocal 中的 user
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
