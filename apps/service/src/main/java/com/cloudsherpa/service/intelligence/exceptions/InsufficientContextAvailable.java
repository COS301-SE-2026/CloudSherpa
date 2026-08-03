package com.cloudsherpa.service.intelligence.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InsufficientContextAvailable extends RuntimeException {
  public InsufficientContextAvailable(String message) {
    super("Insufficient historical data available to make forecasting prediction " + message);
  }
}
