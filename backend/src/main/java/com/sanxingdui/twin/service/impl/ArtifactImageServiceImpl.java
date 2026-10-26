package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.ArtifactImage;
import com.sanxingdui.twin.mapper.ArtifactImageMapper;
import com.sanxingdui.twin.service.ArtifactImageService;
import org.springframework.stereotype.Service;

/**
 * 文物图片表 服务实现
 */
@Service
public class ArtifactImageServiceImpl extends ServiceImpl<ArtifactImageMapper, ArtifactImage> implements ArtifactImageService {
}
