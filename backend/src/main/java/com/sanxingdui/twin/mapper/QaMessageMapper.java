package com.sanxingdui.twin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sanxingdui.twin.entity.QaMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问答消息表 Mapper
 */
@Mapper
public interface QaMessageMapper extends BaseMapper<QaMessage> {
}
