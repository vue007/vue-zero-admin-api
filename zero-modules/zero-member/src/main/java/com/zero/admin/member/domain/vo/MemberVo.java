package com.zero.admin.member.domain.vo;

import com.zero.admin.member.domain.Member;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/** 会员视图，不暴露密码摘要。 */
@Data
@AutoMapper(target = Member.class)
public class MemberVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long memberId;
    private String tenantId;
    private String username;
    private String nickname;
    private String mobile;
    private String email;
    private String avatar;
    private String status;
    private String registerSource;
    private String loginIp;
    private Date loginDate;
    private Date createTime;
    private String remark;
}
