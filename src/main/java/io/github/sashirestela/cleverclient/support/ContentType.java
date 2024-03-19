package io.github.sashirestela.cleverclient.support;

import io.github.sashirestela.cleverclient.util.Constant;

public enum ContentType {

    MULTIPART_FORMDATA(
            "multipart/form-data",
            "; boundary=\"" + Constant.BOUNDARY_VALUE + "\""),
    APPLICATION_JSON(
            "application/json",
            "");

    private String mimeType;
    private String details;

    ContentType(String mimeType, String details) {
        this.mimeType = mimeType;
        this.details = details;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public String getDetails() {
        return this.details;
    }

}
