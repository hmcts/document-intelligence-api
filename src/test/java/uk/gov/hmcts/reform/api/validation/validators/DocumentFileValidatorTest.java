package uk.gov.hmcts.reform.api.validation.validators;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;
import uk.gov.hmcts.reform.api.config.UploadValidationProperties;
import uk.gov.hmcts.reform.api.errorhandling.exceptions.InvalidFileException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentFileValidatorTest {

    private UploadValidationProperties properties;
    private DocumentFileValidator validator;

    @BeforeEach
    void setup() {
        properties = new UploadValidationProperties();
        properties.setMaxFileSize(DataSize.ofMegabytes(5));
        properties.setMaxPdfPages(5);
        properties.setMaxImagePixels(5_000);
        properties.setMinImageDimension(50);
        properties.setMaxImageDimension(100);
        properties.setAllowedTypes(List.of("application/pdf", "image/png", "image/jpeg"));
        validator = new DocumentFileValidator(properties);
    }

    @Test
    void acceptsValidPdf() throws IOException {
        byte[] pdf = createPdf(1);
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", pdf);

        assertDoesNotThrow(() -> validator.isValid(file, null));
    }

    @Test
    void rejectsPdfExceedingPageLimit() throws IOException {
        byte[] pdf = createPdf(10);
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", pdf);

        InvalidFileException ex = assertThrows(InvalidFileException.class, () -> validator.isValid(file, null));
        assertThat(ex.getMessage()).contains("page limit");
    }

    @Test
    void acceptsValidImage() throws IOException {
        byte[] image = createPng(60, 60);
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", image);

        assertDoesNotThrow(() -> validator.isValid(file, null));
    }

    @Test
    void rejectsImageExceedingPixelLimit() throws IOException {
        properties.setMaxImagePixels(100); // tighten for test
        validator = new DocumentFileValidator(properties);

        byte[] image = createPng(60, 60); // within dimension range but over pixel cap
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", image);

        InvalidFileException ex = assertThrows(InvalidFileException.class, () -> validator.isValid(file, null));
        assertThat(ex.getMessage()).contains("pixel count");
    }

    @Test
    void rejectsUnsupportedType() {
        MockMultipartFile file = new MockMultipartFile("file", "note.txt", "text/plain", "hello".getBytes());

        InvalidFileException ex = assertThrows(InvalidFileException.class, () -> validator.isValid(file, null));
        assertThat(ex.getMessage()).contains("Unsupported file type");
    }

    @Test
    void rejectsOversizedFile() {
        byte[] largePayload = new byte[(int) properties.getMaxFileSize().toBytes() + 1];
        MockMultipartFile file = new MockMultipartFile("file", "big.pdf", "application/pdf", largePayload);

        InvalidFileException ex = assertThrows(InvalidFileException.class, () -> validator.isValid(file, null));
        assertThat(ex.getMessage()).contains("allowed size");
    }

    @Test
    void rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        InvalidFileException ex = assertThrows(InvalidFileException.class, () -> validator.isValid(file, null));
        assertThat(ex.getMessage()).contains("File is required");
    }

    private byte[] createPdf(int pages) throws IOException {
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

    private byte[] createPng(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        }
    }
}
