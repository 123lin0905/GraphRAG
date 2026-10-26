package com.sanxingdui.twin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sanxingdui.twin.entity.SystemConfig;
import com.sanxingdui.twin.rag.LlmSettings;

import java.util.List;
import java.util.Map;

/**
 * 系统参数配置服务
 */
public interface SystemConfigService extends IService<SystemConfig> {

    /** 读取配置值，不存在或为空时返回默认值 */
    String getValue(String key, String defaultValue);

    /** 读取整数配置 */
    int getInt(String key, int defaultValue);

    /** 查询配置列表（SECRET 类型脱敏） */
    List<SystemConfig> listMasked(String group);

    /** 批量更新配置（脱敏占位值不覆盖原值） */
    void updateValues(Map<String, String> values);

    /** 当前生效的大模型配置 */
    LlmSettings llmSettings();
}
