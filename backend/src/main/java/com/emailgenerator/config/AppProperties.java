package com.emailgenerator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Application-level configuration properties.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Cors cors = new Cors();
    private Request request = new Request();

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOriginPatterns = new ArrayList<>(List.of("chrome-extension://*"));
    }

    @Getter
    @Setter
    public static class Request {
        private int maxEmailContentLength = 50000;
    }
}
