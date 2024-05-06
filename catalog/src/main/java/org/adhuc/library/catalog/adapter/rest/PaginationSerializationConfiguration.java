package org.adhuc.library.catalog.adapter.rest;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.PagedModel;

import java.io.IOException;

@Configuration
public class PaginationSerializationConfiguration {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.serializerByType(PagedModel.PageMetadata.class, new PageMetadataSerializer());
    }

    private static class PageMetadataSerializer extends JsonSerializer<PagedModel.PageMetadata> {
        @Override
        public void serialize(PagedModel.PageMetadata value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeFieldName("page");
            gen.writeStartObject();
            gen.writeNumberField("size", value.getSize());
            gen.writeNumberField("total_elements", value.getTotalElements());
            gen.writeNumberField("total_pages", value.getTotalPages());
            gen.writeNumberField("number", value.getNumber());
            gen.writeEndObject();
        }
    }
}
