package uk.gov.hmcts.reform.api.support;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import uk.gov.hmcts.reform.api.helpers.DocumentHelper;

import java.io.IOException;

public abstract class AbstractFunctionalTest {

    private static final String BASE_URL = System.getenv().getOrDefault("TEST_URL", "http://localhost:8997");

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
}
