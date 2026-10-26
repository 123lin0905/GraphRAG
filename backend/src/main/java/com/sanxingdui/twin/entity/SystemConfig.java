package com.sanxingdui.twin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sanxingdui.twin.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统参数配置表（system_config）
 *
 * @author sanxingdui-twin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_config")
@Schema(description = "系统参数配置表")
public class SystemConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 配置键（如 llm.api_key） */
    @Schema(description = "配置键（如 llm.api_key）")
    private String configKey;

    /** 配置值 */
    @Schema(description = "配置值")
    private String configValue;

    /** 配置分组：LLM/RAG/RESERVATION/SYSTEM */
    @Schema(description = "配置分组：LLM/RAG/RESERVATION/SYSTEM")
    private String configGroup;

    /** 值类型：STRING/NUMBER/BOOLEAN/SECRET */
    @Schema(description = "值类型：STRING/NUMBER/BOOLEAN/SECRET")
    private String valueType;

    /** 配置说明 */
    @Schema(description = "配置说明")
    private String description;
}
