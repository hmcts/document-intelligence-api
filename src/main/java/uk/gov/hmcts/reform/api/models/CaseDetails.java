package uk.gov.hmcts.reform.api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Case details associated with the uploaded document")
public class CaseDetails {

    @Schema(description = "Case reference number", example = "12345-2024", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String caseNumber;

    public CaseDetails(String caseNumber) {
        this.caseNumber = caseNumber;
    }
}
