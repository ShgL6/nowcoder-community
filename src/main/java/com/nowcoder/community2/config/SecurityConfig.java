package com.nowcoder.community2.config;

import com.nowcoder.community2.utils.CommonUtils;
import com.nowcoder.community2.utils.Const;
import com.nowcoder.community2.utils.Notice;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 作权限控制
        http.authorizeRequests().antMatchers(
                "/comment/**",
                "/discussPost/post",
                "/like",
                "/message/**",
                "/user/setting",
                "/user/header/upload",
                "/user/password",
                "/user/follow").hasAnyAuthority(
                    Const.AUTHORITY_USER,
                    Const.AUTHORITY_ADMIN,
                    Const.AUTHORITY_MODERATOR)
                .antMatchers("/discussPost/top",
                        "/discussPost/wonder")
                .hasAnyAuthority(Const.AUTHORITY_MODERATOR)
                .antMatchers("/discussPost/delete")
                .hasAnyAuthority(Const.AUTHORITY_ADMIN)
                .anyRequest().permitAll()
                .and().csrf().disable();

        // 权限不够时的处理
        http.exceptionHandling().authenticationEntryPoint(
                // 未登录时的处理
                new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        String header = request.getHeader("x-requested-with");
                        // 异步请求
                        if("XMLHttpRequest".equals(header)){
                            String jsonString = CommonUtils.getJSONString(403, Notice.USER_NOT_LOGIN.getInfo());
                            response.setContentType("application/plain;charset=utf-8");
                            response.getWriter().write(jsonString);
                        }else{
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                }
        ).accessDeniedHandler(
                // 权限不足
                new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        String header = request.getHeader("x-requested-with");
                        // 异步请求
                        if("XMLHttpRequest".equals(header)){
                            String jsonString = CommonUtils.getJSONString(403, Notice.NOT_HAVE_AUTHORITY.getInfo());
                            response.setContentType("application/plain;charset=utf-8");
                            response.getWriter().write(jsonString);
                        }else{
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                }
        );


        // 覆盖 Security 的 logout 方法，执行已有的 logout
        // 善意欺骗
        http.logout().logoutUrl("/securitylogout");





    }
}
