package com.maomao.community.util;

import com.maomao.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户的信息，代替session对象
 */
@Component
public class HostHolder {
    // ThreadLocal是以线程为key取值的 完成线程隔离
    private ThreadLocal<User> users = new ThreadLocal<User>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }
}
