package com.zero.admin.member.domain.bo;

import com.zero.admin.base.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 后台会员查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberQueryBo extends BaseEntity {

    private Long memberId;

    private String username;

    private String nickname;

    private String mobile;

    private String status;
}
