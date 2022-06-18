package com.nowcoder.community2.interceptor;

import com.nowcoder.community2.annotation.LoginRequired;
import com.nowcoder.community2.entity.User;
import com.nowcoder.community2.utils.CommonUtils;
import com.nowcoder.community2.utils.Const;
import com.nowcoder.community2.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Value("${community.context}")
    private String context;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){
            Method method = ((HandlerMethod) handler).getMethod();
            LoginRequired annotation = method.getAnnotation(LoginRequired.class);
            // 带有 @LoginRequired 注解
            if(annotation != null){
                User user = hostHolder.get();
                // 未登录
                if(user == null){
                    String header = request.getHeader("X-Requested-With");

                    if("XMLHttpRequest".equals(header)){
                        // 异步 ajax
                        String jsonString = CommonUtils.getJSONString(-1, Const.USER_NOT_LOGIN.getInfo());
                        response.setContentType("application/plain;charset=utf-8");
                        response.getWriter().write(jsonString);
                    }else{
                        // 普通请求
                        response.sendRedirect(context + "/login");
                    }

                    return false;
                }
            }

        }
        return true;
    }
}
