package com.fraudshield.alert.websocket;


import com.fraudshield.alert.dto.FraudAlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@RequiredArgsConstructor
public class WebSocketController {

    @MessageMapping("/alerts")
    @SendTo("/topic/fraud-alerts")
    public FraudAlertEvent handleAlert(FraudAlertEvent event) {
        return event;
    }

}
