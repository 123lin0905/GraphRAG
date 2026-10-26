package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.KgEntity;
import com.sanxingdui.twin.mapper.KgEntityMapper;
import com.sanxingdui.twin.service.KgEntityService;
import org.springframework.stereotype.Service;

/**
 * 知识图谱实体表 服务实现
 */
@Service
public class KgEntityServiceImpl extends ServiceImpl<KgEntityMapper, KgEntity> implements KgEntityService {
}
