package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.Museum;
import com.sanxingdui.twin.service.MuseumService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 博物馆 管理接口（通用增删改查）
 */
@Tag(name = "博物馆")
@RestController
@RequestMapping("/api/museums")
public class MuseumController extends BaseCrudController<Museum, MuseumService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("name", "address");
    }
}
