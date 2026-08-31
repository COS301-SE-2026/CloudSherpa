package com.cloudsherpa.service.intelligence.service;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import com.cloudsherpa.service.intelligence.dto.SanatizedSeries;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Sampler {

  private final Logger logger = LoggerFactory.getLogger(Sampler.class);

  private record CandidateDifferenceProperties(
      boolean duplicateTimestamp, boolean invalidatesSeries) {}

  public SanatizedSeries sample(List<TimestampedNumericDataPoint> original, boolean padWithZeros) {
    return sample(original, padWithZeros, Instant.now());
  }

  public SanatizedSeries sample(
      List<TimestampedNumericDataPoint> original, boolean padWithZeros, Instant padToInstant) {
    logger.info("Starting sample with {} original points", original.size());

    if (original.size() < 2) {
      return new SanatizedSeries(List.of(), 0);
    }

    List<TimestampedNumericDataPoint> processing = new ArrayList<>();

    for (TimestampedNumericDataPoint timestampedNumericDataPoint : original) {
      processing.add(
          new TimestampedNumericDataPoint(
              timestampedNumericDataPoint.value(),
              timestampedNumericDataPoint.timestamp().truncatedTo(ChronoUnit.SECONDS)));
    }

    processing.sort(Comparator.comparing(TimestampedNumericDataPoint::timestamp).reversed());

    List<Long> differences = getDifferences(processing);
    List<Cluster> clusteredDifferences = clusterDifferences(differences);
    Long periodicity = choosePeriodicity(clusteredDifferences);

    if (padWithZeros) {

      Duration durationBetweenTimeOfRequestAndLastIngestedDataPoint =
          Duration.between(processing.getFirst().timestamp(), padToInstant);
      // cause pad with zero side effect if last ingested data point >= than 1 hour
      if (durationBetweenTimeOfRequestAndLastIngestedDataPoint.toHours() >= 1) {
        long paddingPeriodicity = Math.abs(periodicity);
        long secondsToAdd =
            durationBetweenTimeOfRequestAndLastIngestedDataPoint.toSeconds() / paddingPeriodicity;
        Instant newInstant =
            processing.getFirst().timestamp().plusSeconds(secondsToAdd * paddingPeriodicity);
        if (newInstant.isAfter(processing.getFirst().timestamp())) {
          TimestampedNumericDataPoint newDataPoint =
              new TimestampedNumericDataPoint(BigDecimal.valueOf(0), newInstant);
          processing.addFirst(newDataPoint);
        }

        logger.info(
            "Add a recent timestamp to a series with the most recent timestamp greater than one hour after {}",
            padToInstant);
      }
    }

    List<TimestampedNumericDataPoint> sanatizedSeries =
        santizeSeries(processing, periodicity, padWithZeros);
    return new SanatizedSeries(sanatizedSeries, periodicity);
  }

  private List<Long> getDifferences(List<TimestampedNumericDataPoint> original) {
    List<Long> differences = new ArrayList<>();
    for (int i = 0; i < original.size() - 1; i++) {
      Long difference =
          Duration.between(original.get(i).timestamp(), original.get(i + 1).timestamp())
              .toSeconds();
      differences.add(difference);
    }
    return differences;
  }

  private List<Cluster> clusterDifferences(List<Long> differences) {
    // Sort differences
    Collections.sort(differences);

    // Assign each difference to a cluster
    Cluster initalCluster = new Cluster();
    initalCluster.addToCluster(differences.get(0));

    List<Cluster> clusters = new ArrayList<>();
    clusters.add(initalCluster);

    Cluster currentCluster = initalCluster;

    for (Long difference : differences.subList(1, differences.size())) {
      long currentMedian = currentCluster.getClusterMedian();
      long medianDistance = Math.abs(difference - currentMedian);
      if (medianDistance == 0) {
        currentCluster.addToCluster(difference);
      } else {
        currentCluster = new Cluster();
        currentCluster.addToCluster(difference);
        clusters.add(currentCluster);
      }
    }

    return clusters;
  }

  private long choosePeriodicity(List<Cluster> clusters) {
    Cluster candidateCluster = clusters.get(0);

    for (Cluster currentCluster : clusters.subList(1, clusters.size())) {
      if (currentCluster.getClusterSize() > candidateCluster.getClusterSize()) {
        candidateCluster = currentCluster;
      }
    }

    return candidateCluster.getClusterMedian();
  }

  private List<TimestampedNumericDataPoint> santizeSeries(
      List<TimestampedNumericDataPoint> original, long periodicity, boolean padWithZeros) {
    List<TimestampedNumericDataPoint> sanitizedSeries = new ArrayList<>();
    boolean brokeEarly = false;
    int numberOfPads = 0;
    for (int i = 0; i < original.size() - 1; i++) {

      Instant current = original.get(i).timestamp();
      Instant next = original.get(i + 1).timestamp();

      CandidateDifferenceProperties differenceProperties =
          calculateCandidateDifferenceProperties(current, next, periodicity, padWithZeros);

      if (!differenceProperties.duplicateTimestamp) {
        sanitizedSeries.add(
            new TimestampedNumericDataPoint(original.get(i).value(), original.get(i).timestamp()));

        if (differenceProperties.invalidatesSeries) {
          brokeEarly = true;
          break;
        }

        while (Duration.between(current, next).toSeconds() != periodicity
            && sanitizedSeries.size() < 8092) {
          TimestampedNumericDataPoint addPoint =
              new TimestampedNumericDataPoint(
                  BigDecimal.valueOf(0), current.plusSeconds(periodicity));
          sanitizedSeries.addLast(addPoint);
          current = addPoint.timestamp();
          numberOfPads++;
        }
      }
    }

    if (!brokeEarly) {
      sanitizedSeries.add(
          new TimestampedNumericDataPoint(
              original.getLast().value(), original.getLast().timestamp()));
    }

    sanitizedSeries.sort(Comparator.comparing(TimestampedNumericDataPoint::timestamp));

    if (numberOfPads > 0) {
      logger.info(
          "Padded {} regular gaps.\nOriginal size: {}\nSize after padding {}",
          numberOfPads,
          original.size(),
          sanitizedSeries.size());
    }

    if (sanitizedSeries.size() > 8092) {
      int sizeBeforeTrim = original.size();
      sanitizedSeries.subList(8091, sanitizedSeries.size()).clear();
      logger.info("Trimmed series from {} to {} points", sizeBeforeTrim, sanitizedSeries.size());
    }

    return sanitizedSeries;
  }

  private CandidateDifferenceProperties calculateCandidateDifferenceProperties(
      Instant current, Instant next, long periodicity, boolean padWithZeros) {
    long durationBetweenCurrentAndNext = Duration.between(current, next).toSeconds();

    if (current.compareTo(next) == 0) {
      return new CandidateDifferenceProperties(true, false);
    }

    if (next.isAfter(current)
        || durationBetweenCurrentAndNext % periodicity != 0
        || (durationBetweenCurrentAndNext != periodicity && !padWithZeros)) {
      return new CandidateDifferenceProperties(false, true);
    }

    return new CandidateDifferenceProperties(false, false);
  }
}
