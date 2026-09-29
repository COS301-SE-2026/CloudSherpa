package com.cloudsherpa.service.agenticdashboard.dto;

public record AiToolResultDto(boolean success, String toolName, String result, String error) {

  public static AiToolResultDto success(String toolName, String result) {

    return new AiToolResultDto(true, toolName, result, null);
  }

  public static AiToolResultDto failure(String toolName, String error) {

    return new AiToolResultDto(false, toolName, null, error);
  }
}
