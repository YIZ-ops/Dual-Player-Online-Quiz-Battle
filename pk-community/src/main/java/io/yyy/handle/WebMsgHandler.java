package io.yyy.handle;

import com.alibaba.fastjson.JSON;
import com.mysql.cj.util.StringUtils;
import io.yyy.constant.ClientStatus;
import io.yyy.constant.Commons;
import io.yyy.constant.MsgType;
import io.yyy.constant.RobotName;
import io.yyy.entity.PkRoom;
import io.yyy.entity.QuestionModel;
import io.yyy.entity.WebMsg;
import io.yyy.service.QuestionService;
import io.yyy.utils.StringToObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 消息处理类
 */
@Slf4j
@Component
public class WebMsgHandler {

    @Resource
    private QuestionService questionService;

    public void handle(WebSocketSession session, WebSocketMessage<?> msg) throws IOException {
        // 客户端传的 json 数据
        WebMsg webMsg = StringToObjectUtil.parseWebMsg(String.valueOf(msg.getPayload()));
        if (webMsg == null || StringUtils.isNullOrEmpty(webMsg.getUsername())) {
            // 消息为空或者用户名为空
            log.info("消息错误{}", webMsg);
            throw new RuntimeException("消息错误");
        }
        String type = webMsg.getType(); // 消息类型
        String username = webMsg.getUsername(); // 用户昵称
        String avatar = webMsg.getAvatar(); // 用户头像
        String roomId = webMsg.getRoomNo(); // 房间号
        // 注册消息
        WebSocketSession webSocketSession = WebSessionManager.get(username);
        if (webSocketSession == null) {
            register(session, webMsg);
        }
        switch (type) {
            // 心跳包
            case MsgType.PING:
                log.info("心跳包，用户[{}]", username);
                ping(session, username);
                break;
            // PK 请求
            case MsgType.PK:
                log.info("开始处理 PK 请求，请求客户端[{}]", username);
                pk(session, username, avatar);
                break;
            // 离开房间 把对应房间的位置空出来 但不断开连接
            case MsgType.LEAVE:
                log.info("离开房间{}", username);
                leave(session, username);
                break;
            // 收到分数 发送给对方分数
            case MsgType.SCORE:
                log.info("开始处理分数请求，请求客户端[{}]", username);
                score(roomId, username, webMsg.getData());
                break;
            // 客户端答题完成
            case MsgType.SELFFINISHED:
                log.info("用户{}完成答题", username);
                finished(roomId, username);
                break;
            // 客户端匹配等待超时，直接发送请求题目列表，这个时候要保证此客户端不在房间里，保险起见，再次尝试清理出房间
            case MsgType.QUESTIONS:
                log.info("用户{}匹配超时，请求题目列表", username);
                sendQuestions(session, username);
                break;
            default:
                break;
        }
    }

    /**
     * 注册
     *
     * @param session
     * @param webMsg
     */
    private void register(WebSocketSession session, WebMsg webMsg) throws IOException {
        // 注册
        WebSessionManager.add(webMsg.getUsername(), session);
        log.info("用户[{}]注册成功", webMsg.getUsername());
        TextMessage message = new TextMessage(new WebMsg().setData("register success")
                .setType(MsgType.REG_SUCCESS)
                .toJsonString());
        session.sendMessage(message);
    }

    /**
     * 心跳包
     *
     * @param session
     * @throws IOException
     */
    private void ping(WebSocketSession session, String username) throws IOException {
        if (session != null) {
            TextMessage message = new TextMessage(new WebMsg()
                    .setData("pong")
                    .setUsername(username)
                    .setType(MsgType.PONG).toJsonString());
            session.sendMessage(message);
        }
    }

    /**
     * 发送题目
     *
     * @param session
     * @param username
     * @throws IOException
     */
    private void sendQuestions(WebSocketSession session, String username) throws IOException {
        // 先清理出房间
        WebSessionManager.leaveRoom(username);
        // 获取题目列表
        List<QuestionModel> questionList = questionService.listRandomQuestion(5);
        // 客户端看到的机器人的昵称
        StringBuilder stringBuilder = new StringBuilder();
        if (username.length() % Commons.OU_NUM == 0) {
            stringBuilder.append(RobotName.A);
        } else {
            stringBuilder.append(RobotName.B);
        }
        stringBuilder.append(new Random().nextInt(101));
        // 返回信息
        TextMessage message = new TextMessage(new WebMsg()
                .setData(JSON.toJSONString(questionList))
                .setType(MsgType.QUESTIONS).setUsername(stringBuilder.toString()).toJsonString());
        session.sendMessage(message);
        log.info("下发题目成功,用户{}", username);
    }

    /**
     * 处理 PK 请求
     *
     * @param username 用户名
     * @param session  连接
     * @throws IOException
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
     *
     * @param session
     * @param pkRoom
     * @param username
     * @throws IOException
     */
    private void sendPkMatchedMessage(WebSocketSession session, PkRoom pkRoom, String username) throws IOException {
        String opponentUsername = username.equals(pkRoom.getSessionAUsername())
                ? pkRoom.getSessionBUsername() : pkRoom.getSessionAUsername();
        String opponentAvatar = username.equals(pkRoom.getSessionAUsername())
                ? pkRoom.getSessionBAvatar() : pkRoom.getSessionAAvatar();

        TextMessage result = new TextMessage(new WebMsg()
                .setData(pkRoom.getRoomNo())
                .setUsername(opponentUsername)
                .setAvatar(opponentAvatar)
                .setType(MsgType.MATCHED)
                .toJsonString());

        session.sendMessage(result);
    }

    /**
     * 处理匹配成功
     *
     * @param pkRoom
     * @throws IOException
     */

    private void handleSuccessfulMatch(PkRoom pkRoom) throws IOException {
        String roomNo = pkRoom.getRoomNo();
        WebSocketSession session1 = WebSessionManager.get(pkRoom.getSessionAUsername());
        WebSocketSession session2 = WebSessionManager.get(pkRoom.getSessionBUsername());

        if (session1 != null && session2 != null && session1.isOpen() && session2.isOpen()) {
            log.info("用户[{}]和[{}]匹配成功，房间号[{}]", pkRoom.getSessionAUsername(), pkRoom.getSessionBUsername(), roomNo);

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

            log.info("题目发送成功，客户端[{}]和[{}]", session1.getId(), session2.getId());
        }
    }

    private void handleMatchTimeout(WebSocketSession session, PkRoom pkRoom, String username) throws IOException {
        // 使用定时任务检测匹配超时
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            // 超时后检查是否已匹配
            if (pkRoom != null && pkRoom.checkEmpty()) {
                log.info("用户[{}]匹配超时，取消匹配", username);
                try {
                    session.sendMessage(new TextMessage(new WebMsg()
                            .setType(MsgType.MATCH_TIMEOUT)
                            .toJsonString()));
                } catch (IOException e) {
                    log.error("发送匹配超时消息失败", e);
                }
                WebSessionManager.remove(username);
            }
        }, 30, TimeUnit.SECONDS); // 设置超时时间 30 秒
        scheduler.shutdown(); // 任务完成后关闭 scheduler
    }

    /**
     * 离开房间
     *
     * @param session
     * @param username
     */
    private void leave(WebSocketSession session, String username) throws IOException {
        // 清理出房间
        WebSessionManager.leaveRoom(username);
        // 发送离开消息
        session.sendMessage(new TextMessage(new WebMsg()
                .setData("leave room")
                .setUsername(username)
                .setType(MsgType.SUCCESS).toJsonString()));
    }

    /**
     * 发送客户端分数到对方
     *
     * @param roomId
     * @param username
     * @param score
     * @throws IOException
     */
    private void score(String roomId, String username, Object score) throws IOException {
        // 获取房间
        PkRoom pkRoom = WebSessionManager.getRoom(roomId);
        if (null != pkRoom) {
            // 获取对方的session
            WebSocketSession session = WebSessionManager.get(username.equals(pkRoom.getSessionAUsername())
                    ? pkRoom.getSessionBUsername() : pkRoom.getSessionAUsername());
            if (session != null) {
                session.sendMessage(new TextMessage(new WebMsg()
                        .setData(score)
                        .setUsername(username)
                        .setType(MsgType.SCORE).toJsonString()));
            }
        }
    }

    /**
     * 客户端答题完成 发送消息到对方
     *
     * @param roomId
     * @param username
     */
    private void finished(String roomId, String username) throws IOException {
        // 获取房间
        PkRoom pkRoom = WebSessionManager.getRoom(roomId);
        if (null != pkRoom) {
            // 获取对方的session
            WebSocketSession session = WebSessionManager.get(username.equals(pkRoom.getSessionAUsername())
                    ? pkRoom.getSessionBUsername() : pkRoom.getSessionAUsername());
            if (session != null) {
                session.sendMessage(new TextMessage(new WebMsg()
                        .setData("finished")
                        .setUsername(username)
                        .setType(MsgType.OPPONENTFINISHED).toJsonString()));
            }
        }
    }


}
