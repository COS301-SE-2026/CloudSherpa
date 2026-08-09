package com.cloudsherpa.service.intelligence.registry;

import com.cloudsherpa.lib.entities.ProviderEnum;
import com.cloudsherpa.lib.repositories.NormalizedCostsRepository;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class ChargeProviderRegistry {
  private final ConcurrentMap<String, ProviderEnum> chargeIdProviderMap = new ConcurrentHashMap<>();
  private final NormalizedCostsRepository normalizedCostsRepository;

  public ChargeProviderRegistry(NormalizedCostsRepository normalizedCostsRepository) {
    this.normalizedCostsRepository = normalizedCostsRepository;
  }

  public ProviderEnum getChargeProvider(String chargeId) {
    if (chargeIdProviderMap.get(chargeId) != null) {
      return chargeIdProviderMap.get(chargeId);
    }

    ProviderEnum chargeProvider = normalizedCostsRepository.getChargeProvider(chargeId);
    if (chargeProvider != null) {
      chargeIdProviderMap.put(chargeId, chargeProvider);
      return chargeProvider;
    } else {
      return null;
    }
  }
}
