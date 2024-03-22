package io.github.sashirestela.cleverclient.support;

import java.util.List;
import java.util.Set;

public class CleverClientSSE {

    private static final String EVENT_HEADER = "event: ";
    private static final String DATA_HEADER = "data: ";
    private static final String SEPARATOR = "";

    private LineRecord lineRecord;
    private List<String> endsOfStream;
    private Set<String> events;

    public CleverClientSSE(LineRecord lineRecord) {
        this.lineRecord = lineRecord;
        this.endsOfStream = Configurator.one().getEndsOfStream();
        this.events = Set.of(SEPARATOR);
    }

    public CleverClientSSE(LineRecord lineRecord, Set<String> events) {
        this.lineRecord = lineRecord;
        this.endsOfStream = Configurator.one().getEndsOfStream();
        this.events = events;
    }

    public boolean isActualData() {
        return isMatchedEvent() && lineRecord.current().startsWith(DATA_HEADER)
                && endsOfStream.stream().anyMatch(eos -> !lineRecord.current().contains(eos));
    }

    public String getActualData() {
        return lineRecord.current().replace(DATA_HEADER, "").strip();
    }

    private boolean isMatchedEvent() {
        return events.stream()
                .anyMatch(ev -> lineRecord.previous().equals((ev.equals(SEPARATOR) ? SEPARATOR : EVENT_HEADER + ev)));
    }

    public String getMatchedEvent() {
        return isMatchedEvent() ? lineRecord.previous().replace(EVENT_HEADER, "").strip() : null;
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
