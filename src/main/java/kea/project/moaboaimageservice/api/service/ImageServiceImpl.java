package kea.project.moaboaimageservice.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
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

    @Value("${kakao.cloud.access.url}")
    private String accessTokenRequestUrl;

    @Value("${kakao.cloud.access.id}")
    private String accessId;

    @Value("${kakao.cloud.access.secret}")
    private String accessSecret;

    @Value("${kakao.cloud.token.name}")
    private String TokenName;

    @Value("${kakao.cloud.access.header.token.name}")
    private String accessTokenHeaderName;

    @Override
    public UploadImageResponse uploadImage(MultipartFile image) {
        try {
            String imageExtension = getImageExtension(image);
            String imageName = createImageName(imageExtension);
            String uploadUrl = generateUploadUrl(imageName);
            String cdnUrl = generateCdnUrl(imageName);
            HttpHeaders httpHeaders = createImageHttpHeaders(image);
            Resource fileAsResource = createImageFileResource(image);

            HttpEntity<Resource> requestEntity = new HttpEntity<>(fileAsResource, httpHeaders);
            restTemplate.exchange(uploadUrl, HttpMethod.PUT, requestEntity, String.class);

            return UploadImageResponse.of(cdnUrl);
        } catch (HttpStatusCodeException e) {
            throw new CustomException(ResponseCode.INVALID_TOKEN);
        } catch (IOException e) {
            throw new CustomException(ResponseCode.FAIL_UPLOAD);
        }
    }

    @Override
    public void refreshToken() {
        String newToken = requestNewToken();
        redisUtil.set(TokenName, newToken);
    }

    private String requestNewToken() {

        Map<String, Object> requestBody = createRequestBody();
        String jsonRequestBody = convertToJson(requestBody);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(jsonRequestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(accessTokenRequestUrl, HttpMethod.POST, entity, String.class);

        String token = response.getHeaders().getFirst(accessTokenHeaderName);

        if (token == null) {
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }

        return token;
    }

    private Map<String, Object> createRequestBody() {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> authMap = new HashMap<>();
        Map<String, Object> identityMap = new HashMap<>();
        Map<String, Object> applicationCredentialMap = new HashMap<>();

        applicationCredentialMap.put("id", accessId);  // Replace with your access key ID
        applicationCredentialMap.put("secret", accessSecret);  // Replace with your secret access key

        identityMap.put("methods", new String[]{"application_credential"});
        identityMap.put("application_credential", applicationCredentialMap);

        authMap.put("identity", identityMap);
        requestBody.put("auth", authMap);

        return requestBody;
    }

    private String convertToJson(Map<String, Object> requestBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert request body to JSON", e);
        }
    }

    private String getImageExtension(MultipartFile image) {
        return StringUtils.getFilenameExtension(image.getOriginalFilename());
    }

    private HttpHeaders createImageHttpHeaders(MultipartFile image) {
        HttpHeaders httpHeaders = new HttpHeaders();
        MediaType mimeType = getMediaType(image);
        httpHeaders.setContentType(mimeType);

        String authToken = redisUtil.get(TokenName);
        httpHeaders.add(TokenName, authToken);

        return httpHeaders;
    }

    private MediaType getMediaType(MultipartFile image) {
        String mimeType = image.getContentType();
        return mimeType == null || mimeType.isEmpty() ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(mimeType);
    }

    private Resource createImageFileResource(MultipartFile image) throws IOException {
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
