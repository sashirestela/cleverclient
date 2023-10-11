package io.github.sashirestela.cleverclient.support;

public class CleverClientSSE {

  private static final String DATA_HEADER = "data: ";

  private String rawData;

  public CleverClientSSE(String rawData) {
    this.rawData = rawData;
  }

  public String getRawData() {
    return rawData;
  }

  public boolean isActualData() {
    return rawData.startsWith(DATA_HEADER);
  }

  public String getActualData() {
    return rawData.replace(DATA_HEADER, "").strip();
  }

}