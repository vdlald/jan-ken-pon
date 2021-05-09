package com.vladislav.jankenpon.components;

import com.vladislav.jankenpon.pojo.Game;
import com.vladislav.jankenpon.pojo.GameRoom;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GameRoomFactory {

  public GameRoom create(int maxRounds) {
    final Game game = new Game()
        .setMaxRounds(maxRounds);
    return new GameRoom()
        .setId(UUID.randomUUID())
        .setGame(game);
  }
}
