package com.cloudsherpa.service.intelligence.service;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Sampler {

  private final Logger logger = LoggerFactory.getLogger(Sampler.class);

  private Set<Long> candidates = new HashSet<>();
  private boolean padWithZeros;

  public SanatizedSeries sample(List<TimestampedNumericDataPoint> original, boolean padWithZeros) {
    logger.info("Starting sample with {} original points", original.size());
    this.padWithZeros = padWithZeros;

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

    // Sorting results in negative periodicity, but necessary for the correct cutoff point
    processing.sort(Comparator.comparing(TimestampedNumericDataPoint::timestamp).reversed());

    List<Long> differences = getDifferences(processing);
    List<Cluster> clusteredDifferences = clusterDifferences(differences);
    Long periodicity = choosePeriodicity(clusteredDifferences);

    if (padWithZeros) {

      Instant now = Instant.now();

      Duration durationBetweenTimeOfRequestAndLastIngestedDataPoint =
          Duration.between(processing.getFirst().timestamp(), now);
      // cause pad with zero side effect if last ingested data point >= than 1 hour
      if (durationBetweenTimeOfRequestAndLastIngestedDataPoint.toHours() >= 1) {
        long secondsToAdd =
            durationBetweenTimeOfRequestAndLastIngestedDataPoint.toSeconds() / periodicity;
        Instant newInstant =
            processing.getFirst().timestamp().plusSeconds(secondsToAdd * periodicity);
        TimestampedNumericDataPoint newDataPoint =
            new TimestampedNumericDataPoint(BigDecimal.valueOf(0), newInstant);
        processing.addFirst(newDataPoint);
      }
    }

    logger.info(
        "Sample selected periodicity {} seconds from {} differences and {} clusters",
        periodicity,
        differences.size(),
        clusteredDifferences.size());

    List<TimestampedNumericDataPoint> sanatizedSeries = santizeSeries(processing, periodicity);
    logger.info("Finished sample with {} processed points", processing.size());
    return new SanatizedSeries(sanatizedSeries, periodicity);
  }

  private List<Long> getDifferences(List<TimestampedNumericDataPoint> original) {
    List<Long> differences = new ArrayList<>();
    for (int i = 0; i < original.size() - 1; i++) {
      Long difference =
          Duration.between(original.get(i).timestamp(), original.get(i + 1).timestamp())
              .toSeconds();
      differences.add(difference);
      candidates.add(difference);
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
      List<TimestampedNumericDataPoint> original, long periodicity) {
    List<TimestampedNumericDataPoint> sanitizedSeries = new ArrayList<>();
    boolean brokeEarly = false;
    for (int i = 0; i < original.size() - 1; i++) {

      // Safe to add current since difference between current and previous checked in previous
      // iteration
      sanitizedSeries.add(
          new TimestampedNumericDataPoint(original.get(i).value(), original.get(i).timestamp()));

      Instant current = original.get(i).timestamp();
      Instant next = original.get(i + 1).timestamp();

      long durationBetweenCurrentAndNext = Duration.between(current, next).toSeconds();

      if (durationBetweenCurrentAndNext > periodicity
          || durationBetweenCurrentAndNext % periodicity != 0
          || (durationBetweenCurrentAndNext != periodicity && !padWithZeros)) {
        // periodicity at which data published changed, unrecoverable at this stage, going to work
        // with what was obtained up until this point
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
      }
    }

    if (!brokeEarly) {
      sanitizedSeries.add(
          new TimestampedNumericDataPoint(
              original.getLast().value(), original.getLast().timestamp()));
    }

    sanitizedSeries.sort(Comparator.comparing(TimestampedNumericDataPoint::timestamp));

    if (sanitizedSeries.size() > 8092) {
      int sizeBeforeTrim = original.size();
      sanitizedSeries.subList(8091, sanitizedSeries.size()).clear();
      logger.info(
          "Padded {} missing points and trimmed series from {} to {} points",
          sanitizedSeries.size(),
          sizeBeforeTrim,
          sanitizedSeries.size());
    } else {
      logger.info(
          "Padded {} missing points. Series now has {} points",
          sanitizedSeries.size(),
          sanitizedSeries.size());
    }

    return sanitizedSeries;
  }
}
