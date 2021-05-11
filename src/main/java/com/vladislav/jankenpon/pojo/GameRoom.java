package com.vladislav.jankenpon.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GameRoom {

  private UUID id;
  private Game game;
  private List<Player> players = new ArrayList<>();

  public Player getPlayer(String username) {
    final Optional<Player> optional = findPlayer(username);
    if (optional.isPresent()) {
      return optional.get();
    }
    throw new RuntimeException("player not found");
  }

  public Optional<Player> findPlayer(String username) {
    for (Player player : players) {
      if (player.getUsername().equals(username)) {
        return Optional.of(player);
      }
    }
    return Optional.empty();
  }

  public void addPlayer(Player player) {
    final String username = player.getUsername();

    final Optional<Player> optional = findPlayer(username);
    if (optional.isPresent()) {
      throw new RuntimeException(String.format("Player with %s username already exist", username));
    }

    players.add(player);
  }

  @JsonIgnore
  public Set<String> getPlayersUsernames() {
    return players.stream()
        .map(Player::getUsername)
        .collect(Collectors.toUnmodifiableSet());
  }
}
