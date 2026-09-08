package com.zero.admin.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zero.admin.base.core.constant.SystemConstants;
import com.zero.admin.base.core.exception.ServiceException;
import com.zero.admin.base.core.utils.MapstructUtils;
import com.zero.admin.base.core.utils.StringUtils;
import com.zero.admin.base.mybatis.core.page.PageQuery;
import com.zero.admin.base.mybatis.core.page.TableDataInfo;
import com.zero.admin.base.tenant.helper.TenantHelper;
import com.zero.admin.member.domain.Member;
import com.zero.admin.member.domain.MemberSocial;
import com.zero.admin.member.domain.bo.MemberQueryBo;
import com.zero.admin.member.domain.bo.MemberRegisterBo;
import com.zero.admin.member.domain.model.MemberSocialIdentity;
import com.zero.admin.member.domain.vo.MemberVo;
import com.zero.admin.member.mapper.MemberMapper;
import com.zero.admin.member.mapper.MemberSocialMapper;
import com.zero.admin.member.service.IMemberService;
import com.zero.admin.member.service.MemberPasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Locale;

/** 会员领域服务实现。 */
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements IMemberService {

    @Autowired(required = false)
    private final MemberMapper memberMapper;

    @Autowired(required = false)
    private final MemberSocialMapper socialMapper;

    private final MemberPasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberVo register(String tenantId, MemberRegisterBo bo) {
        return TenantHelper.dynamic(tenantId, () -> {
            String username = normalizeUsername(bo.getUsername());
            assertUsernameAvailable(username);
            assertContactAvailable(bo.getMobile(), bo.getEmail());

            Member member = MapstructUtils.convert(bo, Member.class);
            if (member == null) {
                throw new ServiceException("会员注册信息转换失败");
            }
            member.setTenantId(tenantId);
            member.setUsername(username);
            member.setNickname(StringUtils.blankToDefault(normalizeOptional(bo.getNickname()), username));
            member.setMobile(normalizeOptional(bo.getMobile()));
            member.setEmail(normalizeEmail(bo.getEmail()));
            member.setPassword(passwordEncoder.encode(bo.getPassword()));
            member.setStatus(SystemConstants.NORMAL);
            member.setDelFlag(SystemConstants.NORMAL);
            member.setRegisterSource("password");
            memberMapper.insert(member);
            return memberMapper.selectVoById(member.getMemberId());
        });
    }

    @Override
    public MemberVo authenticatePassword(String tenantId, String username, String rawPassword) {
        return TenantHelper.dynamic(tenantId, () -> {
            Member member = memberMapper.selectOne(new LambdaQueryWrapper<Member>()
                .eq(Member::getUsername, normalizeUsername(username)));
            if (member == null || !passwordEncoder.matches(rawPassword, member.getPassword())) {
                throw new ServiceException("会员账号或密码错误");
            }
            assertEnabled(member);
            return MapstructUtils.convert(member, MemberVo.class);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberVo loginOrRegisterSocial(String tenantId, MemberSocialIdentity identity) {
        return TenantHelper.dynamic(tenantId, () -> {
            String source = normalizeSource(identity.getSource());
            String openId = normalizeRequired(identity.getOpenId(), "第三方用户标识不能为空");
            String authId = source + ":" + openId;
            MemberSocial binding = socialMapper.selectOne(new LambdaQueryWrapper<MemberSocial>()
                .eq(MemberSocial::getAuthId, authId));

            if (binding != null) {
                refreshSocialProfile(binding, identity);
                Member member = memberMapper.selectById(binding.getMemberId());
                if (member == null) {
                    throw new ServiceException("第三方账号绑定的会员不存在");
                }
                assertEnabled(member);
                return MapstructUtils.convert(member, MemberVo.class);
            }

            long memberId = IdWorker.getId();
            Member member = new Member();
            member.setMemberId(memberId);
            member.setTenantId(tenantId);
            member.setUsername("m" + memberId);
            member.setNickname(StringUtils.blankToDefault(normalizeOptional(identity.getNickname()), "新会员"));
            member.setAvatar(normalizeOptional(identity.getAvatar()));
            member.setStatus(SystemConstants.NORMAL);
            member.setDelFlag(SystemConstants.NORMAL);
            member.setRegisterSource(source);
            memberMapper.insert(member);

            MemberSocial social = new MemberSocial();
            social.setTenantId(tenantId);
            social.setMemberId(memberId);
            social.setAuthId(authId);
            social.setSource(source);
            social.setOpenId(openId);
            social.setUnionId(normalizeOptional(identity.getUnionId()));
            social.setUsername(normalizeOptional(identity.getUsername()));
            social.setNickname(normalizeOptional(identity.getNickname()));
            social.setAvatar(normalizeOptional(identity.getAvatar()));
            social.setDelFlag(SystemConstants.NORMAL);
            socialMapper.insert(social);
            return memberMapper.selectVoById(memberId);
        });
    }

    @Override
    public void recordLogin(String tenantId, Long memberId, String ip) {
        TenantHelper.dynamic(tenantId, () -> {
            Member member = new Member();
            member.setMemberId(memberId);
            member.setLoginIp(ip);
            member.setLoginDate(new Date());
            memberMapper.updateById(member);
        });
    }

    @Override
    public MemberVo queryById(Long memberId) {
        return memberMapper.selectVoById(memberId);
    }

    @Override
    public TableDataInfo<MemberVo> queryPageList(MemberQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Member> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(bo.getMemberId() != null, Member::getMemberId, bo.getMemberId())
            .like(StringUtils.isNotBlank(bo.getUsername()), Member::getUsername, bo.getUsername())
            .like(StringUtils.isNotBlank(bo.getNickname()), Member::getNickname, bo.getNickname())
            .like(StringUtils.isNotBlank(bo.getMobile()), Member::getMobile, bo.getMobile())
            .eq(StringUtils.isNotBlank(bo.getStatus()), Member::getStatus, bo.getStatus())
            .orderByDesc(Member::getCreateTime);
        Page<MemberVo> page = memberMapper.selectVoPage(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    @Override
    public boolean updateStatus(Long memberId, String status) {
        if (!SystemConstants.NORMAL.equals(status) && !SystemConstants.DISABLE.equals(status)) {
            throw new ServiceException("会员状态只能为0或1");
        }
        Member member = new Member();
        member.setMemberId(memberId);
        member.setStatus(status);
        return memberMapper.updateById(member) > 0;
    }

    private void assertUsernameAvailable(String username) {
        boolean exists = memberMapper.exists(new LambdaQueryWrapper<Member>().eq(Member::getUsername, username));
        if (exists) {
            throw new ServiceException("会员账号已存在");
        }
    }

    private void assertContactAvailable(String mobile, String email) {
        String normalizedMobile = normalizeOptional(mobile);
        if (normalizedMobile != null && memberMapper.exists(
            new LambdaQueryWrapper<Member>().eq(Member::getMobile, normalizedMobile))) {
            throw new ServiceException("手机号码已注册");
        }
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail != null && memberMapper.exists(
            new LambdaQueryWrapper<Member>().eq(Member::getEmail, normalizedEmail))) {
            throw new ServiceException("邮箱已注册");
        }
    }

    private void refreshSocialProfile(MemberSocial binding, MemberSocialIdentity identity) {
        binding.setUnionId(normalizeOptional(identity.getUnionId()));
        binding.setUsername(normalizeOptional(identity.getUsername()));
        binding.setNickname(normalizeOptional(identity.getNickname()));
        binding.setAvatar(normalizeOptional(identity.getAvatar()));
        socialMapper.updateById(binding);
    }

    private void assertEnabled(Member member) {
        if (SystemConstants.DISABLE.equals(member.getStatus())) {
            throw new ServiceException("会员账号已停用");
        }
    }

    private String normalizeUsername(String username) {
        return normalizeRequired(username, "会员账号不能为空").toLowerCase(Locale.ROOT);
    }

    private String normalizeSource(String source) {
        return normalizeRequired(source, "第三方登录来源不能为空").toLowerCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        String value = normalizeOptional(email);
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new ServiceException(message);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return value.strip();
    }
}
