package com.vladislav.jankenpon.configs;

import java.security.Principal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StompPrincipal implements Principal {

  String name;
}
