package kea.project.moaboaimageservice.api.service;

import kea.project.moaboaimageservice.api.controller.dto.UploadImageResponse;
import kea.project.moaboaimageservice.global.common.exception.CustomException;
import kea.project.moaboaimageservice.global.common.exception.ResponseCode;
import kea.project.moaboaimageservice.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final RestTemplate restTemplate;
    private final RedisUtil redisUtil;

    @Value("${kakao.cloud.image.bucket.url}")
    private String kcBucketUrl;

    @Value("${kakao.cloud.image.bucket.name}")
    private String kcBucketName;

    @Value("${kakao.cloud.image.cdn.url}")
    private String cdnUrl;

    @Override
    public UploadImageResponse uploadImage(MultipartFile image) {
        try {
            String imageExtension = getImageExtension(image);
            String imageName = createImageName(imageExtension);
            String uploadUrl = generateUploadUrl(imageName);
            String cdnUrl = generateCdnUrl(imageName);
            HttpHeaders httpHeaders = createHttpHeaders(image);
            Resource fileAsResource = createFileResource(image);

            HttpEntity<Resource> requestEntity = new HttpEntity<>(fileAsResource, httpHeaders);
            restTemplate.exchange(uploadUrl, HttpMethod.PUT, requestEntity, String.class);

            return UploadImageResponse.of(cdnUrl);
        } catch (IOException e) {
            log.error("Image upload failed", e);
            throw new CustomException(ResponseCode.FAIL_UPLOAD);
        }
    }

    private String getImageExtension(MultipartFile image) {
        return StringUtils.getFilenameExtension(image.getOriginalFilename());
    }

    private HttpHeaders createHttpHeaders(MultipartFile image) {
        HttpHeaders httpHeaders = new HttpHeaders();
        MediaType mimeType = getMediaType(image);
        httpHeaders.setContentType(mimeType);

        String authToken = redisUtil.get("X-Auth-Token");
        httpHeaders.add("X-Auth-Token", authToken);

        return httpHeaders;
    }

    private MediaType getMediaType(MultipartFile image) {
        String mimeType = image.getContentType();
        return mimeType == null || mimeType.isEmpty() ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(mimeType);
    }

    private Resource createFileResource(MultipartFile image) throws IOException {
        return new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        };
    }

    private String generateUploadUrl(String imageName) {
        return String.format("%s/%s/%s", kcBucketUrl, kcBucketName, imageName);
    }

    private String generateCdnUrl(String imageName) {
        return String.format("%s/%s", cdnUrl, imageName);
    }

    private String createImageName(String imageExtension) {
        return UUID.randomUUID() + "." + imageExtension;
    }
}
