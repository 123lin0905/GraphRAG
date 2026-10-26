package com.sanxingdui.twin.mapper;

import com.sanxingdui.twin.vo.NameValueVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 数据大屏统计查询（聚合 SQL）
 */
@Mapper
public interface DashboardMapper {

    /** 按一级分类统计文物数量（子分类归并到父分类） */
    @Select("SELECT COALESCE(p.name, c.name) AS name, COUNT(a.id) AS value "
            + "FROM artifact a JOIN artifact_category c ON c.id = a.category_id "
            + "LEFT JOIN artifact_category p ON p.id = c.parent_id AND c.parent_id <> 0 "
            + "WHERE a.deleted = 0 GROUP BY COALESCE(p.name, c.name) ORDER BY value DESC")
    List<NameValueVO> countByTopCategory();

    /** 按细分类统计 */
    @Select("SELECT c.name AS name, COUNT(a.id) AS value FROM artifact a JOIN artifact_category c ON c.id = a.category_id "
            + "WHERE a.deleted = 0 GROUP BY c.name ORDER BY value DESC")
    List<NameValueVO> countByCategory();

    /** 按祭祀坑统计（包含没有文物的坑） */
    @Select("SELECT k.name AS name, COUNT(a.id) AS value FROM excavation_pit k "
            + "LEFT JOIN artifact a ON a.pit_id = k.id AND a.deleted = 0 WHERE k.deleted = 0 "
            + "GROUP BY k.id, k.name ORDER BY k.id")
    List<NameValueVO> countByPit();

    /** 按材质统计 */
    @Select("SELECT m.name AS name, COUNT(a.id) AS value FROM material m "
            + "JOIN artifact a ON a.material_id = m.id AND a.deleted = 0 WHERE m.deleted = 0 "
            + "GROUP BY m.id, m.name ORDER BY value DESC")
    List<NameValueVO> countByMaterial();

    /** 按年代统计 */
    @Select("SELECT p.name AS name, COUNT(a.id) AS value FROM dynasty_period p "
            + "JOIN artifact a ON a.period_id = p.id AND a.deleted = 0 WHERE p.deleted = 0 "
            + "GROUP BY p.id, p.name ORDER BY p.sort")
    List<NameValueVO> countByPeriod();

    /** 按展厅统计 */
    @Select("SELECT h.name AS name, COUNT(a.id) AS value FROM exhibition_hall h "
            + "LEFT JOIN artifact a ON a.hall_id = h.id AND a.deleted = 0 WHERE h.deleted = 0 "
            + "GROUP BY h.id, h.name ORDER BY h.sort")
    List<NameValueVO> countByHall();

    /** 3DGS 模型格式分布 */
    @Select("SELECT format AS name, COUNT(*) AS value FROM model_3d WHERE deleted = 0 GROUP BY format ORDER BY value DESC")
    List<NameValueVO> countModelByFormat();

    /** 3DGS 模型质量（PSNR/SSIM/LPIPS） */
    @Select("SELECT name, format, psnr, ssim, lpips, gaussian_count AS gaussianCount, file_size AS fileSize "
            + "FROM model_3d WHERE deleted = 0 AND psnr IS NOT NULL ORDER BY psnr DESC")
    List<Map<String, Object>> modelQuality();

    /** 最近 N 天每日统计 */
    @Select("SELECT DATE_FORMAT(stat_date, '%Y-%m-%d') AS statDate, offline_visitors AS offlineVisitors, "
            + "online_visits AS onlineVisits, model_views AS modelViews, qa_count AS qaCount, reservation_count AS reservationCount "
            + "FROM visit_statistics WHERE deleted = 0 AND stat_date >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) ORDER BY stat_date")
    List<Map<String, Object>> visitTrend(@Param("days") int days);

    /** 最近 N 天每日用户提问数（来自问答记录表） */
    @Select("SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS name, COUNT(*) AS value FROM qa_message "
            + "WHERE deleted = 0 AND role = 'user' AND created_at >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) "
            + "GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d') ORDER BY name")
    List<NameValueVO> qaDaily(@Param("days") int days);

    /** 问答回答模式分布（大模型 / 离线兜底） */
    @Select("SELECT IF(is_fallback = 1, '离线兜底', '大模型生成') AS name, COUNT(*) AS value FROM qa_message "
            + "WHERE deleted = 0 AND role = 'assistant' GROUP BY is_fallback")
    List<NameValueVO> qaModeDistribution();

    /** 问答平均耗时与 Token 消耗 */
    @Select("SELECT IFNULL(ROUND(AVG(latency_ms)), 0) AS avgLatencyMs, IFNULL(SUM(total_tokens), 0) AS totalTokens, "
            + "SUM(feedback = 1) AS likes, SUM(feedback = -1) AS dislikes "
            + "FROM qa_message WHERE deleted = 0 AND role = 'assistant'")
    Map<String, Object> qaPerformance();

    /** 评分分布 */
    @Select("SELECT CONCAT(rating, '星') AS name, COUNT(*) AS value FROM review WHERE deleted = 0 AND status = 1 "
            + "GROUP BY rating ORDER BY rating DESC")
    List<NameValueVO> ratingDistribution();

    /** 预约状态分布 */
    @Select("SELECT status AS name, COUNT(*) AS value FROM reservation WHERE deleted = 0 GROUP BY status")
    List<NameValueVO> reservationStatus();

    /** 未来 N 天每日预约人数 */
    @Select("SELECT DATE_FORMAT(visit_date, '%Y-%m-%d') AS name, SUM(visitor_count) AS value FROM reservation "
            + "WHERE deleted = 0 AND status IN ('PENDING','CONFIRMED') AND visit_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL #{days} DAY) "
            + "GROUP BY visit_date ORDER BY visit_date")
    List<NameValueVO> upcomingReservations(@Param("days") int days);

    /** 收藏最多的文物 */
    @Select("SELECT name, favorite_count AS value FROM artifact WHERE deleted = 0 ORDER BY favorite_count DESC, view_count DESC LIMIT #{limit}")
    List<NameValueVO> topFavorited(@Param("limit") int limit);

    /** 路线使用排行 */
    @Select("SELECT name, use_count AS value FROM guide_route WHERE deleted = 0 ORDER BY use_count DESC LIMIT #{limit}")
    List<NameValueVO> topRoutes(@Param("limit") int limit);
}
