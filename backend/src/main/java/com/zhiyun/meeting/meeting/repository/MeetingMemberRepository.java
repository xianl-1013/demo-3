package com.zhiyun.meeting.meeting.repository;

import com.zhiyun.meeting.meeting.entity.MeetingMember;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MeetingMemberRepository {

    private final JdbcTemplate jdbcTemplate;
    /**
     * 更新会议成员在线状态
     *
     * 用途：
     * 1. WebSocket 连接成功时，设置 online = 1
     * 2. WebSocket 真正断开时，设置 online = 0
     *
     * @param meetingId 会议ID
     * @param userId 用户ID
     * @param online 是否在线
     * @return 影响行数
     */
    public int updateOnlineStatus(String meetingId, Long userId, Boolean online) {
        if (Boolean.TRUE.equals(online)) {
            String sql = """
                UPDATE meeting_member
                SET online = 1,
                    left_at = NULL
                WHERE meeting_id = ?
                  AND user_id = ?
                """;

            return jdbcTemplate.update(sql, meetingId, userId);
        }

        String sql = """
            UPDATE meeting_member
            SET online = 0,
                left_at = NOW()
            WHERE meeting_id = ?
              AND user_id = ?
            """;

        return jdbcTemplate.update(sql, meetingId, userId);
    }
    /**
     * 更新成员麦克风状态
     *
     * @param meetingId 会议ID
     * @param userId 用户ID
     * @param micOn 是否开启麦克风
     * @return 影响行数
     */
    public int updateMicStatus(String meetingId, Long userId, Boolean micOn) {
        String sql = """
            UPDATE meeting_member
            SET mic_on = ?
            WHERE meeting_id = ?
              AND user_id = ?
            """;

        return jdbcTemplate.update(
                sql,
                Boolean.TRUE.equals(micOn) ? 1 : 0,
                meetingId,
                userId
        );
    }

    /**
     * 更新成员摄像头状态
     *
     * @param meetingId 会议ID
     * @param userId 用户ID
     * @param cameraOn 是否开启摄像头
     * @return 影响行数
     */
    public int updateCameraStatus(String meetingId, Long userId, Boolean cameraOn) {
        String sql = """
            UPDATE meeting_member
            SET camera_on = ?
            WHERE meeting_id = ?
              AND user_id = ?
            """;

        return jdbcTemplate.update(
                sql,
                Boolean.TRUE.equals(cameraOn) ? 1 : 0,
                meetingId,
                userId
        );
    }

    public MeetingMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    /**
     * 根据会议ID和用户ID查询会议成员
     *
     * 用途：
     * 1. 判断某个用户是否在会议中
     * 2. 判断目标成员是否存在
     */
    public MeetingMember findByMeetingIdAndUserId(String meetingId, Long userId) {
        String sql = """
            SELECT id, meeting_id, meeting_no, room_id, user_id, user_name, avatar,
                   role, mic_on, camera_on, online, joined_at, left_at, create_time
            FROM meeting_member
            WHERE meeting_id = ?
              AND user_id = ?
            LIMIT 1
            """;

        List<MeetingMember> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            MeetingMember member = new MeetingMember();
            member.setId(rs.getLong("id"));
            member.setMeetingId(rs.getString("meeting_id"));
            member.setMeetingNo(rs.getString("meeting_no"));
            member.setRoomId(rs.getString("room_id"));
            member.setUserId(rs.getLong("user_id"));
            member.setUserName(rs.getString("user_name"));
            member.setAvatar(rs.getString("avatar"));
            member.setRole(rs.getString("role"));
            member.setMicOn(rs.getInt("mic_on") == 1);
            member.setCameraOn(rs.getInt("camera_on") == 1);
            member.setOnline(rs.getInt("online") == 1);

            if (rs.getTimestamp("joined_at") != null) {
                member.setJoinedAt(rs.getTimestamp("joined_at").toLocalDateTime());
            }

            if (rs.getTimestamp("left_at") != null) {
                member.setLeftAt(rs.getTimestamp("left_at").toLocalDateTime());
            }

            if (rs.getTimestamp("create_time") != null) {
                member.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
            }

            return member;
        }, meetingId, userId);

        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }

    /**
     * 全体静音
     *
     * 这里默认不静音主持人自己，只静音其他在线成员。
     *
     * @param meetingId      会议ID
     * @param operatorUserId 操作人用户ID，也就是主持人ID
     * @param micOn          麦克风状态，false 表示关闭
     * @return 影响行数
     */
    public int updateAllMicStatusExceptOperator(String meetingId, Long operatorUserId, Boolean micOn) {
        String sql = """
            UPDATE meeting_member
            SET mic_on = ?
            WHERE meeting_id = ?
              AND online = 1
              AND user_id <> ?
            """;

        return jdbcTemplate.update(
                sql,
                Boolean.TRUE.equals(micOn) ? 1 : 0,
                meetingId,
                operatorUserId
        );
    }

    /**
     * 保存或更新会议成员
     *
     * 同一个 meeting_id + user_id 只能有一条记录。
     * 如果已存在：更新在线状态、麦克风、摄像头、角色等。
     * 如果不存在：插入新记录。
     */
    public void saveOrUpdate(MeetingMember member) {
        String countSql = """
        SELECT COUNT(1)
        FROM meeting_member
        WHERE meeting_id = ?
          AND user_id = ?
        """;

        Integer count = jdbcTemplate.queryForObject(
                countSql,
                Integer.class,
                member.getMeetingId(),
                member.getUserId()
        );

        if (count != null && count > 0) {
            String updateSql = """
            UPDATE meeting_member
            SET meeting_no = ?,
                room_id = ?,
                user_name = ?,
                avatar = ?,
                role = ?,
                mic_on = ?,
                camera_on = ?,
                online = 1,
                joined_at = NOW(),
                left_at = NULL
            WHERE meeting_id = ?
              AND user_id = ?
            """;

            jdbcTemplate.update(
                    updateSql,
                    member.getMeetingNo(),
                    member.getRoomId(),
                    member.getUserName(),
                    member.getAvatar(),
                    member.getRole(),
                    Boolean.TRUE.equals(member.getMicOn()) ? 1 : 0,
                    Boolean.TRUE.equals(member.getCameraOn()) ? 1 : 0,
                    member.getMeetingId(),
                    member.getUserId()
            );
            return;
        }

        String insertSql = """
        INSERT INTO meeting_member (
            meeting_id,
            meeting_no,
            room_id,
            user_id,
            user_name,
            avatar,
            role,
            mic_on,
            camera_on,
            online,
            joined_at,
            left_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, NOW(), NULL)
        """;

        jdbcTemplate.update(
                insertSql,
                member.getMeetingId(),
                member.getMeetingNo(),
                member.getRoomId(),
                member.getUserId(),
                member.getUserName(),
                member.getAvatar(),
                member.getRole(),
                Boolean.TRUE.equals(member.getMicOn()) ? 1 : 0,
                Boolean.TRUE.equals(member.getCameraOn()) ? 1 : 0
        );
    }

    public int leaveMeeting(String meetingId, Long userId) {
        String sql = """
            UPDATE meeting_member
            SET online = 0,
                left_at = NOW()
            WHERE meeting_id = ?
              AND user_id = ?
            """;

        return jdbcTemplate.update(sql, meetingId, userId);
    }

    public int offlineAllByMeetingId(String meetingId) {
        String sql = """
                UPDATE meeting_member
                SET online = 0,
                    left_at = NOW()
                WHERE meeting_id = ?
                """;

        return jdbcTemplate.update(sql, meetingId);
    }

    public List<MeetingMember> findOnlineListByMeetingId(String meetingId) {
        String sql = """
                SELECT id, meeting_id, meeting_no, room_id, user_id, user_name, avatar,
                       role, mic_on, camera_on, online, joined_at, left_at, create_time
                FROM meeting_member
                WHERE meeting_id = ?
                  AND online = 1
                ORDER BY 
                    CASE WHEN role = 'host' THEN 0 ELSE 1 END,
                    joined_at ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            MeetingMember member = new MeetingMember();
            member.setId(rs.getLong("id"));
            member.setMeetingId(rs.getString("meeting_id"));
            member.setMeetingNo(rs.getString("meeting_no"));
            member.setRoomId(rs.getString("room_id"));
            member.setUserId(rs.getLong("user_id"));
            member.setUserName(rs.getString("user_name"));
            member.setAvatar(rs.getString("avatar"));
            member.setRole(rs.getString("role"));
            member.setMicOn(rs.getInt("mic_on") == 1);
            member.setCameraOn(rs.getInt("camera_on") == 1);
            member.setOnline(rs.getInt("online") == 1);

            if (rs.getTimestamp("joined_at") != null) {
                member.setJoinedAt(rs.getTimestamp("joined_at").toLocalDateTime());
            }

            if (rs.getTimestamp("left_at") != null) {
                member.setLeftAt(rs.getTimestamp("left_at").toLocalDateTime());
            }

            if (rs.getTimestamp("create_time") != null) {
                member.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
            }

            return member;
        }, meetingId);
    }
    public boolean existsMember(String meetingId, Long userId) {
        String sql = """
            SELECT COUNT(1)
            FROM meeting_member
            WHERE meeting_id = ?
              AND user_id = ?
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, meetingId, userId);

        return count != null && count > 0;
    }

    public int updateJoinStatus(String meetingId, Long userId, Boolean micOn, Boolean cameraOn) {
        String sql = """
            UPDATE meeting_member
            SET online = 1,
                mic_on = ?,
                camera_on = ?,
                left_at = NULL
            WHERE meeting_id = ?
              AND user_id = ?
            """;

        return jdbcTemplate.update(
                sql,
                Boolean.TRUE.equals(micOn) ? 1 : 0,
                Boolean.TRUE.equals(cameraOn) ? 1 : 0,
                meetingId,
                userId
        );
    }
}