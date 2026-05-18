package com.zhiyun.meeting.common.result;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 分页返回对象
 *
 * 用于列表接口统一返回分页数据。
 *
 * @param <T> 列表数据类型
 */
@Schema(description = "分页返回对象")
public class PageResult<T> {

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", example = "1")
    private Integer pageNum;

    /**
     * 每页条数
     */
    @Schema(description = "每页条数", example = "20")
    private Integer pageSize;

    /**
     * 总条数
     */
    @Schema(description = "总条数", example = "100")
    private Long total;

    /**
     * 当前页数据
     */
    @Schema(description = "当前页数据")
    private List<T> rows;

    public PageResult() {
    }

    public PageResult(Integer pageNum, Integer pageSize, Long total, List<T> rows) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.rows = rows;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public Long getTotal() {
        return total;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }
}