package com.nowcoder.community2.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

public class CommonUtils {

    // 生成随机字符串
    public static String getUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    // MD5 加密
    public static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    // 生成 salt
    public static String getSalt(){
        return getUUID().substring(0,5);
    }




    /**
     * JSON 用于异步请求响应数据
     * @param code 状态码
     * @param msg 消息内容
     * @param map 其他数据（冗余）
     * @return
     */
    public static String getJSONString(int code, String msg, Map<String,Object> map){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code",code);
        jsonObject.put("msg",msg);
        if(map != null){
            for(String key : map.keySet()){
                jsonObject.put(key,map.get(key));
            }
        }

        return jsonObject.toJSONString();

    }

    public static String getJSONString(int code, String msg){
        return getJSONString(code,msg,null);
    }

    public static String getJSONString(int code){
        return getJSONString(code,null);
    }




}
