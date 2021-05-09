package com.vladislav.jankenpon.components;

import com.vladislav.jankenpon.pojo.Player;
import com.vladislav.jankenpon.utils.Credits;
import com.vladislav.jankenpon.utils.Utils;
import org.springframework.stereotype.Component;

@Component
public class PlayerFactory {

  public Player create(Credits credits) {
    return new Player()
        .setUsername(credits.getUsername())
        .setPassword(credits.getPassword())
        .setColor(Utils.randomColor());
  }
}
