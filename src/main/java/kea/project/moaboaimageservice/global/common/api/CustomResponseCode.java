package kea.project.moaboaimageservice.global.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CustomResponseCode {
    SUCCESS("요청에 성공하였습니다.");
    private final String message;
}
