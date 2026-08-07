package com.cloudsherpa.service.intelligence.service;

import com.cloudsherpa.lib.dtos.TimestampedNumericDataPoint;
import com.cloudsherpa.service.intelligence.exceptions.InfiniteSamplingLoop;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
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
public class UsageSampler implements Sampler {

  private final Logger logger = LoggerFactory.getLogger(UsageSampler.class);

  private Set<Long> candidates = new HashSet<>();

  public List<TimestampedNumericDataPoint> sample(List<TimestampedNumericDataPoint> original) {
    logger.info("Starting usage sample with {} original points", original.size());
    List<TimestampedNumericDataPoint> processing = new ArrayList<>(original);

    List<Long> differences = getDifferences(processing);
    List<Cluster> clusteredDifferences = clusterDifferences(differences);
    Long periodicity = choosePeriodicity(clusteredDifferences);

    logger.info(
        "Usage sample selected periodicity {} seconds from {} differences and {} clusters",
        periodicity,
        differences.size(),
        clusteredDifferences.size());

    padSeries(processing, periodicity);
    logger.info("Finished usage sample with {} processed points", processing.size());
    return processing;
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

  private void padSeries(List<TimestampedNumericDataPoint> original, long periodicity) {
    List<TimestampedNumericDataPoint> pointsToBeAdded = new ArrayList<>();
    for (int i = 0; i < original.size() - 1; i++) {

      Instant current = original.get(i).timestamp();
      Instant next = original.get(i + 1).timestamp();

      if (Duration.between(current, next).toSeconds() < periodicity) {
        // unrecoverable at this stage
        break;
      }

      int infiniteLoopDetectionThreshold = 8092;
      int loopIterations = 0;
      while (Duration.between(current, next).toSeconds() != periodicity
          && pointsToBeAdded.size() < 8092) {
        TimestampedNumericDataPoint addPoint =
            new TimestampedNumericDataPoint(
                BigDecimal.valueOf(0), current.plusSeconds(periodicity));
        pointsToBeAdded.addLast(addPoint);
        current = addPoint.timestamp();

        loopIterations++;
        if (loopIterations > infiniteLoopDetectionThreshold) {
          throw new InfiniteSamplingLoop();
        }
      }
    }

    original.addAll(pointsToBeAdded);
    original.sort(Comparator.comparing(TimestampedNumericDataPoint::timestamp));

    if (original.size() > 8092) {
      int sizeBeforeTrim = original.size();
      original.subList(8091, original.size()).clear();
      logger.info(
          "Padded {} missing points and trimmed series from {} to {} points",
          pointsToBeAdded.size(),
          sizeBeforeTrim,
          original.size());
    } else {
      logger.info(
          "Padded {} missing points. Series now has {} points",
          pointsToBeAdded.size(),
          original.size());
    }
  }
}
