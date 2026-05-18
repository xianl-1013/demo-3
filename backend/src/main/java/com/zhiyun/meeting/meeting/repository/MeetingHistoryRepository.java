package com.zhiyun.meeting.meeting.repository;

import com.zhiyun.meeting.meeting.vo.MeetingHistoryResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 会议历史 Repository
 *
 * 重点：
 * 同一个 meeting_id 只返回一条历史记录。
 */
@Repository
public class MeetingHistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MeetingHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 查询会议历史列表
     *
     * 使用 ROW_NUMBER 按 meeting_id 去重：
     * 同一个会议只保留一条。
     */
    public List<MeetingHistoryResponse> findHistoryList(Long userId,
                                                        String type,
                                                        Integer pageNum,
                                                        Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;

        StringBuilder whereSql = new StringBuilder();
        whereSql.append(" WHERE m.user_id = ? ");

        List<Object> params = new ArrayList<>();
        params.add(userId);

        if ("created".equals(type)) {
            whereSql.append(" AND r.host_user_id = ? ");
            params.add(userId);
        } else if ("joined".equals(type)) {
            whereSql.append(" AND r.host_user_id <> ? ");
            params.add(userId);
        }

        String sql = """
                SELECT
                    t.meeting_id,
                    t.meeting_no,
                    t.room_id,
                    t.title,
                    t.status,
                    t.host_user_id,
                    t.my_role,
                    t.host,
                    t.online,
                    t.start_time,
                    t.end_time,
                    t.joined_at,
                    t.left_at
                FROM (
                    SELECT
                        r.meeting_id,
                        r.meeting_no,
                        r.room_id,
                        r.title,
                        r.status,
                        r.host_user_id,
                        m.role AS my_role,
                        CASE WHEN r.host_user_id = m.user_id THEN 1 ELSE 0 END AS host,
                        m.online,
                        r.start_time,
                        r.end_time,
                        m.joined_at,
                        m.left_at,
                        ROW_NUMBER() OVER (
                            PARTITION BY r.meeting_id
                            ORDER BY 
                                CASE WHEN m.online = 1 THEN 0 ELSE 1 END,
                                m.joined_at DESC,
                                m.id DESC
                        ) AS rn
                    FROM meeting_room r
                    INNER JOIN meeting_member m ON r.meeting_id = m.meeting_id
                """ + whereSql + """
                ) t
                WHERE t.rn = 1
                ORDER BY t.start_time DESC
                LIMIT ? OFFSET ?
                """;

        params.add(pageSize);
        params.add(offset);

        return jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) -> {
            MeetingHistoryResponse item = new MeetingHistoryResponse();

            item.setMeetingId(rs.getString("meeting_id"));
            item.setMeetingNo(rs.getString("meeting_no"));
            item.setRoomId(rs.getString("room_id"));
            item.setTitle(rs.getString("title"));
            item.setStatus(rs.getString("status"));
            item.setHostUserId(rs.getLong("host_user_id"));
            item.setMyRole(rs.getString("my_role"));
            item.setHost(rs.getInt("host") == 1);
            item.setOnline(rs.getInt("online") == 1);
            item.setStartTime(formatDateTime(rs.getTimestamp("start_time")));
            item.setEndTime(formatDateTime(rs.getTimestamp("end_time")));
            item.setJoinedAt(formatDateTime(rs.getTimestamp("joined_at")));
            item.setLeftAt(formatDateTime(rs.getTimestamp("left_at")));

            return item;
        });
    }

    /**
     * 查询会议历史总数
     *
     * 必须 COUNT DISTINCT meeting_id。
     * 否则 meeting_member 有重复记录时，总数也会重复。
     */
    public Long countHistory(Long userId, String type) {
        StringBuilder whereSql = new StringBuilder();
        whereSql.append(" WHERE m.user_id = ? ");

        List<Object> params = new ArrayList<>();
        params.add(userId);

        if ("created".equals(type)) {
            whereSql.append(" AND r.host_user_id = ? ");
            params.add(userId);
        } else if ("joined".equals(type)) {
            whereSql.append(" AND r.host_user_id <> ? ");
            params.add(userId);
        }

        String sql = """
                SELECT COUNT(DISTINCT r.meeting_id)
                FROM meeting_room r
                INNER JOIN meeting_member m ON r.meeting_id = m.meeting_id
                """ + whereSql;

        Long count = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                params.toArray()
        );

        return count == null ? 0L : count;
    }

    private String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }

        return timestamp.toLocalDateTime().format(DATE_TIME_FORMATTER);
    }
}