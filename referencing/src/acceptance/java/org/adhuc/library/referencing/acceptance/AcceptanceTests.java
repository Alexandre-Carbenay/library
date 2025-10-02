package org.adhuc.library.referencing.acceptance;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.cucumber.java.Before;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import org.jspecify.annotations.Nullable;
import org.junit.platform.suite.api.*;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME;
import static io.restassured.RestAssured.config;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("org.adhuc.library.referencing.acceptance")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME, value = "true")
@SuppressWarnings("preview")
public class AcceptanceTests {

    private static final String REFERENCING_SERVICE_NAME = "referencing";
    private static final String REFERENCING_EXPOSED_PORT = "8080";

    @Before
    public void configureRestAssured() {
        var baseUrl = new ServiceUrlResolver(REFERENCING_SERVICE_NAME, REFERENCING_EXPOSED_PORT).serviceUrl();
        RestAssured.baseURI = STR."\{baseUrl}/api";
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.config = config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                (type, s) -> {
                    var mapper = new ObjectMapper();
                    mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
                    mapper.setSerializationInclusion(Include.NON_ABSENT);
                    mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                    return mapper;
                }
        ));
    }

    static class ServiceUrlResolver {
        private static final String DEFAULT_HOST = "localhost";
        private final int port;

        ServiceUrlResolver(@Nullable String serviceName, @Nullable String exposedPort) {
            if (serviceName == null) {
                throw new NullPointerException("serviceName is marked non-null but is null");
            }
            if (exposedPort == null) {
                throw new NullPointerException("exposedPort is marked non-null but is null");
            }
            var portPropertyName = STR."\{serviceName}.tcp.\{exposedPort}";
            this.port = Integer.parseInt(System.getProperty(portPropertyName, exposedPort));
        }

        String serviceUrl() {
            return String.format("https://%s:%s", DEFAULT_HOST, this.port);
        }
    }

}
