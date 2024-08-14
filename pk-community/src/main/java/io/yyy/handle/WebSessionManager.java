package io.yyy.handle;

import com.mysql.cj.util.StringUtils;
import io.yyy.constant.ClientStatus;
import io.yyy.entity.PkRoom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WebSessionManager {

    public static final ConcurrentHashMap<String, WebSocketSession> SESSION_POOL = new ConcurrentHashMap<>(64);
    public static final Map<String, PkRoom> roomMap = new ConcurrentHashMap<>();

    /**
     * 用户名和 session 绑定
     */
    public static void add(String key, WebSocketSession session) {
        SESSION_POOL.put(key, session);
        log.info("User [{}] bound to Session [{}]", key, session.getId());
        log.info("User [{}] entered PK waiting", key);
    }

    /**
     * 用户名和 session 解绑
     */
    public static WebSocketSession remove(String key) {
        log.info("User [{}] left the room", key);
        WebSocketSession session = SESSION_POOL.remove(key);
        if (session != null) {
            log.info("User [{}] unbound from Session [{}]", key, session.getId());
        }
        return session;
    }

    /**
     * 删除并同步关闭连接
     */
    public static void removeAndClose(String key) {
        WebSocketSession session = remove(key);
        if (session != null) {
            try {
                session.close();
                log.info("Session [{}] closed", session.getId());
            } catch (IOException e) {
                log.error("Error closing session [{}]: {}", session.getId(), e.getMessage());
            }
        }
    }

    /**
     * 通过 session 把绑定的用户给删除以及踢出房间
     */
    public static void removeByData(WebSocketSession session) {
        String username = null;
        for (Map.Entry<String, WebSocketSession> entry : SESSION_POOL.entrySet()) {
            if (entry.getValue() == session) {
                username = entry.getKey();
                SESSION_POOL.remove(username);
                break;
            }
        }
        if (!StringUtils.isNullOrEmpty(username)) {
            removeUserFromRoom(username);
        }
    }

    /**
     * 通过昵称获得 session
     */
    public static WebSocketSession get(String key) {
        return StringUtils.isNullOrEmpty(key) ? null : SESSION_POOL.get(key);
    }

    /**
     * 返回房间号，有值则表示已进入房间
     */
    public static String checkRoom(String username) {
        if (StringUtils.isNullOrEmpty(username)) {
            return null;
        }
        PkRoom room = roomMap.values().stream()
                .filter(r -> username.equals(r.getSessionAUsername()) || username.equals(r.getSessionBUsername()))
                .findFirst()
                .orElse(null);
        return room != null ? room.getRoomNo() : null;
    }

    /**
     * 进入房间，找到一个剩余一个位置的的房间，没有则新建一个房间
     */
    public static synchronized PkRoom setInRoom(String username, String avatar) {
        for (PkRoom room : roomMap.values()) {
            if (room.checkEmpty() && ClientStatus.WAIT.equals(room.getStatus())) {
                if (StringUtils.isNullOrEmpty(room.getSessionAUsername())) {
                    room.setSessionAUsername(username);
                    room.setSessionAAvatar(avatar);
                } else if (StringUtils.isNullOrEmpty(room.getSessionBUsername())) {
                    room.setSessionBUsername(username);
                    room.setSessionBAvatar(avatar);
                }
                room.setUserNum(room.getUserNum() + 1);
                if (room.getUserNum() == 2) {
                    room.setStatus(ClientStatus.READY);
                }
                return room;
            }
        }
        // 新建一个房间
        String roomNo = UUID.randomUUID().toString();
        PkRoom pkRoom = new PkRoom()
                .setRoomNo(roomNo)
                .setSessionAUsername(username)
                .setSessionAAvatar(avatar)
                .setStatus(ClientStatus.WAIT)
                .setUserNum(1);
        roomMap.put(roomNo, pkRoom);
        return pkRoom;
    }


    /**
     * 获取房间信息
     */
    public static PkRoom getRoom(String roomNo) {
        return roomMap.get(roomNo);
    }

    /**
     * 离开房间
     */
    public static void leaveRoom(String username) {
        if (!StringUtils.isNullOrEmpty(username)) {
            removeUserFromRoom(username);
        }
    }

    private static void removeUserFromRoom(String username) {
        for (PkRoom room : roomMap.values()) {
            if (username.equals(room.getSessionAUsername())) {
                room.setSessionAUsername(null);
                room.setSessionAAvatar(null);
                room.setUserNum(room.getUserNum() - 1);
                room.setStatus(ClientStatus.WAIT);
            } else if (username.equals(room.getSessionBUsername())) {
                room.setSessionBUsername(null);
                room.setSessionBAvatar(null);
                room.setUserNum(room.getUserNum() - 1);
                room.setStatus(ClientStatus.WAIT);
            }
        }
    }
}
