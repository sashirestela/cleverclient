package io.github.sashirestela.cleverclient.support;

import java.util.List;
import java.util.Set;

public class CleverClientSSE {

    public static final String EVENT_HEADER = "event: ";
    private static final String DATA_HEADER = "data: ";
    private static final String SEPARATOR = "";

    private LineRecord lineRecord;
    private List<String> endsOfStream;
    private Set<String> eventsWithHeader;

    public CleverClientSSE(LineRecord lineRecord) {
        this.lineRecord = lineRecord;
        this.endsOfStream = Configurator.one().getEndsOfStream();
        this.eventsWithHeader = Set.of(SEPARATOR);
    }

    public CleverClientSSE(LineRecord lineRecord, Set<String> eventsWithHeader) {
        this.lineRecord = lineRecord;
        this.endsOfStream = Configurator.one().getEndsOfStream();
        this.eventsWithHeader = eventsWithHeader;
    }

    public boolean isActualData() {
        return eventsWithHeader.contains(lineRecord.previous()) && lineRecord.current().startsWith(DATA_HEADER)
                && endsOfStream.stream().anyMatch(eos -> !lineRecord.current().contains(eos));
    }

    public String getActualData() {
        return lineRecord.current().replace(DATA_HEADER, "").strip();
    }

    public String getMatchedEvent() {
        return eventsWithHeader.contains(lineRecord.previous()) ? lineRecord.previous() : null;
    }

    public static class LineRecord {

        private String currentLine;
        private String previousLine;

        public LineRecord(String previousLine, String currentLine) {
            this.previousLine = previousLine;
            this.currentLine = currentLine;
        }

        public LineRecord() {
            this("", "");
        }

        public void updateWith(String line) {
            this.previousLine = this.currentLine;
            this.currentLine = line;
        }

        public String current() {
            return this.currentLine;
        }

        public String previous() {
            return this.previousLine;
        }

    }

}
