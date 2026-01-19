package org.adhuc.library.support.rest.validation.jsr303;

import org.adhuc.library.support.rest.validation.RequestValidationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@Import(Jsr303ValidationExceptionHandler.class)
@ConditionalOnBooleanProperty("org.adhuc.library.support.rest.validation.jsr303.enabled")
public class Jsr303ValidationConfiguration {

    private final RequestValidationProperties properties;

    public Jsr303ValidationConfiguration(RequestValidationProperties properties) {
        this.properties = properties;
    }

    @Bean
    public MessageSource messageSource() {
        var messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename(properties.jsr303().messageBasename());
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(true);
        return messageSource;
    }

    @Bean
    public LocalValidatorFactoryBean getValidator(MessageSource messageSource) {
        var bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }

}
