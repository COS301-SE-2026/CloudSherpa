package com.cloudsherpa.service.mock;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mock")
public class MockController {
  @GetMapping()
  public String getMethodName() {
    return "Service Success\n";
  }
}
