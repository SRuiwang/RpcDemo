package com.sr.app.service.inte;

import com.sr.app.pojo.User;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

public interface UserService {

    public User getUserById(String id) throws Throwable;

    public User getUserByIdAndName(String id, String name) throws Throwable;

    public List<User> getUserByDate(Date createDate) throws Throwable;
}