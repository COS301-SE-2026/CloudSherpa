package com.cloudsherpa.ingestion.provider.permissions;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public abstract class PermissionsRegistry {

  protected final Set<String> commonReadOnly;

  protected PermissionsRegistry(Set<String> commonReadOnly) {
    this.commonReadOnly = commonReadOnly;
  }

  public abstract Map<String, Set<String>> getRegistry();

  public static Map<String, Set<String>> mergePermissionMaps(
      Map<String, Set<String>> first, Map<String, Set<String>> second) {

    Map<String, Set<String>> result = new HashMap<>(first);

    second.forEach(
        (service, permissions) ->
            result.merge(
                service.toLowerCase(Locale.ROOT),
                new HashSet<>(permissions),
                (existing, incoming) -> {
                  existing.addAll(incoming);
                  return existing;
                }));

    return result;
  }

  public Set<String> getPermissions(String service) {
    return getRegistry().getOrDefault(service, Collections.emptySet());
  }

  public Set<String> getPermissions(Set<String> services) {
    Set<String> permissions = new HashSet<>(commonReadOnly);

    for (String service : services) {
      permissions.addAll(getPermissions(service));
    }

    return Set.copyOf(permissions);
  }
}
