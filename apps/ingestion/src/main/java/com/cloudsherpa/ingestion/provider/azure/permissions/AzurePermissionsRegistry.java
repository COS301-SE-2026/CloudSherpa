package com.cloudsherpa.ingestion.provider.azure.permissions;

import com.cloudsherpa.ingestion.provider.azure.scanner.AzureResourceDiscoveryService;
import com.cloudsherpa.ingestion.provider.permissions.PermissionsRegistry;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AzurePermissionsRegistry extends PermissionsRegistry {

  public static final Set<String> COMMON_READ_ONLY = Set.of("Reader", "Monitoring Reader");

  private final Map<String, Set<String>> registry;

  public AzurePermissionsRegistry(AzureResourceDiscoveryService discoveryRegistryProvider) {
    super(COMMON_READ_ONLY);
    this.registry = discoveryRegistryProvider.getPermissionsRegistry();
  }

  public Map<String, Set<String>> getRegistry() {
    return registry;
  }
}
