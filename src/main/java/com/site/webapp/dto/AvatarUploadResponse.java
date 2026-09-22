package com.site.webapp.dto;

public class AvatarUploadResponse {
    private String s3Key;
    private String url;

    public AvatarUploadResponse() {
    }

    public AvatarUploadResponse(String s3Key, String url) {
        this.s3Key = s3Key;
        this.url = url;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
