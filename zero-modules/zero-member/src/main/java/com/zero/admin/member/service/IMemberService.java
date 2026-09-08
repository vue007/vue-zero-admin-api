package com.zero.admin.member.service;

import com.zero.admin.base.mybatis.core.page.PageQuery;
import com.zero.admin.base.mybatis.core.page.TableDataInfo;
import com.zero.admin.member.domain.bo.MemberQueryBo;
import com.zero.admin.member.domain.bo.MemberRegisterBo;
import com.zero.admin.member.domain.model.MemberSocialIdentity;
import com.zero.admin.member.domain.vo.MemberVo;

/** 会员领域服务。 */
public interface IMemberService {

    MemberVo register(String tenantId, MemberRegisterBo bo);

    MemberVo authenticatePassword(String tenantId, String username, String rawPassword);

    MemberVo loginOrRegisterSocial(String tenantId, MemberSocialIdentity identity);

    void recordLogin(String tenantId, Long memberId, String ip);

    MemberVo queryById(Long memberId);

    TableDataInfo<MemberVo> queryPageList(MemberQueryBo bo, PageQuery pageQuery);

    boolean updateStatus(Long memberId, String status);
}
