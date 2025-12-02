package uk.gov.hmcts.reform.api.controllers;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.api.support.AbstractFunctionalTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

class DivorceDocumentFunctionalTest extends AbstractFunctionalTest {

    @Test
    @DisplayName("accepts valid PDF and case details")
    void shouldAcceptValidPdf() throws Exception {
        byte[] pdf = pdf(1);

        Response response = upload(pdf, "doc.pdf", baseDivorceCase());

        assertThat(response.statusCode()).isEqualTo(OK.value());
        assertThat(response.body().asString()).isEqualTo("ok");
    }

    @Test
    @DisplayName("rejects blank applicantName")
    void shouldRejectBlankApplicantName() throws Exception {
        byte[] pdf = pdf(1);
        Map<String, Object> payload = baseDivorceCase();
        payload.put("applicantName", "");
        String caseJson = toJson(payload);

        Response response = upload(pdf, "doc.pdf", caseJson);

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        List<String> fields = errorFields(response);
        List<String> messages = errorMessages(response);
        assertThat(response.jsonPath().getString("message")).containsIgnoringCase("validation failed");
        assertThat(fields).contains("applicantName");
        assertThat(messages).anyMatch(msg -> msg.toLowerCase().contains("must not be blank"));
    }

    @Test
    @DisplayName("rejects respondentName when too short")
    void shouldRejectShortRespondentName() throws Exception {
        byte[] pdf = pdf(1);
        Map<String, Object> payload = baseDivorceCase();
        payload.put("respondentName", "A");
        String caseJson = toJson(payload);

        Response response = upload(pdf, "doc.pdf", caseJson);

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        List<String> fields = errorFields(response);
        List<String> messages = errorMessages(response);
        assertThat(fields).contains("respondentName");
        assertThat(messages).anyMatch(msg -> msg.toLowerCase().contains("size must be between 2 and 200"));
    }

    @Test
    @DisplayName("rejects respondentName when too long")
    void shouldRejectLongRespondentName() throws Exception {
        byte[] pdf = pdf(1);
        String longName = "B".repeat(201);
        Map<String, Object> payload = baseDivorceCase();
        payload.put("respondentName", longName);
        String caseJson = toJson(payload);

        Response response = upload(pdf, "doc.pdf", caseJson);

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        List<String> fields = errorFields(response);
        List<String> messages = errorMessages(response);
        assertThat(fields).contains("respondentName");
        assertThat(messages).anyMatch(msg -> msg.toLowerCase().contains("size must be between 2 and 200"));
    }

    @Test
    @DisplayName("rejects applicantName when too short")
    void shouldRejectShortApplicantName() throws Exception {
        byte[] pdf = pdf(1);
        Map<String, Object> payload = baseDivorceCase();
        payload.put("applicantName", "A");
        String caseJson = toJson(payload);

        Response response = upload(pdf, "doc.pdf", caseJson);

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        List<String> fields = errorFields(response);
        List<String> messages = errorMessages(response);
        assertThat(fields).contains("applicantName");
        assertThat(messages).anyMatch(msg -> msg.toLowerCase().contains("size must be between 2 and 200"));
    }

    @Test
    @DisplayName("rejects applicantName when too long")
    void shouldRejectLongApplicantName() throws Exception {
        byte[] pdf = pdf(1);
        String longName = "A".repeat(201);
        Map<String, Object> payload = baseDivorceCase();
        payload.put("applicantName", longName);
        String caseJson = toJson(payload);

        Response response = upload(pdf, "doc.pdf", caseJson);

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        List<String> fields = errorFields(response);
        List<String> messages = errorMessages(response);
        assertThat(fields).contains("applicantName");
        assertThat(messages).anyMatch(msg -> msg.toLowerCase().contains("size must be between 2 and 200"));
    }

    @Test
    @DisplayName("rejects countryOfMarriage when too short")
    void shouldRejectShortCountry() throws Exception {
        byte[] pdf = pdf(1);
        Map<String, Object> payload = baseDivorceCase();
        payload.put("ukMarriage", false);
        payload.put("countryOfMarriage", "A");
        String caseJson = toJson(payload);

        Response response = upload(pdf, "doc.pdf", caseJson);

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        List<String> fields = errorFields(response);
        List<String> messages = errorMessages(response);
        assertThat(fields).contains("countryOfMarriage");
        assertThat(messages).anyMatch(msg -> msg.toLowerCase().contains("size must be between 2 and 200"));
    }

    @Test
    @DisplayName("accepts non-UK marriage with country provided")
    void shouldAcceptNonUkMarriageWithCountry() throws Exception {
        byte[] pdf = pdf(1);
        Map<String, Object> payload = baseDivorceCase();
        payload.put("ukMarriage", false);
        payload.put("countryOfMarriage", "France");
        payload.put("placeOfMarriage", "Paris");
        payload.put("certificateNumber", "CERT-999");
        String caseJson = toJson(payload);

        Response response = upload(pdf, "doc.pdf", caseJson);

        assertThat(response.statusCode()).isEqualTo(OK.value());
        assertThat(response.body().asString()).isEqualTo("ok");
    }

    @Test
    @DisplayName("rejects missing country when ukMarriage is false")
    void shouldRejectMissingCountryForNonUkMarriage() throws Exception {
        byte[] pdf = pdf(1);
        Map<String, Object> payload = baseDivorceCase();
        payload.put("ukMarriage", false);
        payload.remove("countryOfMarriage");
        String caseJson = toJson(payload);

        Response response = upload(pdf, "doc.pdf", caseJson);

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        List<String> fields = errorFields(response);
        List<String> messages = errorMessages(response);
        assertThat(fields).contains("countryProvidedForNonUkMarriage");
        assertThat(messages).anyMatch(msg -> msg.toLowerCase().contains("must be provided"));
    }

    @Test
    @DisplayName("rejects invalid marriageDate format")
    void shouldRejectInvalidMarriageDate() throws Exception {
        byte[] pdf = pdf(1);
        Map<String, Object> payload = baseDivorceCase();
        payload.put("marriageDate", "04-05-2012");
        String caseJson = toJson(payload);

        Response response = upload(pdf, "doc.pdf", caseJson);

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        assertThat(bodyLower(response)).contains("bad request");
    }

    @Test
    @DisplayName("rejects missing marriageDate")
    void shouldRejectMissingMarriageDate() throws Exception {
        byte[] pdf = pdf(1);
        Map<String, Object> payload = baseDivorceCase();
        payload.remove("marriageDate");
        String caseJson = toJson(payload);

        Response response = upload(pdf, "doc.pdf", caseJson);

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        String body = bodyLower(response);
        assertThat(body).contains("marriagedate");
    }

    @Test
    @DisplayName("rejects unsupported file type")
    void shouldRejectUnsupportedType() {
        Response response = uploadDocument(
            "/documents/divorce",
            "hello".getBytes(),
            "note.txt",
            toJson(baseDivorceCase())
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
            toJson(baseDivorceCase())
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
            toJson(baseDivorceCase())
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
            toJson(baseDivorceCase())
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
            toJson(baseDivorceCase())
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
            toJson(baseDivorceCase()),
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
            toJson(baseDivorceCase()),
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
            toJson(baseDivorceCase()),
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
            toJson(baseDivorceCase()),
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
            toJson(baseDivorceCase()),
            "image/jpeg"
        );

        assertThat(response.statusCode()).isEqualTo(OK.value());
        assertThat(response.body().asString()).isEqualTo("ok");
    }

    @Test
    @DisplayName("accepts valid TIFF")
    void shouldAcceptValidTiff() throws Exception {
        byte[] tiff = tiff(200, 200);

        Response response = uploadDocument(
            "/documents/divorce",
            tiff,
            "scan.tiff",
            toJson(baseDivorceCase()),
            "image/tiff"
        );

        assertThat(response.statusCode()).isEqualTo(OK.value());
        assertThat(response.body().asString()).isEqualTo("ok");
    }

    @Test
    @DisplayName("rejects corrupt TIFF")
    void shouldRejectCorruptTiff() {
        byte[] corrupt = "bad-tiff".getBytes();

        Response response = uploadDocument(
            "/documents/divorce",
            corrupt,
            "bad.tiff",
            toJson(baseDivorceCase()),
            "image/tiff"
        );

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        String message = response.jsonPath().getString("message").toLowerCase();
        assertThat(message).matches(".*(invalid|unsupported).*");
    }

    @Test
    @DisplayName("rejects unsupported image type")
    void shouldRejectUnsupportedImageType() {
        byte[] tiffLike = "not-supported-image".getBytes();

        Response response = uploadDocument(
            "/documents/divorce",
            tiffLike,
            "file.svg",
            toJson(baseDivorceCase()),
            "image/svg+xml"
        );

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        assertThat(response.jsonPath().getString("message")).isEqualTo("Unsupported file type.");
    }

    @Test
    @DisplayName("rejects missing ukMarriage (defaults to non-UK and fails country rule)")
    void shouldRejectMissingUkMarriage() throws Exception {
        byte[] pdf = pdf(1);
        Map<String, Object> payload = baseDivorceCase();
        payload.remove("ukMarriage");
        payload.remove("countryOfMarriage");
        String caseJson = toJson(payload);

        Response response = uploadDocument("/documents/divorce", pdf, "doc.pdf", caseJson);

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        List<String> fields = response.jsonPath().getList("errors.field", String.class);
        assertThat(fields).contains("countryProvidedForNonUkMarriage");
    }

    @Test
    @DisplayName("allows missing translationProvided (defaults false)")
    void shouldAllowMissingTranslationProvided() throws Exception {
        byte[] pdf = pdf(1);
        Map<String, Object> payload = baseDivorceCase();
        payload.remove("translationProvided");
        String caseJson = toJson(payload);

        Response response = uploadDocument("/documents/divorce", pdf, "doc.pdf", caseJson);

        assertThat(response.statusCode()).isEqualTo(OK.value());
        assertThat(response.body().asString()).isEqualTo("ok");
    }

    @Test
    @DisplayName("rejects too long countryOfMarriage")
    void shouldRejectLongCountry() throws Exception {
        byte[] pdf = pdf(1);
        String longCountry = "A".repeat(201);
        Map<String, Object> payload = baseDivorceCase();
        payload.put("ukMarriage", false);
        payload.put("countryOfMarriage", longCountry);
        String caseJson = toJson(payload);

        Response response = uploadDocument("/documents/divorce", pdf, "doc.pdf", caseJson);

        assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.value());
        List<String> fields = errorFields(response);
        List<String> messages = errorMessages(response);
        assertThat(fields).contains("countryOfMarriage");
        assertThat(messages).anyMatch(msg -> msg.toLowerCase().contains("size must be between 2 and 200"));
    }

    @Test
    @DisplayName("rejects PDF exceeding max file size")
    void shouldRejectOversizePdf() throws Exception {
        byte[] smallPdf = pdf(1);
        byte[] oversized = padToSize(smallPdf, 26 * 1024 * 1024);

        Response response = upload(oversized, "big.pdf", baseDivorceCase());

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CONTENT_TOO_LARGE.value());
    }

    @Test
    @DisplayName("rejects TIFF exceeding max file size")
    void shouldRejectOversizeTiff() throws Exception {
        byte[] smallTiff = tiff(100, 100);
        byte[] oversized = padToSize(smallTiff, 26 * 1024 * 1024);

        Response response = upload(oversized, "big.tiff", baseDivorceCase(), "image/tiff");

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CONTENT_TOO_LARGE.value());
    }
}
