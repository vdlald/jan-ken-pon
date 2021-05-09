package com.vladislav.jankenpon.controllers.handlers;

import com.vladislav.jankenpon.pojo.GameRoom;
import com.vladislav.jankenpon.pojo.Player;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayerReadyHandler {

  private final SimpMessagingTemplate simpMessagingTemplate;

  public void handle(GameRoom gameRoom, Player player) {
    gameRoom.getGame().playerReady(player);

    simpMessagingTemplate.convertAndSend(
        String.format("/topic/%s/player.ready", gameRoom.getId()),
        Map.of("username", player.getUsername())
    );
  }
}
