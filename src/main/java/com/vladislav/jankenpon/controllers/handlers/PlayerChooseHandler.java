package com.vladislav.jankenpon.controllers.handlers;

import com.vladislav.jankenpon.pojo.Game;
import com.vladislav.jankenpon.pojo.GameRoom;
import com.vladislav.jankenpon.pojo.GameTransaction;
import com.vladislav.jankenpon.pojo.GameTransaction.Choice;
import com.vladislav.jankenpon.pojo.Player;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayerChooseHandler {

  private final TryEndRoundHandler tryEndRoundHandler;
  private final SimpMessagingTemplate simpMessagingTemplate;

  public void handle(GameRoom gameRoom, Player player, Request request) {
    final Game game = gameRoom.getGame();

    // set choose
    final GameTransaction transaction = game.getCurrentTransaction();
    transaction.setPlayerChoice(player, request.choice);

    simpMessagingTemplate.convertAndSend(
        String.format("/topic/%s/player.chosen", gameRoom.getId()),
        new Response(player.getUsername())
    );

    tryEndRoundHandler.handle(gameRoom);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Request {

    private Choice choice;
  }

  @Value
  public static class Response {

    String username;
  }
}
