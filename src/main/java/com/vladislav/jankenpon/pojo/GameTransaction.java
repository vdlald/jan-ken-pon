package com.vladislav.jankenpon.pojo;

import com.vladislav.jankenpon.utils.Pair;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GameTransaction {

  private int round;

  @Setter(AccessLevel.NONE)
  private Map<String, Choice> playersChoice = new HashMap<>();
  private Set<String> requiredPlayers = new HashSet<>();

  public void setPlayerChoice(Player player, Choice choice) {
    if (!requiredPlayers.contains(player.getUsername())) {
      throw new RuntimeException("the player does not play in this transaction");
    }
    playersChoice.put(player.getUsername(), choice);
  }

  public boolean isTransactionFull() {
    return playersChoice.keySet().equals(requiredPlayers);
  }

  @AllArgsConstructor
  public enum Choice {
    ROCK, PAPER, SCISSORS;

    public int whichStronger(Choice choice) {
      if (this == choice) {
        return 0;  // draw
      }
      final Choice weaker = map.get(this).getFirst();
      if (weaker == choice) {
        return 1;  // this stronger
      } else {
        return -1;  // choice stronger
      }
    }

    private static Map<Choice, Pair<Choice, Choice>> map = new EnumMap<>(Choice.class);

    static {
      // rock stronger than scissors, but weaker than paper
      map.put(ROCK, Pair.of(SCISSORS, PAPER));
      map.put(PAPER, Pair.of(ROCK, SCISSORS));
      map.put(SCISSORS, Pair.of(PAPER, ROCK));
    }
  }
}
