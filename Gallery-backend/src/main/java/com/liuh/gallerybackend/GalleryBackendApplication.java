package com.liuh.gallerybackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.liuh.gallerybackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class GalleryBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(GalleryBackendApplication.class, args);
    }

}
