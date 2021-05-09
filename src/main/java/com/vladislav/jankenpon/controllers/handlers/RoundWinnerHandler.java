package com.vladislav.jankenpon.controllers.handlers;

import com.vladislav.jankenpon.pojo.GameRoom;
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
    gameRoom.getGame().addRoundWinner(username);

    simpMessagingTemplate.convertAndSend(
        String.format("/topic/%s/game.round.result", gameRoom.getId()),
        new Response(username)
    );

    nextRoundHandler.handle(gameRoom);
  }

  @Value
  public static class Response {

    String winner;
  }
}
