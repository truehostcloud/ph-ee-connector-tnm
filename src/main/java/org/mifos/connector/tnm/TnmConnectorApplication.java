package org.mifos.connector.tnm;

import static org.mifos.connector.tnm.camel.config.CamelProperties.CUSTOM_HEADER_FILTER_STRATEGY;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.mifos.connector.tnm.camel.config.CustomHeaderFilterStrategy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Class containing the main (entry point) method.
 */
@SpringBootApplication
public class TnmConnectorApplication {

    /**
     * Configures the object mapper to be used for serialization and deserialization.
     *
     * @return {@link ObjectMapper}
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean(CUSTOM_HEADER_FILTER_STRATEGY)
    public CustomHeaderFilterStrategy headerFilterStrategy() {
        return new CustomHeaderFilterStrategy();
    }

    public static void main(String[] args) {
        SpringApplication.run(TnmConnectorApplication.class, args);
    }

}
