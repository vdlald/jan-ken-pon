package com.vladislav.jankenpon.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladislav.jankenpon.pojo.GameRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameRoomSerializer {

  private final ObjectMapper objectMapper;

  public String serialize(GameRoom gameRoom) {
    try {
      return objectMapper.writeValueAsString(gameRoom);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
