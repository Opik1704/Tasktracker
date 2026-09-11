package com.site.webapp.service;

import com.site.webapp.exception.FileStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public FileStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file, String folder) {
        String s3Key = folder + "/" + UUID.randomUUID() + "." + StringUtils.getFilenameExtension(file.getOriginalFilename());
        log.debug("Starting upload file '{}' to S3 bucket '{}' with key '{}'",file.getOriginalFilename(), bucketName, s3Key);
        try{
            PutObjectRequest request= PutObjectRequest.builder().bucket(bucketName).key(s3Key).contentType(file.getContentType()).build();
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("Successfully uploaded file '{}' to S3 with key '{}'", file.getOriginalFilename(), s3Key);
        } catch (Exception e) {
            log.error("Error uploading file '{}' with key '{}' to S3", file.getOriginalFilename(), s3Key, e);
            throw new FileStorageException("Failed to upload file to S3", e);
        }

        return s3Key;
    }
    public InputStream downloadFile(String s3Key){
        log.debug("Downloading file with key '{}' from S3 bucket '{}'", s3Key, bucketName);
        try {
            GetObjectRequest request = GetObjectRequest.builder().bucket(bucketName).key(s3Key).build();
            log.info("Successfully initiated download for S3 key '{}'", s3Key);
            return s3Client.getObject(request);
        }catch (Exception e){
            log.error("Error downloading file with key '{}' from S3", s3Key, e);
            throw new FileStorageException("Failed to download file from S3", e);
        }

    }
    public void deleteFile(String s3Key){
        try{
            DeleteObjectRequest request = DeleteObjectRequest.builder().bucket(bucketName).key(s3Key).build();
            s3Client.deleteObject(request);
            log.info("Successfully deleted file with key '{}' from S3", s3Key);
        }
        catch (Exception e){
            log.error("Error deleting file with key '{}' from S3", s3Key, e);
            throw new FileStorageException("Failed to delete file from S3", e);
        }
    }
}
