package com.itservicedesk.modules.attachment.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    StoredFileMetadata storeFile(MultipartFile file);

    Resource loadFileAsResource(String storedFileName);

    void deleteFile(String storedFileName);

    record StoredFileMetadata(
            String originalFileName,
            String storedFileName,
            String filePath,
            String contentType,
            long fileSize
    ) {}
}
