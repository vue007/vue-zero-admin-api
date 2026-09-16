package com.zero.admin.partner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zero.admin.base.core.constant.SystemConstants;
import com.zero.admin.base.core.exception.ServiceException;
import com.zero.admin.base.core.utils.MapstructUtils;
import com.zero.admin.base.core.utils.StringUtils;
import com.zero.admin.base.mybatis.core.page.PageQuery;
import com.zero.admin.base.mybatis.core.page.TableDataInfo;
import com.zero.admin.partner.domain.Partner;
import com.zero.admin.partner.domain.bo.PartnerBo;
import com.zero.admin.partner.domain.vo.PartnerVo;
import com.zero.admin.partner.mapper.PartnerMapper;
import com.zero.admin.partner.service.IPartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;

/** 合作客户服务实现。 */
@Service
@RequiredArgsConstructor
public class PartnerServiceImpl implements IPartnerService {

    private final PartnerMapper partnerMapper;

    @Override
    public TableDataInfo<PartnerVo> queryPageList(PartnerBo bo, PageQuery pageQuery) {
        Page<PartnerVo> page = partnerMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public PartnerVo queryById(Long partnerId) {
        return partnerMapper.selectVoById(partnerId);
    }

    @Override
    public int insertPartner(PartnerBo bo) {
        normalize(bo);
        assertUnique(bo);
        Partner partner = MapstructUtils.convert(bo, Partner.class);
        if (partner == null) {
            throw new ServiceException("合作客户信息转换失败");
        }
        partner.setStatus(StringUtils.blankToDefault(partner.getStatus(), SystemConstants.NORMAL));
        partner.setDelFlag(SystemConstants.NORMAL);
        return partnerMapper.insert(partner);
    }

    @Override
    public int updatePartner(PartnerBo bo) {
        if (bo.getPartnerId() == null) {
            throw new ServiceException("合作客户ID不能为空");
        }
        normalize(bo);
        assertUnique(bo);
        Partner partner = MapstructUtils.convert(bo, Partner.class);
        return partnerMapper.updateById(partner);
    }

    @Override
    public int deletePartnerByIds(Long[] partnerIds) {
        return partnerMapper.deleteByIds(Arrays.asList(partnerIds));
    }

    private LambdaQueryWrapper<Partner> buildQueryWrapper(PartnerBo bo) {
        return Wrappers.<Partner>lambdaQuery()
            .like(StringUtils.isNotBlank(bo.getPartnerCode()), Partner::getPartnerCode, bo.getPartnerCode())
            .like(StringUtils.isNotBlank(bo.getPartnerName()), Partner::getPartnerName, bo.getPartnerName())
            .like(StringUtils.isNotBlank(bo.getContactName()), Partner::getContactName, bo.getContactName())
            .like(StringUtils.isNotBlank(bo.getContactPhone()), Partner::getContactPhone, bo.getContactPhone())
            .eq(StringUtils.isNotBlank(bo.getStatus()), Partner::getStatus, bo.getStatus())
            .orderByDesc(Partner::getCreateTime);
    }

    private void assertUnique(PartnerBo bo) {
        LambdaQueryWrapper<Partner> codeQuery = Wrappers.<Partner>lambdaQuery()
            .eq(Partner::getPartnerCode, bo.getPartnerCode())
            .ne(bo.getPartnerId() != null, Partner::getPartnerId, bo.getPartnerId());
        if (partnerMapper.exists(codeQuery)) {
            throw new ServiceException("合作客户编码已存在");
        }
        if (StringUtils.isNotBlank(bo.getCreditCode())) {
            LambdaQueryWrapper<Partner> creditCodeQuery = Wrappers.<Partner>lambdaQuery()
                .eq(Partner::getCreditCode, bo.getCreditCode())
                .ne(bo.getPartnerId() != null, Partner::getPartnerId, bo.getPartnerId());
            if (partnerMapper.exists(creditCodeQuery)) {
                throw new ServiceException("统一社会信用代码已存在");
            }
        }
    }

    private void normalize(PartnerBo bo) {
        bo.setPartnerCode(bo.getPartnerCode().strip().toUpperCase(Locale.ROOT));
        bo.setPartnerName(bo.getPartnerName().strip());
        bo.setCreditCode(normalizeOptional(bo.getCreditCode(), true));
        bo.setContactName(normalizeOptional(bo.getContactName(), false));
        bo.setContactPhone(normalizeOptional(bo.getContactPhone(), false));
        bo.setContactEmail(normalizeEmail(bo.getContactEmail()));
        bo.setAddress(normalizeOptional(bo.getAddress(), false));
        bo.setRemark(normalizeOptional(bo.getRemark(), false));
    }

    private String normalizeOptional(String value, boolean uppercase) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String normalized = value.strip();
        return uppercase ? normalized.toUpperCase(Locale.ROOT) : normalized;
    }

    private String normalizeEmail(String value) {
        String normalized = normalizeOptional(value, false);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }
}
