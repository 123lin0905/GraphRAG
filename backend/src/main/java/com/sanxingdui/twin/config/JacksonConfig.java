package com.sanxingdui.twin.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 全局配置：统一 java.time 日期格式
 */
@Configuration
public class JacksonConfig {

    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE = "yyyy-MM-dd";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME);
        DateTimeFormatter df = DateTimeFormatter.ofPattern(DATE);
        return builder -> builder
                .serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(dtf))
                .deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(dtf))
                .serializerByType(LocalDate.class, new LocalDateSerializer(df))
                .deserializerByType(LocalDate.class, new LocalDateDeserializer(df));
    }
}
