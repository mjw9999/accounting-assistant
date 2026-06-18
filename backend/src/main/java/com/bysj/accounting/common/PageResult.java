package com.bysj.accounting.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 统一分页响应结构，替代直接返回 Page<T>（避免 PageImpl 序列化警告）
 */
public class PageResult<T> {

    /** 当前页数据列表 */
    public List<T> records;

    /** 总记录数 */
    public long total;

    /** 当前页码（从 1 开始） */
    public int pageNum;

    /** 每页条数 */
    public int pageSize;

    /** 总页数 */
    public int totalPages;

    public static <T> PageResult<T> of(Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.records = page.getContent();
        result.total = page.getTotalElements();
        result.pageNum = page.getNumber() + 1;   // Spring Page 从 0 开始，对外从 1 开始
        result.pageSize = page.getSize();
        result.totalPages = page.getTotalPages();
        return result;
    }
}
