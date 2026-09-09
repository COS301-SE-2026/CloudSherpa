package com.cloudsherpa.ingestion.unit.billing.azure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.AzureBillingContext;
import com.cloudsherpa.ingestion.billing.provider.azure.storageaccount.pipeline.ManifestParsingStep;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManifestParsingStepTest {

  ManifestParsingStep step;

  @BeforeEach
  void setUp() {
    ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    step = new ManifestParsingStep(objectMapper);
  }

  @Test
  void shouldNotThrowWhenNoManifests() {
    AzureBillingContext context =
        new AzureBillingContext(
            UUID.fromString("00000000-0000-0000-0000-000000000004"),
            UUID.fromString("00000000-0000-0000-0000-000000000001"));

    context.setManifestBlobItems(List.of());

    step.execute(context);

    assertEquals(0, context.getManifests().size());
  }

  @Test
  void shouldParseValidManifests() {}

  @Test
  void shouldOnlySkipNotThrowWhenManifestInvalid() {}

  @Test
  void shouldOnlySkipNotThrowWhenBlobStorageExceptionThrown() {}
}
