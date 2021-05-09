package com.vladislav.jankenpon.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Player {

  private String username;

  @JsonProperty(access = Access.WRITE_ONLY)
  private String password;
  private String color;

}
