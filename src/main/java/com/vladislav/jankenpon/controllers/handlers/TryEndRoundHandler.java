package com.vladislav.jankenpon.controllers.handlers;

import com.vladislav.jankenpon.pojo.Game;
import com.vladislav.jankenpon.pojo.GameRoom;
import com.vladislav.jankenpon.pojo.GameTransaction;
import com.vladislav.jankenpon.pojo.GameTransaction.Choice;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TryEndRoundHandler {

  private final RoundWinnerHandler roundWinnerHandler;
  private final SimpMessagingTemplate simpMessagingTemplate;

  public void handle(GameRoom gameRoom) {
    final Game game = gameRoom.getGame();
    final GameTransaction transaction = game.getCurrentTransaction();

    if (!transaction.isTransactionFull()) {
      return;
    }

    final Map<String, Choice> playersChoice = transaction.getPlayersChoice();
    final Collection<Choice> choices = playersChoice.values();

    if (choices.size() == 2) {
      hasWinner(gameRoom, playersChoice, choices);
    } else {
      draw(gameRoom, transaction);
    }
  }

  // the round is not over
  private void draw(GameRoom gameRoom, GameTransaction transaction) {
    final Game game = gameRoom.getGame();
    final Set<String> usernames = transaction.getRequiredPlayers();

    final GameTransaction newTransaction = new GameTransaction()
        .setRequiredPlayers(usernames)
        .setRound(game.getRound());
    game.addTransaction(newTransaction);

    simpMessagingTemplate.convertAndSend(
        String.format("/topic/%s/game.round.draw", gameRoom.getId()),
        Map.of("usernames", List.of(usernames))
    );
  }

  private void hasWinner(
      GameRoom gameRoom,
      Map<String, Choice> playersChoices,
      Collection<Choice> choices
  ) {
    final Game game = gameRoom.getGame();

    // init choicesToPlayers
    final Map<Choice, List<String>> choicesToPlayers = new EnumMap<>(Choice.class);
    for (Choice value : choices) {
      choicesToPlayers.put(value, new ArrayList<>());
    }

    // set choicesToPlayers
    for (Entry<String, Choice> entry : playersChoices.entrySet()) {
      final List<String> list = choicesToPlayers.get(entry.getValue());
      list.add(entry.getKey());
    }

    // determine winners
    final List<Choice> choicesList = new ArrayList<>(choices);
    final Choice first = choicesList.get(0);
    final Choice second = choicesList.get(1);

    final List<String> winners;
    final int i = first.whichStronger(second);
    if (i == 1) {
      // first group stronger
      winners = choicesToPlayers.get(first);
    } else {
      // second group stronger
      winners = choicesToPlayers.get(second);
    }

    if (winners.size() == 1) {
      // one player wins
      roundWinnerHandler.handle(gameRoom, winners.get(0));
    } else {
      // the next round does not start
      final GameTransaction gameTransaction = new GameTransaction()
          .setRequiredPlayers(new HashSet<>(winners))
          .setRound(game.getRound());
      game.addTransaction(gameTransaction);

      // this means that the winners play each other
      simpMessagingTemplate.convertAndSend(
          String.format("/topic/%s/game.round.draw", gameRoom.getId()),
          Map.of("usernames", winners)
      );
    }
  }
}
