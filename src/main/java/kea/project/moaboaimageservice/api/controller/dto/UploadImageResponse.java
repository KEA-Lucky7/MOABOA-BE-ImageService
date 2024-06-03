package kea.project.moaboaimageservice.api.controller.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UploadImageResponse {
    private final String imageUrl;

    @Builder
    public UploadImageResponse(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public static UploadImageResponse of(String url) {
        return new UploadImageResponse(url);
    }
}
