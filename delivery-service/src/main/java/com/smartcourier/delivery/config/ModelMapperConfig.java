package com.smartcourier.delivery.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();


        mapper.getConfiguration()
                .setSkipNullEnabled(true)   // ignore null values
                .setAmbiguityIgnored(true); // avoid mapping conflicts

        return mapper;
    }
}
