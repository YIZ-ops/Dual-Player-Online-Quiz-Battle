package io.yyy.handle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import javax.annotation.Resource;

@Slf4j
@Component
public class MyWsHandler extends AbstractWebSocketHandler {

    @Resource
    private WebMsgHandler msgHandler;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.debug("[{}] connected", session.getId());
        super.afterConnectionEstablished(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        log.debug("Received message: {}", message.getPayload());
        msgHandler.handle(session, message);
        super.handleMessage(session, message);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        super.handleBinaryMessage(session, message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Error occurred", exception);
        super.handleTransportError(session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.debug("[{}] disconnected", session.getId());
        WebSessionManager.removeByData(session);
        super.afterConnectionClosed(session, status);
    }
}
