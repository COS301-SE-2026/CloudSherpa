package com.cloudsherpa.service.intelligence.exceptions;

public class InfiniteSamplingLoop extends RuntimeException {
  public InfiniteSamplingLoop() {
    super("An Infinite Loop has occured during the sampling process");
  }
}
