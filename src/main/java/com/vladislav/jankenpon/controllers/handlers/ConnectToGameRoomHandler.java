package com.vladislav.jankenpon.controllers.handlers;

import com.vladislav.jankenpon.pojo.GameRoom;
import com.vladislav.jankenpon.pojo.Player;
import com.vladislav.jankenpon.utils.Credits;
import com.vladislav.jankenpon.utils.WebsocketUtils;
import java.security.Principal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConnectToGameRoomHandler {

  private final WebsocketUtils websocketUtils;
  private final NewPlayerHandler newPlayerHandler;
  private final SimpMessagingTemplate simpMessagingTemplate;

  public void handle(GameRoom gameRoom, Principal principal, Credits credits) {
    final Optional<Player> optional = gameRoom.findPlayer(credits.getUsername());

    if (optional.isEmpty()) {
      if (gameRoom.getGame().isStarted()) {
        throw new RuntimeException("Can't create a new player, because the game started");
      }
      newPlayerHandler.handle(gameRoom, credits);
    } else {
      // authorize player
      websocketUtils.authorize(gameRoom, credits);
    }

    // send game room to connected player
    simpMessagingTemplate.convertAndSendToUser(
        principal.getName(),
        "/queue/reply/gameRoom.connected",
        new Response(gameRoom)
    );
  }

  @Value
  public static class Response {

    GameRoom gameRoom;
  }
}
