package com.sanxingdui.twin.controller;

import com.sanxingdui.twin.entity.DynastyPeriod;
import com.sanxingdui.twin.service.DynastyPeriodService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 年代文化期 管理接口（通用增删改查）
 */
@Tag(name = "年代文化期")
@RestController
@RequestMapping("/api/periods")
public class DynastyPeriodController extends BaseCrudController<DynastyPeriod, DynastyPeriodService> {

    @Override
    protected List<String> keywordColumns() {
        return List.of("name", "culture_name");
    }
}
