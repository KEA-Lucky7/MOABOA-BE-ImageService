package kea.project.moaboaimageservice.global.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {
    //GLOBAL
    BAD_REQUEST("GLB-ERR-001", HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    METHOD_NOT_ALLOWED("GLB-ERR-002", HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메서드입니다."),
    INTERNAL_SERVER_ERROR("GLB-ERR-003", HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류입니다."),
    EMPTY_IMAGE("IMG-ERR-001",HttpStatus.BAD_REQUEST , "이미지는 비어있으면 안됩니다."),
    FAIL_UPLOAD("IMG-ERR-002",HttpStatus.BAD_REQUEST , "이미지 업로드에 실패했습니다."),
    INVALID_IMAGE_TYPE("IMG-ERR-003", HttpStatus.BAD_REQUEST , "이미지 타입이 올바르지 않습니다."),
    INVALID_TOKEN("IMG-ERR-004", HttpStatus.INTERNAL_SERVER_ERROR, "다시한번 시도해주세요");

    private final String code;
    private final HttpStatus status;
    private final String message;


    public String getMessage(Throwable e) {
        return this.getMessage(this.getMessage() + " - " + e.getMessage());
    }

    public String getMessage(String message) {
        return Optional.ofNullable(message)
                .filter(Predicate.not(String::isBlank))
                .orElse(this.getMessage());
    }
}
