package com.vladislav.jankenpon.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Game {

  private boolean started;
  private Set<String> readyPlayers = new HashSet<>();
  private boolean finished;

  @JsonIgnore
  private List<GameTransaction> gameTransactions = new ArrayList<>();

  @Setter(AccessLevel.NONE)
  private int round;

  private int maxRounds;

  // index + 1 = round
  private List<String> roundsWinners = new ArrayList<>();

  public void playerReady(Player player) {
    readyPlayers.add(player.getUsername());
  }

  public void start() {
    started = true;
  }

  public void addTransaction(GameTransaction gameTransaction) {
    gameTransactions.add(gameTransaction);
  }

  @JsonIgnore
  public GameTransaction getCurrentTransaction() {
    return gameTransactions.get(gameTransactions.size() - 1);
  }

  public void incrementRound() {
    round++;
  }

  public void addRoundWinner(String username) {
    roundsWinners.add(username);
  }
}
