package com.site.webapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "app.attachment")
public class AttachmentProperties {
    private long maxSizeBytes = 10485760;
    private Map<String, String> allowedTypes = new HashMap<>();
    private Set<String> forbiddenExtensions = new HashSet<>();

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public void setMaxSizeBytes(long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }

    public Map<String, String> getAllowedTypes() {
        return allowedTypes;
    }

    public void setAllowedTypes(Map<String, String> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    public Set<String> getForbiddenExtensions() {
        return forbiddenExtensions;
    }

    public void setForbiddenExtensions(Set<String> forbiddenExtensions) {
        this.forbiddenExtensions = forbiddenExtensions;
    }
}
