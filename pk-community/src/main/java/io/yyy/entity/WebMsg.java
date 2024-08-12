package io.yyy.entity;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class WebMsg implements Serializable {

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 消息类型   MsgType
     */
    private String type;

    /**
     * 内容
     */
    private Object data;

    /**
     * 房间编号
     */
    private String roomNo;

    /**
     * 转化成json字符串
     *
     * @return
     */
    public String toJsonString() {
        return JSON.toJSONString(this);
    }

}
