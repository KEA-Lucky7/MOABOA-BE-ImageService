package kea.project.moaboaimageservice.global.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class CustomException extends RuntimeException {

    private final ResponseCode responseCode;
}