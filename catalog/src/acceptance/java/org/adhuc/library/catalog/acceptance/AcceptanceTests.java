package org.adhuc.library.catalog.acceptance;

import io.cucumber.java.Before;
import io.restassured.RestAssured;
import org.junit.platform.suite.api.*;

import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("org.adhuc.library.catalog.acceptance")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME, value = "true")
@SuppressWarnings("preview")
public class AcceptanceTests {

    private static final String CATALOG_SERVICE_NAME = "catalog";
    private static final String CATALOG_EXPOSED_PORT = "8080";

    @Before
    public void configureRestAssured() {
        var baseUrl = new ServiceUrlResolver(CATALOG_SERVICE_NAME, CATALOG_EXPOSED_PORT).serviceUrl();
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
