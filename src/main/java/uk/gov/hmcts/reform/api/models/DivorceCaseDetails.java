package uk.gov.hmcts.reform.api.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Schema(description = "Divorce case details for document submission")
public class DivorceCaseDetails {

    @Schema(
        description = "Full name as on marriage certificate",
        example = "Jane Mary Doe",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Size(min = 2, max = 200)
    private String applicantName;

    @Schema(
        description = "Full name as it appears on marriage certificate",
        example = "John Doe",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Size(min = 2, max = 200)
    private String respondentName;

    @Schema(
        description = "Marriage date in ISO-8601 format (yyyy-MM-dd)",
        example = "2012-05-04",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate marriageDate;

    @Schema(
        description = "Did the marriage take place in the UK?",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean ukMarriage;

    @Schema(
        description = "Country where the marriage took place (required if ukMarriage is false)",
        example = "France"
    )
    @Size(min = 2, max = 200)
    private String countryOfMarriage;

    @Schema(
        description = "Translation provided (required if certificate is not in English)",
        example = "false",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean translationProvided;

    @Schema(
        description = "Place of marriage (city, venue, or registry)",
        example = "London",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Size(min = 2, max = 200)
    private String placeOfMarriage;

    @Schema(
        description = "Certificate number or serial number on the marriage certificate",
        example = "CERT-123456",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Size(min = 1, max = 100)
    private String certificateNumber;

    @AssertTrue(message = "countryOfMarriage must be provided when ukMarriage is false")
    public boolean isCountryProvidedForNonUkMarriage() {
        return ukMarriage || (countryOfMarriage != null && !countryOfMarriage.isBlank());
    }
}
