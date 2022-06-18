package com.nowcoder.community2.aspect;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;


@Aspect
@Component
public class ServiceLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    @Pointcut("execution(* com.nowcoder.community2.service.*.*(..))")
    private void pt(){}

    @Before("pt()")
    public void before(JoinPoint point){
        // 用户主机[ip] 上 在[xxx] 访问了 [方法]

        // 获取用户主机
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        String remoteHost = requestAttributes.getRequest().getRemoteHost();

        //TODO

        // 获取时间
        String time = new SimpleDateFormat("yyyy-MM-ss HH:mm:ss").format(new Date());

        // 获取被调用的方法名
        String declaringTypeName = point.getSignature().getDeclaringTypeName();
        String name = point.getSignature().getName();
        String method = declaringTypeName + name;

        logger.debug(String.format("用户[%s]，在[%s],访问了[%s]",remoteHost, time,method));

    }

}
