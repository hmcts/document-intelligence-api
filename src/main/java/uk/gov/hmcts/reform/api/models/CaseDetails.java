package uk.gov.hmcts.reform.api.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Probate case details")
public class CaseDetails {

    @Schema(
        description = "Case reference or number",
        example = "ABC123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Size(min = 1, max = 200)
    private String caseNumber;

    public CaseDetails(String caseNumber) {
        this.caseNumber = caseNumber;
    }
}
