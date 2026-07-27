package com.cloudsherpa.ingestion.provider.util;

import jakarta.annotation.PreDestroy;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class DiscoveryExecutor {

  private static final int MAX_THREADS = 8;

  private final ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

  public <I, O> List<O> execute(Collection<I> workItems, Function<I, List<O>> worker) {

    List<CompletableFuture<List<O>>> futures =
        workItems.stream()
            .map(item -> CompletableFuture.supplyAsync(() -> worker.apply(item), executor))
            .toList();

    return futures.stream().map(CompletableFuture::join).flatMap(List::stream).toList();
  }

  @PreDestroy
  public void shutdown() {
    executor.shutdown();
  }
}
