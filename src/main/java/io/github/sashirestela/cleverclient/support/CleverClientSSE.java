package io.github.sashirestela.cleverclient.support;

public class CleverClientSSE {

    private static final String DATA_HEADER = "data: ";

    private static String endOfStream = null;

    private String rawData;

    public CleverClientSSE(String rawData) {
        this.rawData = rawData;
    }

    public String getRawData() {
        return rawData;
    }

    public boolean isActualData() {
        return rawData.startsWith(DATA_HEADER) && (endOfStream == null || !rawData.contains(endOfStream));
    }

    public String getActualData() {
        return rawData.replace(DATA_HEADER, "").strip();
    }

    public static void setEndOfStream(String endOfStream) {
        CleverClientSSE.endOfStream = endOfStream;
    }

}