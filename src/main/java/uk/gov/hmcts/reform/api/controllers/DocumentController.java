package uk.gov.hmcts.reform.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.api.models.CaseDetails;
import uk.gov.hmcts.reform.api.services.DocumentService;
import uk.gov.hmcts.reform.api.validation.annotations.ValidDocumentFile;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/documents")
@Validated
@Tag(name = "Documents", description = "Document intake endpoints")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(
        value = "/divorce",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
        summary = "Submit divorce document",
        description = "Accepts a divorce document (PDF/PNG/JPEG up to 500MB) with associated case details."
    )
    @ApiResponse(responseCode = "200", description = "Document accepted")
    @ApiResponse(
        responseCode = "400",
        description = "Validation failed",
        content = @Content(schema = @Schema(hidden = true))
    )
    public ResponseEntity<String> processDivorce(
        @ValidDocumentFile @RequestPart("file") MultipartFile file,
        @Valid @RequestPart("case") CaseDetails caseDetails
    ) {
        return ok(documentService.processDivorceDocument(file, caseDetails));
    }

    @PostMapping(
        value = "/probate",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
        summary = "Submit probate document",
        description = "Accepts a probate document (PDF/PNG/JPEG up to 500MB) with associated case details."
    )
    @ApiResponse(responseCode = "200", description = "Document accepted")
    @ApiResponse(
        responseCode = "400",
        description = "Validation failed",
        content = @Content(schema = @Schema(hidden = true))
    )
    public ResponseEntity<String> processProbate(
        @ValidDocumentFile @RequestPart("file") MultipartFile file,
        @Valid @RequestPart("case") CaseDetails caseDetails
    ) {
        return ok(documentService.processProbateDocument(file, caseDetails));
    }
}
