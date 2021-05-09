package com.vladislav.jankenpon.controllers.handlers;

import com.vladislav.jankenpon.pojo.Game;
import com.vladislav.jankenpon.pojo.GameRoom;
import com.vladislav.jankenpon.pojo.GameTransaction;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NextRoundHandler {

  private final FinishGameHandler finishGameHandler;
  private final SimpMessagingTemplate simpMessagingTemplate;

  public void handle(GameRoom gameRoom) {
    final Game game = gameRoom.getGame();

    if (game.getRound() >= game.getMaxRounds()) {
      // finish game
      finishGameHandler.handle(gameRoom);
    } else {
      // keep playing
      game.incrementRound();

      final GameTransaction gameTransaction = new GameTransaction()
          .setRequiredPlayers(gameRoom.getPlayersUsernames())
          .setRound(game.getRound());
      game.addTransaction(gameTransaction);

      simpMessagingTemplate.convertAndSend(
          String.format("/topic/%s/game.nextRound", gameRoom.getId()),
          new Response(game.getRound())
      );
    }
  }

  @Value
  public static class Response {

    int round;
  }
}
