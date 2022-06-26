package com.nowcoder.community2.interceptor;

import com.nowcoder.community2.entity.User;
import com.nowcoder.community2.service.LikeService;
import com.nowcoder.community2.service.MessageService;
import com.nowcoder.community2.utils.Const;
import com.nowcoder.community2.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        User user = hostHolder.get();

        if(user != null && modelAndView != null){

            int allUncheckedLetterCount = messageService.findLetterCount(user.getId(), null, Const.MESSAGE_UNCHECKED);
            modelAndView.addObject("allUncheckedLetterCount",allUncheckedLetterCount);

            int allUncheckedNoticeLetterCount = messageService.findNoticeLetterCount(user.getId(), null, Const.MESSAGE_UNCHECKED);
            modelAndView.addObject("allUncheckedNoticeLetterCount",allUncheckedNoticeLetterCount);

            int allUncheckedMessageCount = allUncheckedLetterCount + allUncheckedNoticeLetterCount;
            modelAndView.addObject("allUncheckedMessageCount",allUncheckedMessageCount);

        }



    }
}
