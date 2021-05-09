package com.vladislav.jankenpon.utils;

import java.awt.Color;

public class Utils {

  public static String randomColor() {
    final Color color = new Color((int) (Math.random() * 0x1000000));
    String buf = Integer.toHexString(color.getRGB());
    return "#" + buf.substring(buf.length() - 6);
  }
}
