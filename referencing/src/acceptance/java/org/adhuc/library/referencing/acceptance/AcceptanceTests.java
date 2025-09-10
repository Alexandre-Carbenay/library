package org.adhuc.library.referencing.acceptance;

import io.cucumber.java.Before;
import io.restassured.RestAssured;
import org.junit.platform.suite.api.*;

import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME;

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
    }

    static class ServiceUrlResolver {
        private static final String DEFAULT_HOST = "localhost";
        private final int port;

        ServiceUrlResolver(String serviceName, String exposedPort) {
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
