package com.lopjv.qlhoctap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Cấu hình chung của ứng dụng: RestTemplate, v.v.
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate bean với timeout để gọi Gemini AI API.
     * Thay thế việc khởi tạo new RestTemplate() trực tiếp trong service.
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10 giây
        factory.setReadTimeout(60000);    // 60 giây
        return new RestTemplate(factory);
    }
}
