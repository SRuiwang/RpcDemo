package com.sr.app.service.impl;

import com.sr.app.pojo.User;
import com.sr.app.service.inte.UserService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    static String chars = "qwertyuiopasdfghjklzxcvbnm";

    static Map<String, User> userMapIndexId = new HashMap<String, User>();
    static Map<String, User> userMapIndexIdAndName = new HashMap<String, User>();

    static {
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setId(String.valueOf(i));
            if (i == 0) {
                user.setName("zhangsan");
            } else {
                int nextInt = new Random().nextInt(chars.length() - 6);
                user.setName(chars.substring(nextInt, nextInt + 4));
            }
            user.setCreateData(new Date());
            user.setUpdateData(new Date());
            userMapIndexId.put(user.getId(), user);
            userMapIndexIdAndName.put(getKey(user), user);
        }
    }


    public User getUserById(String id) throws Throwable {
        return userMapIndexId.get(Integer.valueOf(id));
    }

    public User getUserByIdAndName(String id, String name) throws Throwable {
        User user = new User();
        user.setId(id);
        user.setName(name);
        return userMapIndexIdAndName.get(getKey(user));
    }

    public List<User> getUserByDate(Date createDate) throws Throwable {
        List<User> users = new ArrayList<User>();
        users.add(userMapIndexId.get("1"));
        users.add(userMapIndexId.get("2"));
        users.add(userMapIndexId.get("3"));
        users.add(userMapIndexId.get("4"));
        return users;
    }

    public static void main(String[] args) {
    }

    public static String getKey(User user) {
        return user.getId() + ":" + user.getName();
    }
}