package uk.gov.hmcts.reform.api.support;

import io.restassured.RestAssured;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import uk.gov.hmcts.reform.api.helpers.DocumentHelper;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractFunctionalTest {

    private static final String BASE_URL = System.getenv().getOrDefault("TEST_URL", "http://localhost:8997");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    protected Response uploadDocument(String path, byte[] file, String filename, String caseJson) {
        return RestAssured.given()
            .baseUri(BASE_URL)
            .multiPart("file", filename, file)
            .multiPart("case", caseJson, "application/json")
            .post(path);
    }

    protected Response uploadDocument(String path, byte[] file, String filename, String caseJson, String contentType) {
        return RestAssured.given()
            .baseUri(BASE_URL)
            .multiPart("file", filename, file, contentType)
            .multiPart("case", caseJson, "application/json")
            .post(path);
    }

    protected byte[] pdf(int pages) throws IOException {
        return DocumentHelper.createPdf(pages);
    }

    protected byte[] encryptedPdf() throws IOException {
        return DocumentHelper.createEncryptedPdf();
    }

    protected byte[] png(int width, int height) throws IOException {
        return DocumentHelper.createPng(width, height);
    }

    protected byte[] jpg(int width, int height) throws IOException {
        return DocumentHelper.createJpeg(width, height);
    }

    protected byte[] tiff(int width, int height) throws IOException {
        return DocumentHelper.createTiff(width, height);
    }

    protected Response upload(byte[] file, String filename, Map<String, Object> payload) {
        return uploadDocument("/documents/divorce", file, filename, toJson(payload));
    }

    protected Response upload(byte[] file, String filename, String caseJson) {
        return uploadDocument("/documents/divorce", file, filename, caseJson);
    }

    protected Response upload(byte[] file, String filename, Map<String, Object> payload, String contentType) {
        return uploadDocument("/documents/divorce", file, filename, toJson(payload), contentType);
    }

    protected Map<String, Object> baseDivorceCase() {
        Map<String, Object> map = new HashMap<>();
        map.put("applicantName", "Jane Doe");
        map.put("respondentName", "John Doe");
        map.put("marriageDate", "2012-05-04");
        map.put("ukMarriage", true);
        map.put("countryOfMarriage", "UK");
        map.put("translationProvided", false);
        map.put("placeOfMarriage", "London");
        map.put("certificateNumber", "CERT-123");
        return map;
    }

    protected String toJson(Map<String, Object> payload) {
        try {
            return MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialise payload", e);
        }
    }

    protected byte[] padToSize(byte[] original, int targetBytes) {
        if (original.length >= targetBytes) {
            return original;
        }
        return Arrays.copyOf(original, targetBytes);
    }

    protected List<String> errorFields(Response response) {
        return response.jsonPath().getList("errors.field", String.class);
    }

    protected List<String> errorMessages(Response response) {
        return response.jsonPath().getList("errors.message", String.class);
    }

    protected String bodyLower(Response response) {
        return response.body().asString().toLowerCase();
    }
}
