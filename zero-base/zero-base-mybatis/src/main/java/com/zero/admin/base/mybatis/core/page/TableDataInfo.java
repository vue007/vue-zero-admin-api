package com.zero.admin.base.mybatis.core.page;

import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zero.admin.base.core.domain.R;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 表格分页数据对象
 *
 * @author Akai
 */
@Data
@NoArgsConstructor
public class TableDataInfo<T> extends R<PageData<T>> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分页数据
     */
    private PageData<T> data;

    /**
     * 消息状态码
     */
    private int code;

    /**
     * 消息内容
     */
    private String msg;

    /**
     * 分页
     *
     * @param list  列表数据
     * @param total 总记录数
     */
    public TableDataInfo(List<T> list, long total) {
        this.data = new PageData<>(list, total);
        this.code = HttpStatus.HTTP_OK;
        this.msg = "查询成功";
    }

    /**
     * 根据分页对象构建表格分页数据对象
     */
    public static <T> TableDataInfo<T> build(IPage<T> page) {
        TableDataInfo<T> rspData = new TableDataInfo<>();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setMsg("查询成功");
        rspData.data = new PageData<>(page.getRecords(), page.getTotal());
        return rspData;
    }

    /**
     * 根据数据列表构建表格分页数据对象
     */
    public static <T> TableDataInfo<T> build(List<T> list) {
        TableDataInfo<T> rspData = new TableDataInfo<>();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setMsg("查询成功");
        rspData.data = new PageData<>(list, list.size());
        return rspData;
    }

    /**
     * 构建表格分页数据对象
     */
    public static <T> TableDataInfo<T> build() {
        TableDataInfo<T> rspData = new TableDataInfo<>();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setMsg("查询成功");
        return rspData;
    }

}
