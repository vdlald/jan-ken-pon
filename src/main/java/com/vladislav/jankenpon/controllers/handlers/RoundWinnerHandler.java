package com.vladislav.jankenpon.controllers.handlers;

import com.vladislav.jankenpon.pojo.Game;
import com.vladislav.jankenpon.pojo.GameRoom;
import com.vladislav.jankenpon.pojo.GameTransaction.Choice;
import com.vladislav.jankenpon.utils.Pair;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoundWinnerHandler {

  private final SimpMessagingTemplate simpMessagingTemplate;
  private final NextRoundHandler nextRoundHandler;

  public void handle(GameRoom gameRoom, String username) {
    final Game game = gameRoom.getGame();
    game.addRoundWinner(username);

    simpMessagingTemplate.convertAndSend(
        String.format("/topic/%s/game.round.winner", gameRoom.getId()),
        new Response(username, game.getCurrentTransaction().getPlayersChoicesList())
    );

    nextRoundHandler.handle(gameRoom);
  }

  @Value
  public static class Response {

    String winner;
    List<Pair<String, Choice>> playersChoices;

  }
}
