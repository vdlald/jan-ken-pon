package com.vladislav.jankenpon.components;

import com.vladislav.jankenpon.pojo.GameRoom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GameRoomsHolder {

  private final Map<UUID, GameRoom> rooms = new HashMap<>();

  public void addRoom(GameRoom gameRoom) {
    rooms.put(gameRoom.getId(), gameRoom);
  }

  public GameRoom getGameRoom(UUID id) {
    final GameRoom room = rooms.get(id);
    if (room == null) {
      throw new RuntimeException("GameRoom not found");
    }
    return room;
  }
}
