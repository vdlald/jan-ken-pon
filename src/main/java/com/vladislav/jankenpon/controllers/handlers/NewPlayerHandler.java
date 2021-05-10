package com.vladislav.jankenpon.controllers.handlers;

import com.vladislav.jankenpon.components.PlayerFactory;
import com.vladislav.jankenpon.pojo.GameRoom;
import com.vladislav.jankenpon.pojo.Player;
import com.vladislav.jankenpon.utils.Credits;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewPlayerHandler {

  private final PlayerFactory playerFactory;
  private final SimpMessagingTemplate simpMessagingTemplate;

  public void handle(GameRoom gameRoom, Credits credits) {
    final Player player = playerFactory.create(credits);
    gameRoom.addPlayer(player);

    // notify all user about new player
    simpMessagingTemplate.convertAndSend(
        String.format("/topic/%s/gameRoom.newPlayer", gameRoom.getId()),
        new Response(player)
    );
  }

  @Value
  public static class Response {

    Player player;
  }
}
