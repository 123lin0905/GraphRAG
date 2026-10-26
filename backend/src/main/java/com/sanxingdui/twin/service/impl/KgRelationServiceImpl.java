package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.KgRelation;
import com.sanxingdui.twin.mapper.KgRelationMapper;
import com.sanxingdui.twin.service.KgRelationService;
import org.springframework.stereotype.Service;

/**
 * 知识图谱关系表 服务实现
 */
@Service
public class KgRelationServiceImpl extends ServiceImpl<KgRelationMapper, KgRelation> implements KgRelationService {
}
