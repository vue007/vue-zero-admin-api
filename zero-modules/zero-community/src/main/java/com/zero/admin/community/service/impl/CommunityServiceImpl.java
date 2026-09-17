package com.zero.admin.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zero.admin.base.core.constant.SystemConstants;
import com.zero.admin.base.core.exception.ServiceException;
import com.zero.admin.base.core.utils.StringUtils;
import com.zero.admin.base.json.utils.JsonUtils;
import com.zero.admin.base.mybatis.core.page.PageQuery;
import com.zero.admin.base.mybatis.core.page.TableDataInfo;
import com.zero.admin.community.constant.CommunityConstants;
import com.zero.admin.community.domain.CommunityAsset;
import com.zero.admin.community.domain.CommunityAssetCompatibility;
import com.zero.admin.community.domain.CommunityAssetRelease;
import com.zero.admin.community.domain.CommunityFavorite;
import com.zero.admin.community.domain.bo.CommunityAssetQueryBo;
import com.zero.admin.community.domain.bo.CommunityAssetSaveBo;
import com.zero.admin.community.domain.bo.CommunityReviewBo;
import com.zero.admin.community.domain.vo.CommunityAssetVo;
import com.zero.admin.community.mapper.CommunityAssetCompatibilityMapper;
import com.zero.admin.community.mapper.CommunityAssetMapper;
import com.zero.admin.community.mapper.CommunityAssetReleaseMapper;
import com.zero.admin.community.mapper.CommunityFavoriteMapper;
import com.zero.admin.community.service.CommunityPayloadValidator;
import com.zero.admin.community.service.ICommunityService;
import com.zero.admin.member.domain.vo.MemberVo;
import com.zero.admin.member.service.IMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/** 社区领域服务。所有 C 端方法都要求调用方传入会话中的 applicationId/memberId。 */
@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements ICommunityService {

    private final CommunityAssetMapper assetMapper;
    private final CommunityAssetReleaseMapper releaseMapper;
    private final CommunityAssetCompatibilityMapper compatibilityMapper;
    private final CommunityFavoriteMapper favoriteMapper;
    private final CommunityPayloadValidator payloadValidator;
    private final IMemberService memberService;

    @Override
    public TableDataInfo<CommunityAssetVo> queryAdminPage(CommunityAssetQueryBo bo, PageQuery pageQuery) {
        return toVoPage(assetMapper.selectPage(pageQuery.build(), buildQuery(bo)), null);
    }

    @Override
    public TableDataInfo<CommunityAssetVo> queryFeed(
        Long applicationId, Long memberId, CommunityAssetQueryBo bo, PageQuery pageQuery) {
        bo.setApplicationId(applicationId);
        bo.setStatus(CommunityConstants.STATUS_PUBLISHED);
        LambdaQueryWrapper<CommunityAsset> wrapper = buildQuery(bo)
            .eq(CommunityAsset::getVisibility, CommunityConstants.VISIBILITY_PUBLIC);
        return toVoPage(assetMapper.selectPage(pageQuery.build(), wrapper), memberId);
    }

    @Override
    public TableDataInfo<CommunityAssetVo> queryMine(
        Long applicationId, Long memberId, CommunityAssetQueryBo bo, PageQuery pageQuery) {
        bo.setApplicationId(applicationId);
        bo.setOwnerMemberId(memberId);
        return toVoPage(assetMapper.selectPage(pageQuery.build(), buildQuery(bo)), memberId);
    }

    @Override
    public TableDataInfo<CommunityAssetVo> queryFavorites(
        Long applicationId, Long memberId, CommunityAssetQueryBo bo, PageQuery pageQuery) {
        List<Long> assetIds = favoriteMapper.selectList(Wrappers.<CommunityFavorite>lambdaQuery()
                .eq(CommunityFavorite::getApplicationId, applicationId)
                .eq(CommunityFavorite::getMemberId, memberId))
            .stream().map(CommunityFavorite::getAssetId).toList();
        if (assetIds.isEmpty()) return new TableDataInfo<>(List.of(), 0);
        bo.setApplicationId(applicationId);
        bo.setStatus(CommunityConstants.STATUS_PUBLISHED);
        LambdaQueryWrapper<CommunityAsset> wrapper = buildQuery(bo).in(CommunityAsset::getAssetId, assetIds);
        return toVoPage(assetMapper.selectPage(pageQuery.build(), wrapper), memberId);
    }

    @Override
    public CommunityAssetVo getAdminDetail(Long assetId) {
        return toVo(requireAsset(assetId), null, true);
    }

    @Override
    public CommunityAssetVo getConsumerDetail(Long applicationId, Long memberId, Long assetId) {
        CommunityAsset asset = requireConsumerAsset(applicationId, assetId);
        boolean owner = memberId.equals(asset.getOwnerMemberId());
        if (!owner && (!CommunityConstants.STATUS_PUBLISHED.equals(asset.getStatus())
            || !CommunityConstants.VISIBILITY_PUBLIC.equals(asset.getVisibility()))) {
            throw new ServiceException("社区内容不存在或不可访问");
        }
        return toVo(asset, memberId, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityAssetVo createDraft(Long applicationId, Long memberId, CommunityAssetSaveBo bo) {
        normalize(bo);
        String payload = payloadValidator.validateAndSerialize(bo.getKind(), bo.getDeviceType(), bo.getPayload());

        CommunityAsset asset = new CommunityAsset();
        asset.setApplicationId(applicationId);
        asset.setOwnerMemberId(memberId);
        applyAssetFields(asset, bo);
        asset.setStatus(CommunityConstants.STATUS_DRAFT);
        asset.setFeatured("0");
        asset.setFavoriteCount(0L);
        asset.setDownloadCount(0L);
        asset.setDelFlag(SystemConstants.NORMAL);
        assetMapper.insert(asset);

        CommunityAssetRelease release = createRelease(asset, bo, payload, 1);
        asset.setCurrentReleaseId(release.getReleaseId());
        assetMapper.updateById(asset);
        return toVo(asset, memberId, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityAssetVo updateDraft(
        Long applicationId, Long memberId, Long assetId, CommunityAssetSaveBo bo) {
        CommunityAsset asset = requireOwnedAsset(applicationId, memberId, assetId);
        if (!CommunityConstants.STATUS_DRAFT.equals(asset.getStatus())
            && !CommunityConstants.STATUS_REJECTED.equals(asset.getStatus())) {
            throw new ServiceException("只有草稿或已驳回内容可以修改");
        }
        normalize(bo);
        String payload = payloadValidator.validateAndSerialize(bo.getKind(), bo.getDeviceType(), bo.getPayload());
        applyAssetFields(asset, bo);
        asset.setStatus(CommunityConstants.STATUS_DRAFT);
        asset.setRejectReason(null);
        assetMapper.updateById(asset);

        CommunityAssetRelease release = requireRelease(asset.getCurrentReleaseId());
        applyReleaseFields(release, bo, payload);
        releaseMapper.updateById(release);
        replaceCompatibility(asset, release, bo);
        return toVo(asset, memberId, true);
    }

    @Override
    public void submit(Long applicationId, Long memberId, Long assetId) {
        CommunityAsset asset = requireOwnedAsset(applicationId, memberId, assetId);
        if (!CommunityConstants.STATUS_DRAFT.equals(asset.getStatus())
            && !CommunityConstants.STATUS_REJECTED.equals(asset.getStatus())) {
            throw new ServiceException("当前状态不能提交审核");
        }
        asset.setStatus(CommunityConstants.STATUS_PENDING);
        asset.setRejectReason(null);
        assetMapper.updateById(asset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void favorite(Long applicationId, Long memberId, Long assetId) {
        CommunityAsset asset = requireConsumerAsset(applicationId, assetId);
        requirePublished(asset);
        boolean exists = favoriteMapper.exists(Wrappers.<CommunityFavorite>lambdaQuery()
            .eq(CommunityFavorite::getApplicationId, applicationId)
            .eq(CommunityFavorite::getAssetId, assetId)
            .eq(CommunityFavorite::getMemberId, memberId));
        if (exists) return;
        CommunityFavorite favorite = new CommunityFavorite();
        favorite.setApplicationId(applicationId);
        favorite.setAssetId(assetId);
        favorite.setMemberId(memberId);
        favorite.setDelFlag(SystemConstants.NORMAL);
        try {
            favoriteMapper.insert(favorite);
            assetMapper.update(null, Wrappers.<CommunityAsset>lambdaUpdate()
                .eq(CommunityAsset::getAssetId, assetId)
                .setSql("favorite_count = favorite_count + 1"));
        } catch (DuplicateKeyException ignored) {
            // 并发重复收藏按幂等成功处理。
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfavorite(Long applicationId, Long memberId, Long assetId) {
        LambdaQueryWrapper<CommunityFavorite> wrapper = Wrappers.<CommunityFavorite>lambdaQuery()
            .eq(CommunityFavorite::getApplicationId, applicationId)
            .eq(CommunityFavorite::getAssetId, assetId)
            .eq(CommunityFavorite::getMemberId, memberId);
        if (!favoriteMapper.exists(wrapper)) return;
        favoriteMapper.delete(wrapper);
        assetMapper.update(null, Wrappers.<CommunityAsset>lambdaUpdate()
            .eq(CommunityAsset::getAssetId, assetId)
            .setSql("favorite_count = greatest(favorite_count - 1, 0)"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityAssetVo download(Long applicationId, Long memberId, Long releaseId) {
        CommunityAssetRelease release = requireRelease(releaseId);
        if (!applicationId.equals(release.getApplicationId())) {
            throw new ServiceException("发布版本不存在");
        }
        CommunityAsset asset = requireConsumerAsset(applicationId, release.getAssetId());
        requirePublished(asset);
        if (!releaseId.equals(asset.getCurrentReleaseId())) {
            throw new ServiceException("该版本已不是当前可下载版本");
        }
        assetMapper.update(null, Wrappers.<CommunityAsset>lambdaUpdate()
            .eq(CommunityAsset::getAssetId, asset.getAssetId())
            .setSql("download_count = download_count + 1"));
        asset.setDownloadCount(asset.getDownloadCount() + 1);
        return toVo(asset, memberId, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(Long assetId, CommunityReviewBo bo) {
        CommunityAsset asset = requireAsset(assetId);
        if (!CommunityConstants.STATUS_PENDING.equals(asset.getStatus())) {
            throw new ServiceException("只有待审核内容可以审核");
        }
        String action = bo.getAction().strip().toLowerCase(Locale.ROOT);
        if ("approve".equals(action)) {
            Date now = new Date();
            asset.setStatus(CommunityConstants.STATUS_PUBLISHED);
            asset.setPublishedAt(now);
            asset.setRejectReason(null);
            CommunityAssetRelease release = requireRelease(asset.getCurrentReleaseId());
            release.setPublishedAt(now);
            releaseMapper.updateById(release);
        } else if ("reject".equals(action)) {
            if (StringUtils.isBlank(bo.getReason())) throw new ServiceException("驳回时必须填写原因");
            asset.setStatus(CommunityConstants.STATUS_REJECTED);
            asset.setRejectReason(bo.getReason().strip());
        } else {
            throw new ServiceException("审核动作只允许 approve 或 reject");
        }
        assetMapper.updateById(asset);
    }

    @Override
    public void feature(Long assetId, boolean featured) {
        CommunityAsset asset = requireAsset(assetId);
        if (featured && !CommunityConstants.STATUS_PUBLISHED.equals(asset.getStatus())) {
            throw new ServiceException("只有已发布内容可以设为精选");
        }
        asset.setFeatured(featured ? "1" : "0");
        assetMapper.updateById(asset);
    }

    @Override
    public void hide(Long assetId) {
        CommunityAsset asset = requireAsset(assetId);
        if (!CommunityConstants.STATUS_PUBLISHED.equals(asset.getStatus())) {
            throw new ServiceException("只有已发布内容可以下架");
        }
        asset.setStatus(CommunityConstants.STATUS_HIDDEN);
        asset.setFeatured("0");
        assetMapper.updateById(asset);
    }

    private LambdaQueryWrapper<CommunityAsset> buildQuery(CommunityAssetQueryBo bo) {
        return Wrappers.<CommunityAsset>lambdaQuery()
            .eq(StringUtils.isNotBlank(bo.getTenantId()), CommunityAsset::getTenantId, bo.getTenantId())
            .eq(bo.getApplicationId() != null, CommunityAsset::getApplicationId, bo.getApplicationId())
            .eq(bo.getOwnerMemberId() != null, CommunityAsset::getOwnerMemberId, bo.getOwnerMemberId())
            .and(StringUtils.isNotBlank(bo.getKeyword()), w -> w
                .like(CommunityAsset::getTitle, bo.getKeyword())
                .or().like(CommunityAsset::getSummary, bo.getKeyword()))
            .eq(StringUtils.isNotBlank(bo.getKind()), CommunityAsset::getKind, bo.getKind())
            .eq(StringUtils.isNotBlank(bo.getDeviceType()), CommunityAsset::getDeviceType, bo.getDeviceType())
            .eq(StringUtils.isNotBlank(bo.getStatus()), CommunityAsset::getStatus, bo.getStatus())
            .eq(StringUtils.isNotBlank(bo.getFeatured()), CommunityAsset::getFeatured, bo.getFeatured())
            .orderByDesc(CommunityAsset::getFeatured)
            .orderByDesc(CommunityAsset::getPublishedAt)
            .orderByDesc(CommunityAsset::getCreateTime);
    }

    private TableDataInfo<CommunityAssetVo> toVoPage(Page<CommunityAsset> page, Long memberId) {
        List<CommunityAssetVo> rows = page.getRecords().stream().map(asset -> toVo(asset, memberId, false)).toList();
        return new TableDataInfo<>(rows, page.getTotal());
    }

    private CommunityAssetVo toVo(CommunityAsset asset, Long memberId, boolean includePayload) {
        CommunityAssetVo vo = new CommunityAssetVo();
        vo.setAssetId(asset.getAssetId());
        vo.setTenantId(asset.getTenantId());
        vo.setApplicationId(asset.getApplicationId());
        vo.setOwnerMemberId(asset.getOwnerMemberId());
        vo.setTitle(asset.getTitle());
        vo.setSummary(asset.getSummary());
        vo.setCoverUrl(asset.getCoverUrl());
        vo.setKind(asset.getKind());
        vo.setDeviceType(asset.getDeviceType());
        vo.setVisibility(asset.getVisibility());
        vo.setStatus(asset.getStatus());
        vo.setCurrentReleaseId(asset.getCurrentReleaseId());
        vo.setFeatured(asset.getFeatured());
        vo.setFavoriteCount(asset.getFavoriteCount());
        vo.setDownloadCount(asset.getDownloadCount());
        vo.setPublishedAt(asset.getPublishedAt());
        vo.setRejectReason(asset.getRejectReason());
        vo.setCreateTime(asset.getCreateTime());
        vo.setUpdateTime(asset.getUpdateTime());
        MemberVo owner = memberService.queryById(asset.getOwnerMemberId());
        if (owner != null) {
            vo.setOwnerName(StringUtils.blankToDefault(owner.getNickname(), owner.getUsername()));
            vo.setOwnerAvatar(owner.getAvatar());
        }
        if (memberId != null) {
            vo.setFavorited(favoriteMapper.exists(Wrappers.<CommunityFavorite>lambdaQuery()
                .eq(CommunityFavorite::getApplicationId, asset.getApplicationId())
                .eq(CommunityFavorite::getAssetId, asset.getAssetId())
                .eq(CommunityFavorite::getMemberId, memberId)));
        }
        if (asset.getCurrentReleaseId() != null) {
            CommunityAssetRelease release = requireRelease(asset.getCurrentReleaseId());
            vo.setVersionNo(release.getVersionNo());
            vo.setSchemaVersion(release.getSchemaVersion());
            vo.setSourcePlatformCode(release.getSourcePlatformCode());
            vo.setSourceProductCode(release.getSourceProductCode());
            vo.setSourceCapabilityVersion(release.getSourceCapabilityVersion());
            vo.setPayloadHash(release.getPayloadHash());
            vo.setPayloadSize(release.getPayloadSize());
            vo.setChangelog(release.getChangelog());
            if (includePayload) vo.setPayload(JsonUtils.parseObject(release.getPayload(), Object.class));
        }
        return vo;
    }

    private CommunityAssetRelease createRelease(
        CommunityAsset asset, CommunityAssetSaveBo bo, String payload, int versionNo) {
        CommunityAssetRelease release = new CommunityAssetRelease();
        release.setApplicationId(asset.getApplicationId());
        release.setAssetId(asset.getAssetId());
        release.setVersionNo(versionNo);
        release.setDelFlag(SystemConstants.NORMAL);
        applyReleaseFields(release, bo, payload);
        releaseMapper.insert(release);
        replaceCompatibility(asset, release, bo);
        return release;
    }

    private void replaceCompatibility(
        CommunityAsset asset, CommunityAssetRelease release, CommunityAssetSaveBo bo) {
        compatibilityMapper.delete(Wrappers.<CommunityAssetCompatibility>lambdaQuery()
            .eq(CommunityAssetCompatibility::getReleaseId, release.getReleaseId()));
        CommunityAssetCompatibility compatibility = new CommunityAssetCompatibility();
        compatibility.setApplicationId(asset.getApplicationId());
        compatibility.setReleaseId(release.getReleaseId());
        compatibility.setPlatformCode(bo.getSourcePlatformCode());
        compatibility.setProductCode(bo.getSourceProductCode());
        compatibility.setDeviceType(bo.getDeviceType());
        compatibility.setCapabilityVersion(bo.getSourceCapabilityVersion());
        compatibility.setDelFlag(SystemConstants.NORMAL);
        compatibilityMapper.insert(compatibility);
    }

    private void applyAssetFields(CommunityAsset asset, CommunityAssetSaveBo bo) {
        asset.setTitle(bo.getTitle());
        asset.setSummary(bo.getSummary());
        asset.setCoverUrl(bo.getCoverUrl());
        asset.setKind(bo.getKind());
        asset.setDeviceType(bo.getDeviceType());
        asset.setVisibility(bo.getVisibility());
    }

    private void applyReleaseFields(CommunityAssetRelease release, CommunityAssetSaveBo bo, String payload) {
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
        release.setSchemaVersion(bo.getSchemaVersion());
        release.setSourcePlatformCode(bo.getSourcePlatformCode());
        release.setSourceProductCode(bo.getSourceProductCode());
        release.setSourceCapabilityVersion(bo.getSourceCapabilityVersion());
        release.setPayload(payload);
        release.setPayloadHash(sha256(payloadBytes));
        release.setPayloadSize(payloadBytes.length);
        release.setChangelog(bo.getChangelog());
    }

    private void normalize(CommunityAssetSaveBo bo) {
        bo.setTitle(bo.getTitle().strip());
        bo.setSummary(trimToNull(bo.getSummary()));
        bo.setCoverUrl(trimToNull(bo.getCoverUrl()));
        bo.setKind(bo.getKind().strip().toLowerCase(Locale.ROOT));
        bo.setDeviceType(bo.getDeviceType().strip().toLowerCase(Locale.ROOT));
        bo.setVisibility(StringUtils.blankToDefault(bo.getVisibility(), CommunityConstants.VISIBILITY_PUBLIC)
            .strip().toLowerCase(Locale.ROOT));
        if (!CommunityConstants.VISIBILITY_PUBLIC.equals(bo.getVisibility())
            && !CommunityConstants.VISIBILITY_PRIVATE.equals(bo.getVisibility())) {
            throw new ServiceException("可见性只允许 public 或 private");
        }
        bo.setSourcePlatformCode(bo.getSourcePlatformCode().strip());
        bo.setSourceProductCode(bo.getSourceProductCode().strip());
        bo.setSourceCapabilityVersion(trimToNull(bo.getSourceCapabilityVersion()));
        bo.setChangelog(trimToNull(bo.getChangelog()));
    }

    private String trimToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.strip();
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 不可用", exception);
        }
    }

    private CommunityAsset requireAsset(Long assetId) {
        CommunityAsset asset = assetMapper.selectById(assetId);
        if (asset == null) throw new ServiceException("社区内容不存在");
        return asset;
    }

    private CommunityAsset requireConsumerAsset(Long applicationId, Long assetId) {
        CommunityAsset asset = assetMapper.selectOne(Wrappers.<CommunityAsset>lambdaQuery()
            .eq(CommunityAsset::getAssetId, assetId)
            .eq(CommunityAsset::getApplicationId, applicationId));
        if (asset == null) throw new ServiceException("社区内容不存在");
        return asset;
    }

    private CommunityAsset requireOwnedAsset(Long applicationId, Long memberId, Long assetId) {
        CommunityAsset asset = requireConsumerAsset(applicationId, assetId);
        if (!memberId.equals(asset.getOwnerMemberId())) throw new ServiceException("无权修改该社区内容");
        return asset;
    }

    private CommunityAssetRelease requireRelease(Long releaseId) {
        CommunityAssetRelease release = releaseId == null ? null : releaseMapper.selectById(releaseId);
        if (release == null) throw new ServiceException("社区发布版本不存在");
        return release;
    }

    private void requirePublished(CommunityAsset asset) {
        if (!CommunityConstants.STATUS_PUBLISHED.equals(asset.getStatus())) {
            throw new ServiceException("社区内容尚未发布");
        }
    }
}
