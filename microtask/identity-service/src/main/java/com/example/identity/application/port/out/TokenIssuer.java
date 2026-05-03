package com.example.identity.application.port.out;

import java.util.UUID;

public interface TokenIssuer {

  String issue(UUID userId);
}
