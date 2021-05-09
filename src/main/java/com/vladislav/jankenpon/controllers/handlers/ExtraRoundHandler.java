package com.vladislav.jankenpon.controllers.handlers;

import com.vladislav.jankenpon.pojo.Game;
import com.vladislav.jankenpon.pojo.GameRoom;
import com.vladislav.jankenpon.pojo.GameTransaction;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExtraRoundHandler {

  private final SimpMessagingTemplate simpMessagingTemplate;

  public void handle(GameRoom gameRoom, List<String> usernames) {
    final Game game = gameRoom.getGame();

    game.incrementRound();

    final GameTransaction gameTransaction = new GameTransaction()
        .setRequiredPlayers(new HashSet<>(usernames))
        .setRound(game.getRound());
    game.addTransaction(gameTransaction);

    simpMessagingTemplate.convertAndSend(
        String.format("/topic/%s/game.extraRound", gameRoom.getId()),
        new Response(usernames)
    );
  }

  @Value
  public static class Response {

    List<String> usernames;
  }
}
