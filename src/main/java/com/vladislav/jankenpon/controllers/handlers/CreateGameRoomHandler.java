package com.vladislav.jankenpon.controllers.handlers;

import com.vladislav.jankenpon.components.GameRoomFactory;
import com.vladislav.jankenpon.components.GameRoomsHolder;
import com.vladislav.jankenpon.pojo.GameRoom;
import java.security.Principal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateGameRoomHandler {

  private final GameRoomFactory gameRoomFactory;
  private final GameRoomsHolder gameRoomsHolder;
  private final SimpMessagingTemplate simpMessagingTemplate;

  public void handle(Principal principal, Request request) {
    final GameRoom gameRoom = gameRoomFactory.create(request.maxRounds);
    gameRoomsHolder.addRoom(gameRoom);

    simpMessagingTemplate.convertAndSendToUser(
        principal.getName(),
        "/queue/reply/gameRoom.created",
        new Response(gameRoom.getId().toString())
    );
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Request {

    private int maxRounds;
  }

  @Value
  public static class Response {

    String gameRoomId;
  }
}
