package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.ArtifactLiterature;
import com.sanxingdui.twin.mapper.ArtifactLiteratureMapper;
import com.sanxingdui.twin.service.ArtifactLiteratureService;
import org.springframework.stereotype.Service;

/**
 * 文物文献关联表 服务实现
 */
@Service
public class ArtifactLiteratureServiceImpl extends ServiceImpl<ArtifactLiteratureMapper, ArtifactLiterature> implements ArtifactLiteratureService {
}
