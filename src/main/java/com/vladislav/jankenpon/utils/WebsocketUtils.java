package com.vladislav.jankenpon.utils;

import com.vladislav.jankenpon.pojo.GameRoom;
import com.vladislav.jankenpon.pojo.Player;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

@Component
public class WebsocketUtils {

  public Player authorize(GameRoom gameRoom, Credits credits) {
    final Optional<Player> optional = gameRoom.findPlayer(credits.getUsername());
    if (optional.isEmpty()) {
      throw new RuntimeException("Failed auth");
    }
    final Player found = optional.get();
    if (!found.getPassword().equals(credits.getPassword())) {
      throw new RuntimeException("Failed auth");
    }
    return found;
  }

  @SuppressWarnings("unchecked cust")
  public Credits parseCreditsFromHeaders(MessageHeaders headers) {
    final Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) headers
        .get("nativeHeaders");
    final String encodedAuthorization = nativeHeaders.get("authorization").get(0);
    Objects.requireNonNull(encodedAuthorization, "empty authorization");

    final byte[] encodedAuthorizationBytes = encodedAuthorization.getBytes(StandardCharsets.UTF_8);

    final String authorization = new String(Base64Utils.decode(encodedAuthorizationBytes));

    final String[] split = authorization.split(":");

    return new Credits(split[0], split[1]);
  }
}
