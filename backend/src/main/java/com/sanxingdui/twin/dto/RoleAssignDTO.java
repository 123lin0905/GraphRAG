package com.sanxingdui.twin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 为用户分配角色
 */
@Data
@Schema(description = "分配角色请求")
public class RoleAssignDTO {

    @Schema(description = "角色ID列表")
    @NotEmpty(message = "角色不能为空")
    private List<Long> roleIds;
}
