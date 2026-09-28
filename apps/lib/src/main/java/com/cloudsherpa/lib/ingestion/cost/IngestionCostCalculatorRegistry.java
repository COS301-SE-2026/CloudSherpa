package com.cloudsherpa.lib.ingestion.cost;

import com.cloudsherpa.lib.entities.ProviderEnum;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class IngestionCostCalculatorRegistry {

  private final Map<ProviderEnum, ProviderIngestionCostCalculator> calculators;

  public IngestionCostCalculatorRegistry(
      List<ProviderIngestionCostCalculator> calculators) {

    EnumMap<ProviderEnum, ProviderIngestionCostCalculator> map = new EnumMap<>(ProviderEnum.class);

    for (ProviderIngestionCostCalculator calculator : calculators) {

      map.put(
          calculator.getProvider(),
          calculator);
    }

    this.calculators = Map.copyOf(map);
  }

  public ProviderIngestionCostCalculator get(
      ProviderEnum provider) {

    ProviderIngestionCostCalculator calculator = calculators.get(provider);

    if (calculator == null) {
      throw new IllegalArgumentException(
          "No ingestion cost calculator registered for "
              + provider);
    }

    return calculator;
  }
}
