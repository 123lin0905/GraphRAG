package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.Model3d;
import com.sanxingdui.twin.mapper.Model3dMapper;
import com.sanxingdui.twin.service.Model3dService;
import org.springframework.stereotype.Service;

/**
 * 三维模型表 服务实现
 */
@Service
public class Model3dServiceImpl extends ServiceImpl<Model3dMapper, Model3d> implements Model3dService {
}
