package com.cloudsherpa.service.intelligence.service;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import java.util.List;

public interface Sampler {
  public List<TimestampedNumericDataPoint> sample(List<TimestampedNumericDataPoint> original);
}
