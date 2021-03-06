package com.vladislav.jankenpon.utils;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Pair<T1, T2> {

  private T1 first;
  private T2 second;

  public static <T1, T2> Pair<T1, T2> of(T1 first, T2 second) {
    return new Pair<>(first, second);
  }

  public static <T1, T2> Pair<T1, T2> from(Map.Entry<T1, T2> entry) {
    return new Pair<>(entry.getKey(), entry.getValue());
  }
}
