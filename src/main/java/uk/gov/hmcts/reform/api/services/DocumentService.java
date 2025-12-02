package uk.gov.hmcts.reform.api.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.api.models.CaseDetails;

@Service
public class DocumentService {

    public String processDivorceDocument(MultipartFile file, CaseDetails caseDetails) {
        return "ok";
    }

    public String processProbateDocument(MultipartFile file, CaseDetails caseDetails) {
        return "ok";
    }
}
