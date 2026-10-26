package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.ArtifactCategory;
import com.sanxingdui.twin.mapper.ArtifactCategoryMapper;
import com.sanxingdui.twin.service.ArtifactCategoryService;
import org.springframework.stereotype.Service;

/**
 * 文物分类表 服务实现
 */
@Service
public class ArtifactCategoryServiceImpl extends ServiceImpl<ArtifactCategoryMapper, ArtifactCategory> implements ArtifactCategoryService {
}
