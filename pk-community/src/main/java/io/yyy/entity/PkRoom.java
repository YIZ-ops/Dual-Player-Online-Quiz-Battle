package io.yyy.entity;

import io.yyy.constant.ClientStatus;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PkRoom {

    /**
     * 房间编号
     */
    private String roomNo;
    /**
     * 房间状态
     */
    private String status;
    /**
     * 房间A方
     */
    private String sessionAUsername;
    /**
     * 房间A方头像
     */
    private String sessionAAvatar;
    /**
     * 房间B方
     */
    private String sessionBUsername;
    /**
     * 房间B方头像
     */
    private String sessionBAvatar;

    /**
     * 用户数量
     */
    private Integer userNum = 0;


    public boolean checkEmpty() {
        return sessionAUsername == null || sessionBUsername == null || ClientStatus.WAIT.equals(status);
    }

}
