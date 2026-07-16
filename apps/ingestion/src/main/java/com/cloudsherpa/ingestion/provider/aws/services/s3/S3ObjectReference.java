package com.cloudsherpa.ingestion.provider.aws.services.s3;

import software.amazon.awssdk.services.s3.model.S3Object;

public record S3ObjectReference(String bucketName, S3Object object) {}
