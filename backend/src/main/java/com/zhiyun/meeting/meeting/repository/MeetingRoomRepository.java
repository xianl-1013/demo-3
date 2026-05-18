package com.zhiyun.meeting.meeting.repository;

import com.zhiyun.meeting.meeting.entity.MeetingRoom;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MeetingRoomRepository {

    private final JdbcTemplate jdbcTemplate;

    public MeetingRoomRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int save(MeetingRoom room) {
        String sql = """
                INSERT INTO meeting_room
                (meeting_id, meeting_no, room_id, title, password, host_user_id, status, start_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                room.getMeetingId(),
                room.getMeetingNo(),
                room.getRoomId(),
                room.getTitle(),
                room.getPassword(),
                room.getHostUserId(),
                room.getStatus(),
                room.getStartTime()
        );
    }

    public boolean existsByMeetingNo(String meetingNo) {
        String sql = "SELECT COUNT(1) FROM meeting_room WHERE meeting_no = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, meetingNo);
        return count != null && count > 0;
    }

    public MeetingRoom findByMeetingNo(String meetingNo) {
        String sql = """
                SELECT id, meeting_id, meeting_no, room_id, title, password,
                       host_user_id, status, start_time, end_time, create_time
                FROM meeting_room
                WHERE meeting_no = ?
                LIMIT 1
                """;

        List<MeetingRoom> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            MeetingRoom room = new MeetingRoom();
            room.setId(rs.getLong("id"));
            room.setMeetingId(rs.getString("meeting_id"));
            room.setMeetingNo(rs.getString("meeting_no"));
            room.setRoomId(rs.getString("room_id"));
            room.setTitle(rs.getString("title"));
            room.setPassword(rs.getString("password"));
            room.setHostUserId(rs.getLong("host_user_id"));
            room.setStatus(rs.getString("status"));

            if (rs.getTimestamp("start_time") != null) {
                room.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
            }

            if (rs.getTimestamp("end_time") != null) {
                room.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
            }

            if (rs.getTimestamp("create_time") != null) {
                room.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
            }

            return room;
        }, meetingNo);

        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }
    public MeetingRoom findByMeetingId(String meetingId) {
        String sql = """
            SELECT id, meeting_id, meeting_no, room_id, title, password,
                   host_user_id, status, start_time, end_time, create_time
            FROM meeting_room
            WHERE meeting_id = ?
            LIMIT 1
            """;

        List<MeetingRoom> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            MeetingRoom room = new MeetingRoom();
            room.setId(rs.getLong("id"));
            room.setMeetingId(rs.getString("meeting_id"));
            room.setMeetingNo(rs.getString("meeting_no"));
            room.setRoomId(rs.getString("room_id"));
            room.setTitle(rs.getString("title"));
            room.setPassword(rs.getString("password"));
            room.setHostUserId(rs.getLong("host_user_id"));
            room.setStatus(rs.getString("status"));

            if (rs.getTimestamp("start_time") != null) {
                room.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
            }

            if (rs.getTimestamp("end_time") != null) {
                room.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
            }

            if (rs.getTimestamp("create_time") != null) {
                room.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
            }

            return room;
        }, meetingId);

        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }

    public int endMeeting(String meetingId) {
        String sql = """
            UPDATE meeting_room
            SET status = 'ended',
                end_time = NOW()
            WHERE meeting_id = ?
            """;

        return jdbcTemplate.update(sql, meetingId);
    }
}