package com.fastcampus.projectboard.config;

public class Constants {
    public static final String[] PermitAllUrl = {"/login","/","/logout","/signup","/articles","/articles/{articleID}"
            ,"/users/**","/articles/search-hashtag/**"};
    public static final String[] ResourceArray = {"/css/**","/js/**","/img/**","/lib/**","/profileImg/**"};
    public static final String[] UnPermitAllUrl = {"/articles/post","/articles/{articleId}/**"};
}
