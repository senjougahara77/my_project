package com.maomao.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {

    // 生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5加密
    // hello -> abc123def456
    // hello + 随机字符串 -> abc123def456abc
    public static String md5(String key) {
        // 判断密码为空、null或者空格
        if (StringUtils.isBlank(key)) {
            return null;
        }
        // Spring自带的加密方法 加密为16进制要求传入为byte
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
