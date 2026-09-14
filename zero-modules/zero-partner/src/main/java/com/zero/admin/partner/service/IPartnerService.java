package com.zero.admin.partner.service;

import com.zero.admin.base.mybatis.core.page.PageQuery;
import com.zero.admin.base.mybatis.core.page.TableDataInfo;
import com.zero.admin.partner.domain.bo.PartnerBo;
import com.zero.admin.partner.domain.vo.PartnerVo;

/** 合作客户服务。 */
public interface IPartnerService {

    TableDataInfo<PartnerVo> queryPageList(PartnerBo bo, PageQuery pageQuery);

    PartnerVo queryById(Long partnerId);

    int insertPartner(PartnerBo bo);

    int updatePartner(PartnerBo bo);

    int deletePartnerByIds(Long[] partnerIds);
}
