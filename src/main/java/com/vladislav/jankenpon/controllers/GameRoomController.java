package com.vladislav.jankenpon.controllers;

import com.vladislav.jankenpon.components.GameRoomsHolder;
import com.vladislav.jankenpon.controllers.handlers.ConnectToGameRoomHandler;
import com.vladislav.jankenpon.controllers.handlers.CreateGameRoomHandler;
import com.vladislav.jankenpon.controllers.handlers.PlayerChooseHandler;
import com.vladislav.jankenpon.controllers.handlers.PlayerReadyHandler;
import com.vladislav.jankenpon.controllers.handlers.StartGameHandler;
import com.vladislav.jankenpon.pojo.GameRoom;
import com.vladislav.jankenpon.pojo.Player;
import com.vladislav.jankenpon.utils.Credits;
import com.vladislav.jankenpon.utils.WebsocketUtils;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GameRoomController {

  private final WebsocketUtils websocketUtils;
  private final GameRoomsHolder gameRoomsHolder;

  private final StartGameHandler startGameHandler;
  private final PlayerReadyHandler playerReadyHandler;
  private final PlayerChooseHandler playerChooseHandler;
  private final CreateGameRoomHandler createGameRoomHandler;
  private final ConnectToGameRoomHandler connectToGameRoomHandler;

  @MessageMapping("/api/gameRoom.create")
  public void createGameRoom(
      Principal principal,
      @Payload CreateGameRoomHandler.Request request
  ) {
    createGameRoomHandler.handle(principal, request);
  }

  @MessageMapping("/api/{gameRoomId}/gameRoom.connect")
  public void connectToGameRoom(
      Principal principal,
      MessageHeaders headers,
      @DestinationVariable UUID gameRoomId
  ) {
    final GameRoom gameRoom = gameRoomsHolder.getGameRoom(gameRoomId);
    final Credits credits = websocketUtils.parseCreditsFromHeaders(headers);

    connectToGameRoomHandler.handle(gameRoom, principal, credits);
  }

  @MessageMapping("/api/{gameRoomId}/player.ready")
  public void playerReady(
      MessageHeaders headers,
      @DestinationVariable UUID gameRoomId
  ) {
    final GameRoom gameRoom = gameRoomsHolder.getGameRoom(gameRoomId);
    final Credits credits = websocketUtils.parseCreditsFromHeaders(headers);
    final Player player = websocketUtils.authorize(gameRoom, credits);

    if (gameRoom.getGame().isStarted()) {
      throw new RuntimeException("Game started");
    }

    playerReadyHandler.handle(gameRoom, player);
  }

  @MessageMapping("/api/{gameRoomId}/game.start")
  public void startGame(
      MessageHeaders headers,
      @DestinationVariable UUID gameRoomId
  ) {
    final GameRoom gameRoom = gameRoomsHolder.getGameRoom(gameRoomId);
    final Credits credits = websocketUtils.parseCreditsFromHeaders(headers);
    websocketUtils.authorize(gameRoom, credits);

    if (gameRoom.getGame().isStarted()) {
      throw new RuntimeException("Game started");
    }

    if (gameRoom.getPlayers().size() < 2) {
      throw new RuntimeException("Not enough players");
    }

    if (gameRoom.getGame().getReadyPlayers().size() != gameRoom.getPlayers().size()) {
      throw new RuntimeException("not all players are ready");
    }

    startGameHandler.handle(gameRoom);
  }

  @MessageMapping("/api/{gameRoomId}/player.choose")
  public void playerChoose(
      MessageHeaders headers,
      @Payload PlayerChooseHandler.Request request,
      @DestinationVariable UUID gameRoomId
  ) {
    // auth
    final GameRoom gameRoom = gameRoomsHolder.getGameRoom(gameRoomId);
    final Credits credits = websocketUtils.parseCreditsFromHeaders(headers);
    final Player player = websocketUtils.authorize(gameRoom, credits);

    if (gameRoom.getGame().isFinished()) {
      throw new RuntimeException("Game finished");
    }

    playerChooseHandler.handle(gameRoom, player, request);
  }
}
