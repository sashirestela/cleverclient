package io.github.sashirestela.cleverclient.support;

import io.github.sashirestela.cleverclient.util.Constant;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class HttpMultipart {

    private static final String DASH = "--";
    private static final String DQ = "\"";
    private static final String NL = "\r\n";
    private static final String DISPOSITION = "Content-Disposition: form-data";
    private static final String FIELD_NAME = "; name=";
    private static final String FILE_NAME = "; filename=";
    private static final String CONTENT_TYPE = "Content-Type: ";

    private HttpMultipart() {
    }

    public static List<byte[]> toByteArrays(Map<String, Object> data) {
        List<byte[]> byteArrays = new ArrayList<>();
        for (var entry : data.entrySet()) {
            byteArrays.add(toBytes(DASH + Constant.BOUNDARY_VALUE + NL));
            byteArrays.add(toBytes(DISPOSITION));
            var fieldName = entry.getKey();
            if (isFile(entry.getValue())) {
                String fileName = null;
                String mimeType = null;
                byte[] fileContent = null;
                try {
                    URI uri = new URI(entry.getValue().toString());
                    var path = Paths.get(uri);
                    fileName = path.toString();
                    mimeType = Files.probeContentType(path);
                    fileContent = Files.readAllBytes(path);
                } catch (IOException | URISyntaxException e) {
                    throw new CleverClientException("Error trying to read the file {0}.", fileName, e);
                }
                byteArrays.add(toBytes(FIELD_NAME + DQ + fieldName + DQ + FILE_NAME + DQ + fileName + DQ + NL));
                byteArrays.add(toBytes(CONTENT_TYPE + mimeType + NL));
                byteArrays.add(toBytes(NL));
                byteArrays.add(fileContent);
                byteArrays.add(toBytes(NL));
            } else {
                var fieldValue = entry.getValue();
                var isFirst = true;
                if (fieldValue instanceof Collection) {
                    for (Object item : (Collection<?>) fieldValue) {
                        addIndividualField(byteArrays, fieldName + "[]", item, isFirst);
                        isFirst = false;
                    }
                } else {
                    addIndividualField(byteArrays, fieldName, fieldValue, isFirst);
                }
            }
        }
        byteArrays.add(toBytes(DASH + Constant.BOUNDARY_VALUE + DASH + NL));
        return byteArrays;
    }

    private static void addIndividualField(List<byte[]> byteArrays, String fieldName, Object fieldValue,
            boolean isFirst) {
        if (!isFirst) {
            byteArrays.add(toBytes(DASH + Constant.BOUNDARY_VALUE + NL));
            byteArrays.add(toBytes(DISPOSITION));
        }
        byteArrays.add(toBytes(FIELD_NAME + DQ + fieldName + DQ + NL));
        byteArrays.add(toBytes(NL));
        byteArrays.add(toBytes(fieldValue + NL));
    }

    private static byte[] toBytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    private static boolean isFile(Object value) {
        final String FILE_PROTOCOL = "file:";
        return value instanceof String
                && ((String) value).startsWith(FILE_PROTOCOL);
    }

}
