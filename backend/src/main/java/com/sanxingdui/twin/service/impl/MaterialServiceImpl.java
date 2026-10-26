package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.Material;
import com.sanxingdui.twin.mapper.MaterialMapper;
import com.sanxingdui.twin.service.MaterialService;
import org.springframework.stereotype.Service;

/**
 * 材质表 服务实现
 */
@Service
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper, Material> implements MaterialService {
}
