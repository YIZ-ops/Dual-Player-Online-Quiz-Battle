package io.yyy.utils;

import com.alibaba.fastjson.JSON;
import io.yyy.entity.WebMsg;

public class StringToObjectUtil {
    public static WebMsg parseWebMsg(String msg) {
        return JSON.parseObject(msg, WebMsg.class);
    }
}
