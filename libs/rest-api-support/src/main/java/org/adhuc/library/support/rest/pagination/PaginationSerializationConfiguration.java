package org.adhuc.library.support.rest.pagination;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.PagedModel;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.module.SimpleSerializers;

@Configuration
public class PaginationSerializationConfiguration {
    @Bean
    public JsonMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.addModule(new PaginationModule());
    }

    private static class PaginationModule extends SimpleModule {
        @Override
        public void setupModule(SetupContext context) {
            var serializers = new SimpleSerializers();
            serializers.addSerializer(PagedModel.PageMetadata.class, new PageMetadataSerializer());
            context.addSerializers(serializers);
        }
    }

    private static class PageMetadataSerializer extends ValueSerializer<PagedModel.PageMetadata> {
        @Override
        public void serialize(PagedModel.PageMetadata value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
            gen.writeName("page");
            gen.writeStartObject();
            gen.writeNumberProperty("size", value.getSize());
            gen.writeNumberProperty("total_elements", value.getTotalElements());
            gen.writeNumberProperty("total_pages", value.getTotalPages());
            gen.writeNumberProperty("number", value.getNumber());
            gen.writeEndObject();
        }
    }
}
