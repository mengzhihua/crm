package com.mengzhihua.crm.common;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class CsvExportService {
    private CsvExportService() {
    }

    public static ResponseEntity<byte[]> download(
            String filename,
            List<String> headers,
            List<List<?>> rows
    ) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append(line(headers));
        for (List<?> row : rows) {
            csv.append(line(row));
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "text/csv;charset=UTF-8"
                ))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\""
                )
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String line(List<?> values) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                result.append(',');
            }
            String value = values.get(i) == null
                    ? ""
                    : String.valueOf(values.get(i));
            result.append('"').append(value.replace("\"", "\"\"")).append('"');
        }
        return result.append('\n').toString();
    }
}
