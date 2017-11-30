package com.sr.app;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AppServerStart {
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("spring.xml");
    }
}