package com.example.admin.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页结果封装类
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页数据列表
     */
    private T records;

    public PageResult() {
    }

    public PageResult(Long total, T records) {
        this.total = total;
        this.records = records;
    }

    public static <T> PageResult<T> of(Long total, T records) {
        return new PageResult<>(total, records);
    }
}
