package com.nowcoder.community2.utils;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {

    public static String getValue(HttpServletRequest request,String name){
        Cookie[] cookies = request.getCookies();
        if(cookies != null && cookies.length != 0){
            for(Cookie cookie : cookies){
                if(StringUtils.equals(cookie.getName(),name)){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
