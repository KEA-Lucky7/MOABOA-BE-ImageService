package kea.project.moaboaimageservice.api.service;


import kea.project.moaboaimageservice.api.controller.dto.UploadImageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    UploadImageResponse uploadImage(MultipartFile image);

    void refreshToken();
}
