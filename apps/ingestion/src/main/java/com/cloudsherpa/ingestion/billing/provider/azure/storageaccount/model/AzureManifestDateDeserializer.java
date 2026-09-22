package com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

public class AzureManifestDateDeserializer extends StdDeserializer<Instant> {
  public AzureManifestDateDeserializer() {
    this(null);
  }

  public AzureManifestDateDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String value = p.getText();

    try {
      return OffsetDateTime.parse(value).toInstant();
    } catch (DateTimeParseException e) {
      return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
    }
  }
}
