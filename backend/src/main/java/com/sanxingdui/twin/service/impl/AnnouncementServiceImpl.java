package com.sanxingdui.twin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sanxingdui.twin.entity.Announcement;
import com.sanxingdui.twin.mapper.AnnouncementMapper;
import com.sanxingdui.twin.service.AnnouncementService;
import org.springframework.stereotype.Service;

/**
 * 公告表 服务实现
 */
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {
}
