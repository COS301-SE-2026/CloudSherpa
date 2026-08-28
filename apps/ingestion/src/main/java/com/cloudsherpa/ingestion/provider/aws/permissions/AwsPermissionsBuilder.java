package com.cloudsherpa.ingestion.provider.aws.permissions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class AwsPermissionsBuilder {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final AwsPermissionsRegistry awsPermissionsRegistry;

  public AwsPermissionsBuilder(AwsPermissionsRegistry awsPermissionsRegistry) {
    this.awsPermissionsRegistry = awsPermissionsRegistry;
  }

  public String buildPolicy(Collection<String> services) {

    Set<String> permissions = new TreeSet<>();

    permissions.addAll(AwsPermissionsRegistry.COMMON_READ_ONLY);

    for (String service : services) {
      permissions.addAll(awsPermissionsRegistry.getPermissions(service));
    }

    Map<String, Object> statement = new LinkedHashMap<>();
    statement.put("Effect", "Allow");
    statement.put("Action", permissions);
    statement.put("Resource", "*");

    Map<String, Object> policy = new LinkedHashMap<>();
    policy.put("Version", "2012-10-17");
    policy.put("Statement", List.of(statement));

    try {
      return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(policy);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to generate IAM policy", e);
    }
  }
}
