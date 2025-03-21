package com.zero.admin.system.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import com.zero.admin.base.translation.annotation.Translation;
import com.zero.admin.base.translation.constant.TransConstant;
import com.zero.admin.system.domain.SysNotice;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 通知公告视图对象 sys_notice
 *
 * @author Akai
 */
@Data
@AutoMapper(target = SysNotice.class)
public class SysNoticeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 公告ID
     */
    private Long noticeId;

    /**
     * 公告标题
     */
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告）
     */
    private String noticeType;

    /**
     * 公告内容
     */
    private String noticeContent;

    /**
     * 公告状态（0正常 1关闭）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建者
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    @Translation(type = TransConstant.USER_ID_TO_NAME, mapper = "createBy")
    private String createByName;

    /**
     * 创建时间
     */
    private Date createTime;

}
