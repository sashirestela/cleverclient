package io.github.sashirestela.cleverclient.support;

import java.util.List;

public class CleverClientSSE {

    private static final String DATA_HEADER = "data: ";

    private LineRecord lineRecord;
    private List<String> endsOfStream;
    private List<String> linesToCheck;

    public CleverClientSSE(LineRecord lineRecord) {
        this.lineRecord = lineRecord;
        this.linesToCheck = Configurator.one().getLinesToCheck();
        this.endsOfStream = Configurator.one().getEndsOfStream();
    }

    public LineRecord getLineRecord() {
        return lineRecord;
    }

    public boolean isActualData() {
        return linesToCheck.contains(lineRecord.previous()) && lineRecord.current().startsWith(DATA_HEADER)
                && endsOfStream.stream().anyMatch(eos -> !lineRecord.current().contains(eos));
    }

    public String getActualData() {
        return lineRecord.current().replace(DATA_HEADER, "").strip();
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
