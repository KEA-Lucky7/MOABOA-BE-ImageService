package kea.project.moaboaimageservice.api.controller;

import kea.project.moaboaimageservice.api.controller.dto.UploadImageResponse;
import kea.project.moaboaimageservice.api.service.ImageService;
import kea.project.moaboaimageservice.global.common.exception.CustomException;
import kea.project.moaboaimageservice.global.common.exception.ErrorResponse;
import kea.project.moaboaimageservice.global.common.exception.ResponseCode;
import kea.project.moaboaimageservice.global.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/image")
public class ImageController {

    private final ImageService imageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadImageResponse uploadImage(@RequestPart(value = "image") MultipartFile image) {
        if(image.isEmpty()) {
            log.error("Image File request is empty");
            throw new CustomException(ResponseCode.EMPTY_IMAGE);
        }
        if (!FileUtil.isImageFile(image)) {
            log.error("Uploaded file is not a valid image");
            throw new CustomException(ResponseCode.INVALID_IMAGE_TYPE);
        }
        return imageService.uploadImage(image);
    }
}
