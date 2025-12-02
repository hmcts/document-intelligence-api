package uk.gov.hmcts.reform.api.controllers;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.api.support.AbstractFunctionalTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

class DivorceDocumentFunctionalTest extends AbstractFunctionalTest {

    @Test
    @DisplayName("accepts valid PDF and caseNumber")
    void shouldAcceptValidPdf() throws Exception {
        byte[] pdf = pdf(1);

        Response response = uploadDocument("/documents/divorce", pdf, "doc.pdf", "{\"caseNumber\":\"12345\"}");

        assertThat(response.statusCode()).isEqualTo(OK.value());
        assertThat(response.body().asString()).isEqualTo("ok");
    }

    @Test
    @DisplayName("rejects blank caseNumber")
    void shouldRejectBlankCaseNumber() throws Exception {
        byte[] pdf = pdf(1);

        Response response = uploadDocument("/documents/divorce", pdf, "doc.pdf", "{\"caseNumber\":\"\"}");

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        List<String> fields = response.jsonPath().getList("errors.field", String.class);
        List<String> messages = response.jsonPath().getList("errors.message", String.class);
        assertThat(response.jsonPath().getString("message")).containsIgnoringCase("validation failed");
        assertThat(fields).contains("caseNumber");
        assertThat(messages.stream().anyMatch(msg -> msg.toLowerCase().contains("blank"))).isTrue();
    }

    @Test
    @DisplayName("rejects unsupported file type")
    void shouldRejectUnsupportedType() {
        Response response = uploadDocument(
            "/documents/divorce",
            "hello".getBytes(),
            "note.txt",
            "{\"caseNumber\":\"12345\"}"
        );

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        assertThat(response.jsonPath().getString("message")).isEqualTo("Unsupported file type.");
    }

    @Test
    @DisplayName("rejects empty file")
    void shouldRejectEmptyFile() {
        Response response = uploadDocument(
            "/documents/divorce",
            new byte[0],
            "empty.pdf",
            "{\"caseNumber\":\"12345\"}"
        );

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        assertThat(response.jsonPath().getString("message")).isEqualTo("File is required.");
    }

    @Test
    @DisplayName("rejects PDF exceeding page limit")
    void shouldRejectPdfWithTooManyPages() throws Exception {
        byte[] pdf = pdf(2100);

        Response response = uploadDocument(
            "/documents/divorce",
            pdf,
            "too-many-pages.pdf",
            "{\"caseNumber\":\"12345\"}"
        );

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        assertThat(response.jsonPath().getString("message"))
            .containsIgnoringCase("page");
    }

    @Test
    @DisplayName("rejects encrypted PDF")
    void shouldRejectEncryptedPdf() throws Exception {
        byte[] pdf = encryptedPdf();

        Response response = uploadDocument(
            "/documents/divorce",
            pdf,
            "encrypted.pdf",
            "{\"caseNumber\":\"12345\"}"
        );

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        assertThat(response.jsonPath().getString("message"))
            .isEqualTo("Invalid or unreadable PDF.");
    }

    @Test
    @DisplayName("rejects corrupt PDF")
    void shouldRejectCorruptPdf() {
        byte[] corrupt = "not-a-pdf".getBytes();

        Response response = uploadDocument(
            "/documents/divorce",
            corrupt,
            "corrupt.pdf",
            "{\"caseNumber\":\"12345\"}"
        );

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        assertThat(response.jsonPath().getString("message")).isEqualTo("Unsupported file type.");
    }

    @Test
    @DisplayName("accepts valid image")
    void shouldAcceptValidImage() throws Exception {
        byte[] png = png(100, 100);

        Response response = uploadDocument(
            "/documents/divorce",
            png,
            "image.png",
            "{\"caseNumber\":\"12345\"}",
            "image/png"
        );

        assertThat(response.statusCode()).isEqualTo(OK.value());
        assertThat(response.body().asString()).isEqualTo("ok");
    }

    @Test
    @DisplayName("rejects image below min dimensions")
    void shouldRejectSmallImage() throws Exception {
        byte[] png = png(20, 20);

        Response response = uploadDocument(
            "/documents/divorce",
            png,
            "small.png",
            "{\"caseNumber\":\"12345\"}",
            "image/png"
        );

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        assertThat(response.jsonPath().getString("message"))
            .isEqualTo("Image dimensions below minimum (50 x 50).");
    }

    @Test
    @DisplayName("rejects image exceeding max dimensions")
    void shouldRejectLargeImage() throws Exception {
        byte[] png = png(12000, 120);

        Response response = uploadDocument(
            "/documents/divorce",
            png,
            "large.png",
            "{\"caseNumber\":\"12345\"}",
            "image/png"
        );

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        assertThat(response.jsonPath().getString("message"))
            .isEqualTo("Image dimensions exceed maximum (10000 x 10000).");
    }

    @Test
    @DisplayName("rejects corrupt image bytes")
    void shouldRejectCorruptImage() {
        byte[] corrupt = "not-an-image".getBytes();

        Response response = uploadDocument(
            "/documents/divorce",
            corrupt,
            "bad.png",
            "{\"caseNumber\":\"12345\"}",
            "image/png"
        );

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        String message = response.jsonPath().getString("message").toLowerCase();
        assertThat(message).matches(".*(invalid|unsupported).*");
    }

    @Test
    @DisplayName("accepts valid JPEG")
    void shouldAcceptValidJpeg() throws Exception {
        byte[] jpeg = jpg(200, 200);

        Response response = uploadDocument(
            "/documents/divorce",
            jpeg,
            "photo.jpg",
            "{\"caseNumber\":\"12345\"}",
            "image/jpeg"
        );

        assertThat(response.statusCode()).isEqualTo(OK.value());
        assertThat(response.body().asString()).isEqualTo("ok");
    }

    @Test
    @DisplayName("rejects unsupported image type")
    void shouldRejectUnsupportedImageType() {
        byte[] tiffLike = "not-supported-image".getBytes();

        Response response = uploadDocument(
            "/documents/divorce",
            tiffLike,
            "file.svg",
            "{\"caseNumber\":\"12345\"}",
            "image/svg+xml"
        );

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        assertThat(response.jsonPath().getString("message")).isEqualTo("Unsupported file type.");
    }
}
