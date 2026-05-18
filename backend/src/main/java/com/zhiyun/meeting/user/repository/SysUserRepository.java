package com.zhiyun.meeting.user.repository;

import com.zhiyun.meeting.user.entity.SysUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
// 专门负责查询 sys_user 表
@Repository
public class SysUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public SysUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SysUser findByUsername(String username) {
        String sql = """
                SELECT id, username, password, user_name, avatar, phone, create_time
                FROM sys_user
                WHERE username = ?
                LIMIT 1
                """;

        List<SysUser> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            SysUser user = new SysUser();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setUserName(rs.getString("user_name"));
            user.setAvatar(rs.getString("avatar"));
            user.setPhone(rs.getString("phone"));

            if (rs.getTimestamp("create_time") != null) {
                user.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
            }

            return user;
        }, username);

        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }
    // 增加根据 ID 查询用户
    public SysUser findById(Long id) {
        String sql = """
            SELECT id, username, password, user_name, avatar, phone, create_time
            FROM sys_user
            WHERE id = ?
            LIMIT 1
            """;

        List<SysUser> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            SysUser user = new SysUser();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setUserName(rs.getString("user_name"));
            user.setAvatar(rs.getString("avatar"));
            user.setPhone(rs.getString("phone"));

            if (rs.getTimestamp("create_time") != null) {
                user.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
            }

            return user;
        }, id);

        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }
}