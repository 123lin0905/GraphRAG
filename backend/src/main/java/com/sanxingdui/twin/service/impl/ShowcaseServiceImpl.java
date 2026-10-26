package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.Showcase;
import com.sanxingdui.twin.mapper.ShowcaseMapper;
import com.sanxingdui.twin.service.ShowcaseService;
import org.springframework.stereotype.Service;

/**
 * 展柜表 服务实现
 */
@Service
public class ShowcaseServiceImpl extends ServiceImpl<ShowcaseMapper, Showcase> implements ShowcaseService {
}
