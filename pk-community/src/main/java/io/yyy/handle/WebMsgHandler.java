package io.yyy.handle;

import com.mysql.cj.util.StringUtils;
import io.yyy.constant.ClientStatus;
import io.yyy.constant.MsgType;
import io.yyy.entity.PkRoom;
import io.yyy.entity.QuestionModel;
import io.yyy.entity.WebMsg;
import io.yyy.service.QuestionService;
import io.yyy.utils.StringToObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * 消息处理类
 */
@Slf4j
@Component
public class WebMsgHandler {

    @Resource
    private QuestionService questionService;

    public void handle(WebSocketSession session, WebSocketMessage<?> msg) throws IOException {

        try {
            // 客户端传的 json 数据
            WebMsg webMsg = StringToObjectUtil.parseWebMsg(String.valueOf(msg.getPayload()));
            if (webMsg == null || StringUtils.isNullOrEmpty(webMsg.getUsername())) {
                // 消息为空或者用户名为空
                log.info("Message or username is null{}", webMsg);
                return;
            }
            String type = webMsg.getType(); // 消息类型
            String username = webMsg.getUsername(); // 用户昵称
            WebSocketSession webSocketSession = WebSessionManager.get(username);
            // 注册消息
            if (webSocketSession == null) {
                register(session, webMsg);
            }
            processMessage(session, webMsg, type, username);
        } catch (Exception e) {
            log.error("Error handling message", e);
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    private void processMessage(WebSocketSession session, WebMsg webMsg, String type, String username) throws IOException {
        String roomId = webMsg.getRoomNo();
        switch (type) {
            // 心跳包
            case MsgType.PING:
                log.info("Heartbeat received from [{}]", username);
                ping(session, username);
                break;
            // PK 请求
            case MsgType.PK:
                log.info("Processing PK request from [{}]", username);
                pk(session, username, webMsg.getAvatar());
                break;
            // 离开房间 把对应房间的位置空出来 但不断开连接
            case MsgType.LEAVE:
                log.info("User [{}] leaving room", username);
                leave(session, username);
                break;
            // 收到分数 发送给对方分数
            case MsgType.SCORE:
                log.info("Processing score from [{}]", username);
                score(roomId, username, webMsg.getData());
                break;
            // 客户端答题完成
            case MsgType.SELFFINISHED:
                log.info("User [{}] finished quiz", username);
                finished(roomId, username);
                break;
            default:
                log.warn("Unknown message type: {}", type);
                break;
        }
    }

    private void sendMessage(WebSocketSession session, WebMsg webMsg) throws IOException {
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(webMsg.toJsonString()));
        }
    }

    /**
     * 注册
     */
    private void register(WebSocketSession session, WebMsg webMsg) throws IOException {
        WebSessionManager.add(webMsg.getUsername(), session);
        log.info("User [{}] registered successfully", webMsg.getUsername());
        sendMessage(session, new WebMsg()
                .setData("register success")
                .setType(MsgType.REG_SUCCESS));
    }

    /**
     * 心跳包
     */
    private void ping(WebSocketSession session, String username) throws IOException {
        if (session != null && session.isOpen()) {
            sendMessage(session, new WebMsg()
                    .setData("pong")
                    .setUsername(username)
                    .setType(MsgType.PONG));
        }
    }

    /**
     * 处理 PK 请求
     */
    private void pk(WebSocketSession session, String username, String avatar) throws IOException {
        // 获取请求客户端的 session
        WebSocketSession userSession = WebSessionManager.get(username);
        if (userSession == null) {
            WebSessionManager.add(username, session);
        }
        // 搜寻房间
        String room = WebSessionManager.checkRoom(username);
        PkRoom pkRoom;
        // 当前用户不在房间
        if (StringUtils.isNullOrEmpty(room)) {
            pkRoom = WebSessionManager.setInRoom(username, avatar);
        } else {
            // 用户已经在房间，直接返回房间信息
            pkRoom = WebSessionManager.getRoom(room);
            if (pkRoom != null && !pkRoom.checkEmpty()) {
                sendPkMatchedMessage(session, pkRoom, username);
                return;
            }
        }
        // 匹配成功
        if (pkRoom != null && !pkRoom.checkEmpty() && ClientStatus.READY.equals(pkRoom.getStatus())) {
            handleSuccessfulMatch(pkRoom);
        }
    }

    /**
     * 发送匹配成功消息
     */
    private void sendPkMatchedMessage(WebSocketSession session, PkRoom pkRoom, String username) throws IOException {
        String opponentUsername = username.equals(pkRoom.getSessionAUsername())
                ? pkRoom.getSessionBUsername() : pkRoom.getSessionAUsername();
        String opponentAvatar = username.equals(pkRoom.getSessionAUsername())
                ? pkRoom.getSessionBAvatar() : pkRoom.getSessionAAvatar();
        sendMessage(session, new WebMsg()
                .setData(pkRoom.getRoomNo())
                .setUsername(opponentUsername)
                .setAvatar(opponentAvatar)
                .setType(MsgType.MATCHED));
    }

    /**
     * 处理匹配成功
     */
    private void handleSuccessfulMatch(PkRoom pkRoom) throws IOException {
        String roomNo = pkRoom.getRoomNo();
        WebSocketSession session1 = WebSessionManager.get(pkRoom.getSessionAUsername());
        WebSocketSession session2 = WebSessionManager.get(pkRoom.getSessionBUsername());

        if (session1 != null && session2 != null && session1.isOpen() && session2.isOpen()) {
            log.info("Users [{}] and [{}] matched successfully in room [{}]", pkRoom.getSessionAUsername(), pkRoom.getSessionBUsername(), roomNo);

            sendPkMatchedMessage(session1, pkRoom, pkRoom.getSessionAUsername());
            sendPkMatchedMessage(session2, pkRoom, pkRoom.getSessionBUsername());

            List<QuestionModel> questionList = questionService.listRandomQuestion(5);
            TextMessage questions = new TextMessage(new WebMsg()
                    .setData(questionList)
                    .setType(MsgType.QUESTIONS)
                    .setRoomNo(roomNo)
                    .toJsonString());

            session1.sendMessage(questions);
            session2.sendMessage(questions);

            log.info("Questions sent successfully to [{}] and [{}]", session1.getId(), session2.getId());
        }
    }

    /**
     * 离开房间
     */
    private void leave(WebSocketSession session, String username) throws IOException {
        // 清理出房间
        WebSessionManager.leaveRoom(username);
        sendMessage(session, new WebMsg()
                .setData("leave room")
                .setUsername(username)
                .setType(MsgType.SUCCESS));
    }

    /**
     * 发送客户端分数到对方
     */
    private void score(String roomId, String username, Object score) throws IOException {
        // 获取房间
        PkRoom pkRoom = WebSessionManager.getRoom(roomId);
        if (pkRoom != null) {
            String opponentUsername = username.equals(pkRoom.getSessionAUsername())
                    ? pkRoom.getSessionBUsername() : pkRoom.getSessionAUsername();
            WebSocketSession opponentSession = WebSessionManager.get(opponentUsername);
            if (opponentSession != null && opponentSession.isOpen()) {
                sendMessage(opponentSession, new WebMsg()
                        .setData(score)
                        .setUsername(username)
                        .setType(MsgType.SCORE));
            }
        }
    }

    /**
     * 客户端答题完成 发送消息到对方
     */
    private void finished(String roomId, String username) throws IOException {
        // 获取房间
        PkRoom pkRoom = WebSessionManager.getRoom(roomId);
        if (pkRoom != null) {
            String opponentUsername = username.equals(pkRoom.getSessionAUsername())
                    ? pkRoom.getSessionBUsername() : pkRoom.getSessionAUsername();
            WebSocketSession opponentSession = WebSessionManager.get(opponentUsername);
            if (opponentSession != null && opponentSession.isOpen()) {
                sendMessage(opponentSession, new WebMsg()
                        .setData("finished")
                        .setUsername(username)
                        .setType(MsgType.OPPONENTFINISHED));
            }
        }
    }

}
