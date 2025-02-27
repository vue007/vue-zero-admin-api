package com.zero.admin.base.mybatis.core.page;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageData<T> implements Serializable {
    private long total;
    private List<T> rows;
    private static final long serialVersionUID = 1L;

    public PageData(List<T> list, long total) {
        this.rows = list;
        this.total = total;
    }
    public PageData() {
    }
    public long getTotal() {
        return total;
    }
    public void setTotal(long total) {
        this.total = total;
    }
    public List<T> getRows() {
        return rows;
    }
    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }
}
