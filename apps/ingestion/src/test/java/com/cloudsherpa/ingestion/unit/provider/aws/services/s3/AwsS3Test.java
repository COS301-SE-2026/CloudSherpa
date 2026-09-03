package com.cloudsherpa.ingestion.unit.provider.aws.services.s3;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cloudsherpa.ingestion.connector.CloudCredentials;
import com.cloudsherpa.ingestion.provider.aws.factory.AwsClientFactory;
import com.cloudsherpa.ingestion.provider.aws.services.s3.AwsS3;
import com.cloudsherpa.ingestion.provider.aws.services.s3.S3ObjectUriReference;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.*;

class AwsS3Test {
  private AwsS3 service;

  @BeforeEach
  void setUp() {
    service = new AwsS3();
  }

  @Test
  void uriHelper_shouldParseBucketAndKey() {
    S3Client s3 = mock(S3Client.class);
    software.amazon.awssdk.services.s3.S3Utilities utilities =
        mock(software.amazon.awssdk.services.s3.S3Utilities.class);
    when(s3.utilities()).thenReturn(utilities);
    S3Uri uri = mock(S3Uri.class);
    when(utilities.parseUri(URI.create("s3://bucket/path/file.json"))).thenReturn(uri);
    when(uri.bucket()).thenReturn(java.util.Optional.of("bucket"));
    when(uri.key()).thenReturn(java.util.Optional.of("path/file.json"));
    S3ObjectUriReference r = service.uriHelper(s3, "s3://bucket/path/file.json");
    assertEquals("bucket", r.bucketName());
    assertEquals("path/file.json", r.key());
  }

  @Test
  void uriHelper_whenBucketMissing_shouldThrow() {
    S3Client s3 = mock(S3Client.class);
    var u = mock(software.amazon.awssdk.services.s3.S3Utilities.class);
    when(s3.utilities()).thenReturn(u);
    var uri = mock(S3Uri.class);
    when(u.parseUri(any(URI.class))).thenReturn(uri);
    when(uri.bucket()).thenReturn(java.util.Optional.empty());
    assertThrows(IllegalArgumentException.class, () -> service.uriHelper(s3, "s3://bucket/key"));
  }

  @Test
  void uriHelper_whenKeyMissing_shouldThrow() {
    S3Client s3 = mock(S3Client.class);
    var u = mock(software.amazon.awssdk.services.s3.S3Utilities.class);
    when(s3.utilities()).thenReturn(u);
    var uri = mock(S3Uri.class);
    when(u.parseUri(any(URI.class))).thenReturn(uri);
    when(uri.bucket()).thenReturn(java.util.Optional.of("bucket"));
    when(uri.key()).thenReturn(java.util.Optional.empty());
    assertThrows(IllegalArgumentException.class, () -> service.uriHelper(s3, "s3://bucket/key"));
  }

  private static class ClientSetup {
    final CloudCredentials credentials = mock(CloudCredentials.class);
    final S3ClientBuilder builder = mock(S3ClientBuilder.class);
    final S3Client client = mock(S3Client.class);
    final StaticCredentialsProvider provider = mock(StaticCredentialsProvider.class);

    ClientSetup() {
      when(builder.region(Region.US_EAST_1)).thenReturn(builder);
      when(builder.credentialsProvider(provider)).thenReturn(builder);
      when(builder.build()).thenReturn(client);
    }
  }

  @Test
  void listObjects_shouldReturnResponseContents() {
    ClientSetup x = new ClientSetup();
    S3Object a = S3Object.builder().key("a").build();
    S3Object b = S3Object.builder().key("b").build();
    when(x.client.listObjectsV2(any(ListObjectsV2Request.class)))
        .thenReturn(ListObjectsV2Response.builder().contents(a, b).build());
    try (MockedStatic<S3Client> m = mockStatic(S3Client.class);
        MockedStatic<AwsClientFactory> f = mockStatic(AwsClientFactory.class)) {
      m.when(S3Client::builder).thenReturn(x.builder);
      f.when(() -> AwsClientFactory.credentialsProvider(x.credentials)).thenReturn(x.provider);
      List<S3Object> r = service.listObjects(x.credentials, Region.US_EAST_1, "bucket", "prefix");
      assertEquals(2, r.size());
      assertEquals("a", r.get(0).key());
      verify(x.client).listObjectsV2(any(ListObjectsV2Request.class));
      verify(x.client).close();
    }
  }

  @Test
  void listObjects_whenAwsClientFails_shouldPropagateException() {
    ClientSetup x = new ClientSetup();
    when(x.client.listObjectsV2(any(ListObjectsV2Request.class)))
        .thenThrow(new RuntimeException("AWS unavailable"));
    try (MockedStatic<S3Client> m = mockStatic(S3Client.class);
        MockedStatic<AwsClientFactory> f = mockStatic(AwsClientFactory.class)) {
      m.when(S3Client::builder).thenReturn(x.builder);
      f.when(() -> AwsClientFactory.credentialsProvider(x.credentials)).thenReturn(x.provider);
      assertThrows(
          RuntimeException.class,
          () -> service.listObjects(x.credentials, Region.US_EAST_1, "bucket", null));
    }
  }

  @Test
  void downloadObject_shouldRequestParsedBucketAndKey() {
    ClientSetup x = new ClientSetup();
    var utilities = mock(software.amazon.awssdk.services.s3.S3Utilities.class);
    var parsed = mock(S3Uri.class);
    when(x.client.utilities()).thenReturn(utilities);
    when(utilities.parseUri(any(URI.class))).thenReturn(parsed);
    when(parsed.bucket()).thenReturn(java.util.Optional.of("bucket"));
    when(parsed.key()).thenReturn(java.util.Optional.of("folder/file.json"));
    Path destination = Path.of("/tmp/file.json");
    try (MockedStatic<S3Client> m = mockStatic(S3Client.class);
        MockedStatic<AwsClientFactory> f = mockStatic(AwsClientFactory.class)) {
      m.when(S3Client::builder).thenReturn(x.builder);
      f.when(() -> AwsClientFactory.credentialsProvider(x.credentials)).thenReturn(x.provider);
      service.downloadObject(
          x.credentials, Region.US_EAST_1, "s3://bucket/folder/file.json", destination);
      verify(x.client).getObject(any(GetObjectRequest.class), eq(destination));
      verify(x.client).close();
    }
  }
}
