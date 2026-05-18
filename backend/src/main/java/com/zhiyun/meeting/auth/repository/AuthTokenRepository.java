package com.zhiyun.meeting.auth.repository;

import com.zhiyun.meeting.auth.vo.TokenInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 登录 token Repository
 *
 * Repository 只负责数据库操作：
 * 1. 保存 token
 * 2. 查询 token
 * 3. 删除用户旧 token
 */
@Repository
public class AuthTokenRepository {

    private final JdbcTemplate jdbcTemplate;

    public AuthTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 删除某个用户的旧 token
     *
     * 当前第一版策略：
     * 一个用户只保留一个有效 token。
     * 用户重新登录后，旧 token 失效。
     */
    public int deleteByUserId(Long userId) {
        String sql = """
                DELETE FROM sys_user_token
                WHERE user_id = ?
                """;

        return jdbcTemplate.update(sql, userId);
    }

    /**
     * 保存新 token
     *
     * @param userId        用户ID
     * @param token         token 字符串
     * @param expireHours   过期小时数
     * @return 影响行数
     */
    public int saveToken(Long userId, String token, Integer expireHours) {
        String sql = """
                INSERT INTO sys_user_token
                (user_id, token, expire_time)
                VALUES (?, ?, DATE_ADD(NOW(), INTERVAL ? HOUR))
                """;

        return jdbcTemplate.update(sql, userId, token, expireHours);
    }

    /**
     * 根据 token 查询有效 token 信息
     *
     * 注意：
     * expire_time > NOW() 表示 token 没有过期。
     */
    public TokenInfo findValidToken(String token) {
        String sql = """
                SELECT user_id, token, expire_time
                FROM sys_user_token
                WHERE token = ?
                  AND expire_time > NOW()
                LIMIT 1
                """;

        List<TokenInfo> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            TokenInfo info = new TokenInfo();
            info.setUserId(rs.getLong("user_id"));
            info.setToken(rs.getString("token"));

            if (rs.getTimestamp("expire_time") != null) {
                info.setExpireTime(rs.getTimestamp("expire_time").toLocalDateTime());
            }

            return info;
        }, token);

        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }
}