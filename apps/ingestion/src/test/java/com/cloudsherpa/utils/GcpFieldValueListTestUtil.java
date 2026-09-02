package com.cloudsherpa.utils;

import static com.google.cloud.bigquery.FieldValue.Attribute.PRIMITIVE;
import static com.google.cloud.bigquery.FieldValue.Attribute.REPEATED;

import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.StandardSQLTypeName;
import java.util.List;

public final class GcpFieldValueListTestUtil {

  private GcpFieldValueListTestUtil() {}

  private static FieldValue primitive(String value) {
    return FieldValue.of(PRIMITIVE, value);
  }

  private static FieldValue nullValue() {
    return FieldValue.of(PRIMITIVE, null);
  }

  private static FieldValue numeric(String value) {
    return FieldValue.of(PRIMITIVE, value);
  }

  private static FieldValue timestamp(String epochSeconds) {
    return FieldValue.of(PRIMITIVE, epochSeconds);
  }

  public static FieldValueList validUsageRow() {
    return validUsageRow("1786442400.000000", "1786446000.000000");
  }

  public static FieldValueList validUsageRow(String timestampUsageStart, String timestampUsageEnd) {
    return FieldValueList.of(
        List.of(
            primitive("project-1"),
            primitive("service-1"),
            primitive("sku-1"),
            primitive("usage"),
            primitive("//compute.googleapis.com/projects/project-1/zones/us/vm-1"),
            primitive("//compute.googleapis.com/projects/project-1/zones/us/vm-1"),
            primitive("Compute Engine"),
            primitive("N1 Predefined Instance Core"),
            numeric("12.34"),
            timestamp(timestampUsageStart),
            timestamp(timestampUsageEnd),
            FieldValue.of(REPEATED, List.of())),
        Field.of("project_id", StandardSQLTypeName.STRING),
        Field.of("service_id", StandardSQLTypeName.STRING),
        Field.of("sku_id", StandardSQLTypeName.STRING),
        Field.of("cost_type", StandardSQLTypeName.STRING),
        Field.of("resource_name", StandardSQLTypeName.STRING),
        Field.of("resource_global_name", StandardSQLTypeName.STRING),
        Field.of("service_description", StandardSQLTypeName.STRING),
        Field.of("sku_description", StandardSQLTypeName.STRING),
        Field.of("cost", StandardSQLTypeName.NUMERIC),
        Field.of("usage_start_time", StandardSQLTypeName.TIMESTAMP),
        Field.of("usage_end_time", StandardSQLTypeName.TIMESTAMP),
        Field.newBuilder(
                "credits",
                StandardSQLTypeName.STRUCT,
                Field.of("amount", StandardSQLTypeName.NUMERIC))
            .setMode(Field.Mode.REPEATED)
            .build());
  }

  public static FieldValueList rowWithNullResourceName() {
    return rowWithNullResourceName("1786442400.000000", "1786446000.000000");
  }

  public static FieldValueList rowWithNullResourceName(
      String timestampUsageStart, String timestampUsageEnd) {
    return FieldValueList.of(
        List.of(
            primitive("project-1"),
            primitive("service-1"),
            primitive("sku-1"),
            primitive("usage"),
            nullValue(),
            primitive("//compute.googleapis.com/projects/project-1/zones/us/vm-1"),
            primitive("Compute Engine"),
            primitive("N1 Predefined Instance Core"),
            numeric("12.34"),
            timestamp(timestampUsageStart),
            timestamp(timestampUsageEnd),
            FieldValue.of(REPEATED, List.of())),
        Field.of("project_id", StandardSQLTypeName.STRING),
        Field.of("service_id", StandardSQLTypeName.STRING),
        Field.of("sku_id", StandardSQLTypeName.STRING),
        Field.of("cost_type", StandardSQLTypeName.STRING),
        Field.of("resource_name", StandardSQLTypeName.STRING),
        Field.of("resource_global_name", StandardSQLTypeName.STRING),
        Field.of("service_description", StandardSQLTypeName.STRING),
        Field.of("sku_description", StandardSQLTypeName.STRING),
        Field.of("cost", StandardSQLTypeName.NUMERIC),
        Field.of("usage_start_time", StandardSQLTypeName.TIMESTAMP),
        Field.of("usage_end_time", StandardSQLTypeName.TIMESTAMP),
        Field.newBuilder(
                "credits",
                StandardSQLTypeName.STRUCT,
                Field.of("amount", StandardSQLTypeName.NUMERIC))
            .setMode(Field.Mode.REPEATED)
            .build());
  }
}
