package uk.gov.hmcts.reform.api.config;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MI or WL identity should be used in production, this should be for local only.
 */
@Configuration
public class AzureDocumentIntelligenceConfiguration {

    @Value("${azure.document-intelligence.key}")
    private String key;

    @Value("${azure.document-intelligence.endpoint}")
    private String endpoint;

    @Bean
    public DocumentIntelligenceClient documentIntelligenceClient() {
        return new DocumentIntelligenceClientBuilder()
            .credential(new AzureKeyCredential(key))
            .endpoint(endpoint)
            .buildClient();
    }
}
