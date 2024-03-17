package io.github.sashirestela.cleverclient.support;

import java.util.List;
import java.util.stream.Collectors;

public class CleverClientSSE {

    private static final String EVENT_HEADER = "event: ";
    private static final String DATA_HEADER = "data: ";
    private static final String SEPARATOR = "";

    private static List<String> linesToCheck = null;

    private LineRecord record;
    private List<String> eventsToRead;
    private List<String> endsOfStream;

    public CleverClientSSE(LineRecord record) {
        this.record = record;
        this.eventsToRead = Configurator.one().getEventsToRead();
        this.endsOfStream = Configurator.one().getEndsOfStream();

        if (linesToCheck == null) {
            linesToCheck = this.eventsToRead.stream().filter(etr -> !etr.isEmpty()).map(etr -> (EVENT_HEADER + etr))
                    .collect(Collectors.toList());
            linesToCheck.add(SEPARATOR);
        }
    }

    public LineRecord getRecord() {
        return record;
    }

    public boolean isActualData() {
        return linesToCheck.contains(record.previous()) && record.current().startsWith(DATA_HEADER)
                && endsOfStream.stream().anyMatch(eos -> !record.current().contains(eos));
    }

    public String getActualData() {
        return record.current().replace(DATA_HEADER, "").strip();
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
