package uk.gov.hmcts.reform.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "app.upload")
public class UploadValidationProperties {
    private DataSize maxFileSize;
    private int maxPdfPages;
    private long maxImagePixels;
    private int minImageDimension;
    private int maxImageDimension;
    private List<String> allowedTypes;
}
