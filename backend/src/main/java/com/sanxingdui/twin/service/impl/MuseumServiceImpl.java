package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.Museum;
import com.sanxingdui.twin.mapper.MuseumMapper;
import com.sanxingdui.twin.service.MuseumService;
import org.springframework.stereotype.Service;

/**
 * 博物馆表 服务实现
 */
@Service
public class MuseumServiceImpl extends ServiceImpl<MuseumMapper, Museum> implements MuseumService {
}
