package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.Literature;
import com.sanxingdui.twin.mapper.LiteratureMapper;
import com.sanxingdui.twin.service.LiteratureService;
import org.springframework.stereotype.Service;

/**
 * 文献资料表 服务实现
 */
@Service
public class LiteratureServiceImpl extends ServiceImpl<LiteratureMapper, Literature> implements LiteratureService {
}
