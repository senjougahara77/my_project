package com.maomao.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
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

    // 将json对象转化为json字符串 包含提示信息string和业务数据map 便于前后端交互
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        // 获取json对象
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    // 重载方法，如果没有string或者没有map的情况下
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "zhangsan");
        map.put("age", 25);
        System.out.println(getJSONString(0, "ok", map));
    }
}
