package com.zhiyun.meeting.meeting.service;

import com.zhiyun.meeting.common.exception.BusinessException;
import com.zhiyun.meeting.common.result.PageResult;
import com.zhiyun.meeting.common.result.ResultCode;
import com.zhiyun.meeting.meeting.repository.MeetingHistoryRepository;
import com.zhiyun.meeting.meeting.vo.MeetingHistoryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会议历史 Service
 */
@Service
public class MeetingHistoryService {

    private final MeetingHistoryRepository meetingHistoryRepository;

    public MeetingHistoryService(MeetingHistoryRepository meetingHistoryRepository) {
        this.meetingHistoryRepository = meetingHistoryRepository;
    }

    /**
     * 查询当前登录用户的会议历史
     *
     * type:
     * all：全部
     * created：我创建的
     * joined：我加入的，不包含我创建的
     */
    public PageResult<MeetingHistoryResponse> getMyMeetingHistory(Long currentUserId,
                                                                  String type,
                                                                  Integer pageNum,
                                                                  Integer pageSize) {
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未获取到当前登录用户");
        }

        String queryType = normalizeType(type);

        int finalPageNum = pageNum == null || pageNum <= 0 ? 1 : pageNum;
        int finalPageSize = pageSize == null || pageSize <= 0 ? 20 : pageSize;

        List<MeetingHistoryResponse> rows =
                meetingHistoryRepository.findHistoryList(
                        currentUserId,
                        queryType,
                        finalPageNum,
                        finalPageSize
                );

        Long total =
                meetingHistoryRepository.countHistory(
                        currentUserId,
                        queryType
                );

        PageResult<MeetingHistoryResponse> pageResult = new PageResult<>();
        pageResult.setPageNum(finalPageNum);
        pageResult.setPageSize(finalPageSize);
        pageResult.setTotal(total);
        pageResult.setRows(rows);

        return pageResult;
    }

    private String normalizeType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return "all";
        }

        String cleanType = type.trim();

        if ("created".equals(cleanType)) {
            return "created";
        }

        if ("joined".equals(cleanType)) {
            return "joined";
        }

        return "all";
    }
}