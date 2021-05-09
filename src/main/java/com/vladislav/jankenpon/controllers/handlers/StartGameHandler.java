package com.vladislav.jankenpon.controllers.handlers;

import com.vladislav.jankenpon.pojo.Game;
import com.vladislav.jankenpon.pojo.GameRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartGameHandler {

  private final NextRoundHandler nextRoundHandler;
  private final SimpMessagingTemplate simpMessagingTemplate;

  public void handle(GameRoom gameRoom) {
    final Game game = gameRoom.getGame();
    if (game.getReadyPlayers().size() != gameRoom.getPlayers().size()) {
      throw new RuntimeException("not all players are ready");
    }

    game.start();
    simpMessagingTemplate.convertAndSend(
        String.format("/topic/%s/game.started", gameRoom.getId()),
        ""
    );

    nextRoundHandler.handle(gameRoom);
  }
}
