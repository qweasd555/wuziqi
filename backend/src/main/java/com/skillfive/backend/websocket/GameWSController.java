package com.skillfive.backend.websocket;



import com.skillfive.backend.service.GameFlowService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;   // ← 必须加这行！！！

@Controller
public class GameWSController {

    private final GameFlowService gameFlowService;

    public GameWSController(GameFlowService gameFlowService) {
        this.gameFlowService = gameFlowService;
    }

    @MessageMapping("/game/move")
    @SendTo("/topic/game")
    public Object handleMove(@Payload Map<String, Object> msg) {
        System.out.println("收到落子消息 WS: " + msg);
        return msg;
    }
}
