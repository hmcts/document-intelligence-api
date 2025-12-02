package uk.gov.hmcts.reform.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.api.errorhandling.GlobalExceptionHandler;
import uk.gov.hmcts.reform.api.errorhandling.exceptions.InvalidFileException;
import uk.gov.hmcts.reform.api.models.CaseDetails;
import uk.gov.hmcts.reform.api.services.DocumentService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DocumentControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private DocumentService documentService;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        documentService = mock(DocumentService.class);
        reset(documentService);
        mockMvc = MockMvcBuilders.standaloneSetup(new DocumentController(documentService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void acceptsDivorceUpload() throws Exception {
        when(documentService.processDivorceDocument(any(), any())).thenReturn("ok");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/documents/divorce")
                .file(validPdf())
                .file(casePart("12345")))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo("ok");
    }

    @Test
    void acceptsProbateUpload() throws Exception {
        when(documentService.processProbateDocument(any(), any())).thenReturn("ok");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/documents/probate")
                .file(validPdf())
                .file(casePart("ABC123")))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo("ok");
    }

    @Test
    void mapsInvalidFileExceptionToBadRequest() throws Exception {
        doThrow(new InvalidFileException("File exceeds allowed size (5MB)."))
            .when(documentService).processDivorceDocument(any(), any());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/documents/divorce")
                .file(validPdf())
                .file(casePart("12345")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("File exceeds allowed size (5MB)."));
    }

    private MockMultipartFile validPdf() throws IOException {
        return new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            createPdfBytes(1)
        );
    }

    private MockMultipartFile casePart(String caseNumber) throws IOException {
        return new MockMultipartFile(
            "case",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(new CaseDetails(caseNumber))
        );
    }

    private byte[] createPdfBytes(int pages) throws IOException {
        try (PDDocument document = new PDDocument()) {
            for (int i = 0; i < pages; i++) {
                document.addPage(new PDPage());
            }
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                document.save(out);
                return out.toByteArray();
            }
        }
    }
}
