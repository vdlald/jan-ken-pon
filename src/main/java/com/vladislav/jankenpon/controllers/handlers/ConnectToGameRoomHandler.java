package com.vladislav.jankenpon.controllers.handlers;

import com.vladislav.jankenpon.components.GameRoomSerializer;
import com.vladislav.jankenpon.components.PlayerFactory;
import com.vladislav.jankenpon.pojo.GameRoom;
import com.vladislav.jankenpon.pojo.Player;
import com.vladislav.jankenpon.utils.Credits;
import com.vladislav.jankenpon.utils.WebsocketUtils;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConnectToGameRoomHandler {

  private final PlayerFactory playerFactory;
  private final WebsocketUtils websocketUtils;
  private final GameRoomSerializer gameRoomSerializer;
  private final SimpMessagingTemplate simpMessagingTemplate;

  public void handle(GameRoom gameRoom, Principal principal, Credits credits) {
    final Optional<Player> optional = gameRoom.findPlayer(credits.getUsername());
    if (optional.isEmpty()) {
      // register new player in game room
      final Player player = playerFactory.create(credits);
      gameRoom.addPlayer(player);

      // notify all user about new player
      simpMessagingTemplate.convertAndSend(
          String.format("/topic/%s/gameRoom.newPlayer", gameRoom.getId()),
          Map.of("player", player)
      );
    } else {
      // authorize player
      websocketUtils.authorize(gameRoom, credits);
    }

    // send game room to connected player
    simpMessagingTemplate.convertAndSendToUser(
        principal.getName(),
        "/queue/reply/gameRoom.connected",
        Map.of("gameRoom", gameRoomSerializer.serialize(gameRoom))
    );
  }
}
