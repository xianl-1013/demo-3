package com.zhiyun.meeting.meeting.repository;

import com.zhiyun.meeting.meeting.entity.MeetingChatMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MeetingChatMessageRepository {

    private final JdbcTemplate jdbcTemplate;

    public MeetingChatMessageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int save(MeetingChatMessage message) {
        String sql = """
                INSERT INTO meeting_chat_message
                (message_id, meeting_id, meeting_no, room_id, from_user_id, from_user_name,
                 avatar, message_type, content, send_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                message.getMessageId(),
                message.getMeetingId(),
                message.getMeetingNo(),
                message.getRoomId(),
                message.getFromUserId(),
                message.getFromUserName(),
                message.getAvatar(),
                message.getMessageType(),
                message.getContent(),
                message.getSendTime()
        );
    }

    public List<MeetingChatMessage> findListByMeetingId(String meetingId, Integer pageNum, Integer pageSize) {
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : pageSize;
        int offset = (safePageNum - 1) * safePageSize;

        String sql = """
                SELECT id, message_id, meeting_id, meeting_no, room_id, from_user_id,
                       from_user_name, avatar, message_type, content, send_time, create_time
                FROM meeting_chat_message
                WHERE meeting_id = ?
                ORDER BY send_time ASC, id ASC
                LIMIT ? OFFSET ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            MeetingChatMessage message = new MeetingChatMessage();
            message.setId(rs.getLong("id"));
            message.setMessageId(rs.getString("message_id"));
            message.setMeetingId(rs.getString("meeting_id"));
            message.setMeetingNo(rs.getString("meeting_no"));
            message.setRoomId(rs.getString("room_id"));
            message.setFromUserId(rs.getLong("from_user_id"));
            message.setFromUserName(rs.getString("from_user_name"));
            message.setAvatar(rs.getString("avatar"));
            message.setMessageType(rs.getString("message_type"));
            message.setContent(rs.getString("content"));

            if (rs.getTimestamp("send_time") != null) {
                message.setSendTime(rs.getTimestamp("send_time").toLocalDateTime());
            }

            if (rs.getTimestamp("create_time") != null) {
                message.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
            }

            return message;
        }, meetingId, safePageSize, offset);
    }
}