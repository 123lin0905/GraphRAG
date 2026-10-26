package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.Visitor;
import com.sanxingdui.twin.mapper.VisitorMapper;
import com.sanxingdui.twin.service.VisitorService;
import org.springframework.stereotype.Service;

/**
 * 游客档案表 服务实现
 */
@Service
public class VisitorServiceImpl extends ServiceImpl<VisitorMapper, Visitor> implements VisitorService {
}
