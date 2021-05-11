package com.vladislav.jankenpon.controllers.handlers;

import com.vladislav.jankenpon.pojo.GameRoom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FinishGameHandler {

  private final ExtraRoundHandler extraRoundHandler;
  private final SimpMessagingTemplate simpMessagingTemplate;

  public void handle(GameRoom gameRoom) {
    // if there are many winners in the game, we must create a new round with winners
    final List<String> roundsWinner = gameRoom.getGame().getRoundsWinners();

    final Map<String, Integer> usernameToWins = new HashMap<>();
    for (final String username : roundsWinner) {
      usernameToWins.merge(username, 1, Integer::sum);
    }

    final Map<Integer, List<String>> winsToUsernames = new HashMap<>();
    for (Entry<String, Integer> entry : usernameToWins.entrySet()) {
      final String username = entry.getKey();
      final Integer winsCount = entry.getValue();

      final List<String> usernames = winsToUsernames.get(winsCount);
      if (usernames == null) {
        winsToUsernames.put(winsCount, new ArrayList<>(Collections.singletonList(username)));
      } else {
        usernames.add(username);
      }
    }

    final Integer max = winsToUsernames.keySet().stream().max(Integer::compareTo).get();

    final List<String> usernames = winsToUsernames.get(max);
    if (usernames.size() == 1) {
      final String username = usernames.get(0);

      gameRoom.getGame().setFinished(true);

      simpMessagingTemplate.convertAndSend(
          String.format("/topic/%s/game.finish", gameRoom.getId()),
          new Response(username, roundsWinner)
      );
    } else {
      extraRoundHandler.handle(gameRoom, usernames);
    }
  }

  @Value
  public static class Response {

    String winner;
    List<String> roundsWinners;
  }
}
