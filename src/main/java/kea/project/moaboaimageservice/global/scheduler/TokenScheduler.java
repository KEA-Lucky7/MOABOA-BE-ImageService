package kea.project.moaboaimageservice.global.scheduler;

import jakarta.annotation.PostConstruct;
import kea.project.moaboaimageservice.api.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenScheduler {

    private final ImageService imageService;

    @PostConstruct //spring application이 실행할때마다 token 생성
    public void init() {
        imageService.refreshToken();
    }
    @Scheduled(fixedRate = 39600000) // 11시간마다 실행 (11 * 60 * 60 * 1000 milliseconds)
    public void scheduleTokenRefresh() {
        imageService.refreshToken();
    }
}
