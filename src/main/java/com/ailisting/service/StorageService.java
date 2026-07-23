package com.ailisting.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String uploadFile(MultipartFile file);

    void deleteFile(String fileUrl);

    String getFileUrl(String fileName);
}