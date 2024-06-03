package kea.project.moaboaimageservice.global.aop;

import kea.project.moaboaimageservice.api.service.ImageService;
import kea.project.moaboaimageservice.global.common.exception.CustomException;
import kea.project.moaboaimageservice.global.common.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class TokenAspect {

    private final ImageService imageService;

    @AfterThrowing(pointcut= "execution(* kea.project.moaboaimageservice.api.service.ImageService.uploadImage(..))",throwing = "ex")
    public void afterThrowingUploadImage(JoinPoint joinPoint, CustomException ex ) {
        if(ex.getResponseCode().equals(ResponseCode.INVALID_TOKEN)){
            imageService.refreshToken();
        }
    }
}
