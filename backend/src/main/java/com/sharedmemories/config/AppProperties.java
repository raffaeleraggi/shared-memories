package com.sharedmemories.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String publicBaseUrl;
    private Storage storage = new Storage();
    @Data public static class Storage {
        private String mode;
        private String localBaseUrl;
        private String localPath;
        private R2 r2 = new R2();
    }
    @Data public static class R2 {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucket;
        private String publicBaseUrl;
    }
}
