package BukkeuBukkeu.Book_Eum.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory(() -> {
                    // 1. 기본 JDK HTTP 연결 팩토리 생성
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

                    // 2. 타임아웃 설정 (Duration을 밀리초(int)로 변환 필요)
                    factory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis()); // 연결 10초
                    factory.setReadTimeout((int) Duration.ofMinutes(5).toMillis());     // 읽기 5분 (GCP 대기용)

                    return factory;
                })
                .build();
    }
}
