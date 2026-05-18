package com.zhiyun.meeting.meeting.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 会议会控日志 Repository
 *
 * Repository 的职责：
 * 只负责和数据库打交道。
 * 不写业务判断，不判断是不是主持人。
 */
@Repository
public class MeetingControlLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public MeetingControlLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 保存会控日志
     *
     * @param controlId      会控操作ID
     * @param meetingId      会议ID
     * @param operatorUserId 操作人用户ID
     * @param targetUserId   被操作人用户ID，可以为空
     * @param event          会控事件
     * @param content        操作说明
     * @return 影响行数
     */
    public int save(String controlId,
                    String meetingId,
                    Long operatorUserId,
                    Long targetUserId,
                    String event,
                    String content) {
        String sql = """
                INSERT INTO meeting_control_log
                (control_id, meeting_id, operator_user_id, target_user_id, event, content)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                controlId,
                meetingId,
                operatorUserId,
                targetUserId,
                event,
                content
        );
    }
}