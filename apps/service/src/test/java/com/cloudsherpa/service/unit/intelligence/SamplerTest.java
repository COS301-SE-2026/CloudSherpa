package com.cloudsherpa.service.unit.intelligence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import com.cloudsherpa.service.intelligence.dto.SanatizedSeries;
import com.cloudsherpa.service.intelligence.service.Sampler;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SamplerTest {

  @Autowired Sampler sampler;

  @BeforeEach
  void setUp() {
    this.sampler = new Sampler();
  }

  @Test
  void samplerShouldNotPadAValidSeries() {
    // Given an equally spaced time series and the pad with zero flag set, the sampler should not
    // "insert" any zeros into the timeseries

    // arrange
    List<TimestampedNumericDataPoint> validTimeSeries = getValidTimeSeries();

    // act
    SanatizedSeries sampledSeries =
        sampler.sample(validTimeSeries, true, Instant.ofEpochMilli(9_000));

    // assert
    assertEquals(validTimeSeries.size(), sampledSeries.timestampedNumericDataPoints().size());
  }

  @Test
  void samplerShouldPadCorrectly() {
    // arrange
    List<TimestampedNumericDataPoint> timeSeriesWithGaps = getTimeseriesWithGaps();

    List<TimestampedNumericDataPoint> expected =
        Arrays.asList(
            new TimestampedNumericDataPoint(BigDecimal.valueOf(1), Instant.ofEpochMilli(0)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(2), Instant.ofEpochMilli(1_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(0), Instant.ofEpochMilli(2_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(4), Instant.ofEpochMilli(3_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(5), Instant.ofEpochMilli(4_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(6), Instant.ofEpochMilli(5_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(0), Instant.ofEpochMilli(6_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(8), Instant.ofEpochMilli(7_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(9), Instant.ofEpochMilli(8_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(10), Instant.ofEpochMilli(9_000)));

    // act
    SanatizedSeries sampledSeries =
        sampler.sample(timeSeriesWithGaps, true, Instant.ofEpochMilli(9_000));

    // assert

    // 10 due to 2 expected paddings: 1 from the last datapoint to "now" and 2 for the intervals
    // that are 20 seconds
    // apart where the periodicity is 10s
    assertEquals(-1, sampledSeries.periodicity());
    assertEquals(10, sampledSeries.timestampedNumericDataPoints().size());
    assertEquals(expected, sampledSeries.timestampedNumericDataPoints());
  }

  @Test
  void samplerShouldShortCircuitInvalidTimeseries() {
    List<TimestampedNumericDataPoint> timeSeriesInvalidGaps = getInvalidTimeseries();

    List<TimestampedNumericDataPoint> expected =
        Arrays.asList(
            new TimestampedNumericDataPoint(BigDecimal.valueOf(8), Instant.ofEpochMilli(5_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(9), Instant.ofEpochMilli(7_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(10), Instant.ofEpochMilli(9_000)));

    SanatizedSeries actual =
        sampler.sample(timeSeriesInvalidGaps, true, Instant.ofEpochMilli(9_000));

    assertEquals(-2, actual.periodicity());
    assertEquals(expected.size(), actual.timestampedNumericDataPoints().size());
    assertEquals(expected, actual.timestampedNumericDataPoints());
  }

  @Test
  void samplerShouldShortCircuitInvalidTimeseriesNoPadding() {
    List<TimestampedNumericDataPoint> timeSeriesInvalidGaps = getInvalidTimeseries();

    List<TimestampedNumericDataPoint> expected =
        Arrays.asList(
            new TimestampedNumericDataPoint(BigDecimal.valueOf(8), Instant.ofEpochMilli(5_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(9), Instant.ofEpochMilli(7_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(10), Instant.ofEpochMilli(9_000)));

    SanatizedSeries actual = sampler.sample(timeSeriesInvalidGaps, false);

    assertEquals(-2, actual.periodicity());
    assertEquals(expected.size(), actual.timestampedNumericDataPoints().size());
    assertEquals(expected, actual.timestampedNumericDataPoints());
  }

  @Test
  void samplerShouldHandleDuplicateDataPoints() {
    // arrange
    // This TimestampedNumericDataPoint already exists in the series returned by getValidTimeSeries,
    // if this
    // tests fails/passes for a reason that is not obvious, ensure that this object still exists in
    // that list
    List<TimestampedNumericDataPoint> duplicateSeries = getValidTimeSeries();
    duplicateSeries.add(
        new TimestampedNumericDataPoint(BigDecimal.valueOf(6), Instant.ofEpochMilli(5_000)));

    List<TimestampedNumericDataPoint> expected = getValidTimeSeries();

    // act
    SanatizedSeries actual = sampler.sample(duplicateSeries, true, Instant.ofEpochMilli(9_000));

    // assert
    assertEquals(expected.size(), actual.timestampedNumericDataPoints().size());
    assertEquals(expected, actual.timestampedNumericDataPoints());
  }

  private List<TimestampedNumericDataPoint> getValidTimeSeries() {
    return new ArrayList<>(
        List.of(
            new TimestampedNumericDataPoint(BigDecimal.valueOf(1), Instant.ofEpochMilli(0)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(2), Instant.ofEpochMilli(1_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(3), Instant.ofEpochMilli(2_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(4), Instant.ofEpochMilli(3_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(5), Instant.ofEpochMilli(4_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(6), Instant.ofEpochMilli(5_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(7), Instant.ofEpochMilli(6_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(8), Instant.ofEpochMilli(7_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(9), Instant.ofEpochMilli(8_000)),
            new TimestampedNumericDataPoint(BigDecimal.valueOf(10), Instant.ofEpochMilli(9_000))));
  }

  private List<TimestampedNumericDataPoint> getTimeseriesWithGaps() {
    return Arrays.asList(
        new TimestampedNumericDataPoint(BigDecimal.valueOf(1), Instant.ofEpochMilli(0)),
        new TimestampedNumericDataPoint(BigDecimal.valueOf(2), Instant.ofEpochMilli(1_000)),
        new TimestampedNumericDataPoint(BigDecimal.valueOf(4), Instant.ofEpochMilli(3_000)),
        new TimestampedNumericDataPoint(BigDecimal.valueOf(5), Instant.ofEpochMilli(4_000)),
        new TimestampedNumericDataPoint(BigDecimal.valueOf(6), Instant.ofEpochMilli(5_000)),
        new TimestampedNumericDataPoint(BigDecimal.valueOf(8), Instant.ofEpochMilli(7_000)),
        new TimestampedNumericDataPoint(BigDecimal.valueOf(9), Instant.ofEpochMilli(8_000)),
        new TimestampedNumericDataPoint(BigDecimal.valueOf(10), Instant.ofEpochMilli(9_000)));
  }

  private List<TimestampedNumericDataPoint> getInvalidTimeseries() {
    return Arrays.asList(
        new TimestampedNumericDataPoint(BigDecimal.valueOf(1), Instant.ofEpochMilli(0)),
        new TimestampedNumericDataPoint(BigDecimal.valueOf(5), Instant.ofEpochMilli(2_000)),
        new TimestampedNumericDataPoint(BigDecimal.valueOf(6), Instant.ofEpochMilli(4_000)),
        new TimestampedNumericDataPoint(BigDecimal.valueOf(8), Instant.ofEpochMilli(5_000)),
        new TimestampedNumericDataPoint(BigDecimal.valueOf(9), Instant.ofEpochMilli(7_000)),
        new TimestampedNumericDataPoint(BigDecimal.valueOf(10), Instant.ofEpochMilli(9_000)));
  }
}
