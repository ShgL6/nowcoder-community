package com.nowcoder.community2.utils;

import com.nowcoder.community2.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，代替session
 * 线程隔离
 */
@Component
public class HostHolder {

    private ThreadLocal<User> local = new ThreadLocal<>();

    public void set(User user){
        local.set(user);
    }

    public User get(){
        return local.get();
    }

    public void clear(){
        local.remove();
    }

}
