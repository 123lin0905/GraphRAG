package com.sanxingdui.twin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 三星堆文物数字孪生与智能导览系统 —— 后端启动类
 */
@SpringBootApplication
@ConfigurationPropertiesScan("com.sanxingdui.twin.config")
@MapperScan("com.sanxingdui.twin.mapper")
public class SanxingduiTwinApplication {

    public static void main(String[] args) {
        SpringApplication.run(SanxingduiTwinApplication.class, args);
    }
}
