package com.site.webapp.dto;

public class FileAttachmentResponse {
    private String s3Key;
    private String url;
    private String originalFileName;
    private Long size;
    private String contentType;

    public FileAttachmentResponse(){
    }
    public FileAttachmentResponse(String s3Key,String url,String originalFileName,Long size,String contentType){
        this.s3Key = s3Key;
        this.url = url;
        this.originalFileName = originalFileName;
        this.size = size;
        this.contentType = contentType;
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

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
