package io.yyy.constant;

public interface MsgType {
    // 心跳
    String PING = "ping";
    String PONG = "pong";
    // 注册成功
    String REG_SUCCESS = "registered";
    // 发送分数
    String SCORE = "score";
    // 本方答题完成
    String SELFFINISHED = "self_finished";
    // 对方答题完成
    String OPPONENTFINISHED = "opponent_finished";
    // 发送题目
    String QUESTIONS = "question";
    // 请求匹配
    String PK = "pk";
    // 匹配成功
    String MATCHED = "matched";
    // PK离开
    String LEAVE = "leave";
    String SUCCESS = "100";
}
