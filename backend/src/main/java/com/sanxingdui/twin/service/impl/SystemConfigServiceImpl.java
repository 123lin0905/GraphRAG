package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.common.BusinessException;
import com.sanxingdui.twin.config.LlmProperties;
import com.sanxingdui.twin.entity.SystemConfig;
import com.sanxingdui.twin.mapper.SystemConfigMapper;
import com.sanxingdui.twin.rag.LlmSettings;
import com.sanxingdui.twin.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 系统参数配置服务实现
 */
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    /** 脱敏占位符：前端回传该值表示“不修改” */
    public static final String MASK = "******";

    private final LlmProperties llmProperties;

    @Override
    public String getValue(String key, String defaultValue) {
        SystemConfig config = getOne(new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key), false);
        return config == null || !StringUtils.hasText(config.getConfigValue()) ? defaultValue : config.getConfigValue().trim();
    }

    @Override
    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(getValue(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public List<SystemConfig> listMasked(String group) {
        List<SystemConfig> list = list(new LambdaQueryWrapper<SystemConfig>()
                .eq(StringUtils.hasText(group), SystemConfig::getConfigGroup, group)
                .orderByAsc(SystemConfig::getConfigGroup, SystemConfig::getId));
        list.forEach(c -> {
            if ("SECRET".equals(c.getValueType()) && StringUtils.hasText(c.getConfigValue())) {
                c.setConfigValue(mask(c.getConfigValue()));
            }
        });
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateValues(Map<String, String> values) {
        values.forEach((key, value) -> {
            SystemConfig config = getOne(new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key), false);
            if (config == null) {
                throw new BusinessException("配置项不存在：" + key);
            }
            // 前端回显的脱敏值原样提交时不覆盖
            if (value != null && value.contains(MASK)) {
                return;
            }
            SystemConfig update = new SystemConfig();
            update.setId(config.getId());
            update.setConfigValue(value == null ? "" : value.trim());
            updateById(update);
        });
    }

    @Override
    public LlmSettings llmSettings() {
        String baseUrl = getValue("llm.base_url", llmProperties.getBaseUrl());
        String apiKey = getValue("llm.api_key", llmProperties.getApiKey());
        String model = getValue("llm.model", llmProperties.getModel());
        double temperature;
        try {
            temperature = Double.parseDouble(getValue("llm.temperature", String.valueOf(llmProperties.getTemperature())));
        } catch (NumberFormatException e) {
            temperature = llmProperties.getTemperature();
        }
        int maxTokens = getInt("llm.max_tokens", llmProperties.getMaxTokens());
        return new LlmSettings(baseUrl, apiKey, model, temperature, maxTokens);
    }

    /** 保留前4后4位 */
    private String mask(String secret) {
        if (secret.length() <= 8) {
            return MASK;
        }
        return secret.substring(0, 4) + MASK + secret.substring(secret.length() - 4);
    }
}
