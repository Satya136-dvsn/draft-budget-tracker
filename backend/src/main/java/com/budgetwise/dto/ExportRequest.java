package com.budgetwise.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ExportRequest {
    private String format;
    private String timeRange;
    private Map<String, String> images; // Key: Chart ID/Name, Value: Base64 Image String
}
