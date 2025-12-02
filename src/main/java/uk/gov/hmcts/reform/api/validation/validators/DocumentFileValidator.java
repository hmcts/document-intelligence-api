package uk.gov.hmcts.reform.api.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.api.errorhandling.exceptions.InvalidFileException;
import uk.gov.hmcts.reform.api.config.UploadValidationProperties;
import uk.gov.hmcts.reform.api.validation.annotations.ValidDocumentFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class DocumentFileValidator implements ConstraintValidator<ValidDocumentFile, MultipartFile> {

    private static final Tika TIKA = new Tika();
    private final UploadValidationProperties properties;

    public DocumentFileValidator(UploadValidationProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        byte[] bytes = extractBytes(file);
        String mediaType = detectMediaType(bytes, file.getOriginalFilename());

        if ("application/pdf".equals(mediaType)) {
            validatePdf(bytes);
        } else if (mediaType != null && mediaType.startsWith("image/")) {
            validateImage(bytes);
        } else {
            throw new InvalidFileException("Unsupported file type.");
        }
        return true;
    }

    private byte[] extractBytes(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is required.");
        }
        if (file.getSize() > properties.getMaxFileSize().toBytes()) {
            throw new InvalidFileException(String.format(
                "File exceeds allowed size (%s).",
                properties.getMaxFileSize()
            ));
        }

        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new InvalidFileException("Invalid or unreadable file.");
        }
    }

    private String detectMediaType(byte[] bytes, String originalFilename) {
        String type = TIKA.detect(bytes, originalFilename);
        if (!properties.getAllowedTypes().contains(type)) {
            throw new InvalidFileException("Unsupported file type.");
        }
        return type;
    }

    private void validatePdf(byte[] bytes) {
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            if (doc.isEncrypted()) {
                throw new InvalidFileException("PDF encryption is not supported.");
            }
            if (doc.getNumberOfPages() > properties.getMaxPdfPages()) {
                throw new InvalidFileException(String.format(
                    "PDF exceeds allowed page limit (%d).",
                    properties.getMaxPdfPages()
                ));
            }
        } catch (IOException ex) {
            throw new InvalidFileException("Invalid or unreadable PDF.");
        }
    }

    private void validateImage(byte[] bytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new InvalidFileException("Invalid image file.");
            }

            long width = image.getWidth();
            long height = image.getHeight();
            if (width <= 0 || height <= 0) {
                throw new InvalidFileException("Invalid image dimensions.");
            }
            if (width < properties.getMinImageDimension() || height < properties.getMinImageDimension()) {
                throw new InvalidFileException(String.format(
                    "Image dimensions below minimum (%d x %d).",
                    properties.getMinImageDimension(),
                    properties.getMinImageDimension()
                ));
            }
            if (width > properties.getMaxImageDimension() || height > properties.getMaxImageDimension()) {
                throw new InvalidFileException(String.format(
                    "Image dimensions exceed maximum (%d x %d).",
                    properties.getMaxImageDimension(),
                    properties.getMaxImageDimension()
                ));
            }

            long pixels = width * height;
            if (pixels > properties.getMaxImagePixels()) {
                throw new InvalidFileException(String.format(
                    "Image exceeds allowed pixel count (%d).",
                    properties.getMaxImagePixels()
                ));
            }

        } catch (IOException ex) {
            throw new InvalidFileException("Invalid or unreadable image.");
        }
    }
}
