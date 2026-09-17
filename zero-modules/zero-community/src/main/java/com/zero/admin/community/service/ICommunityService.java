package com.zero.admin.community.service;

import com.zero.admin.base.mybatis.core.page.PageQuery;
import com.zero.admin.base.mybatis.core.page.TableDataInfo;
import com.zero.admin.community.domain.bo.CommunityAssetQueryBo;
import com.zero.admin.community.domain.bo.CommunityAssetSaveBo;
import com.zero.admin.community.domain.bo.CommunityReviewBo;
import com.zero.admin.community.domain.vo.CommunityAssetVo;

public interface ICommunityService {
    TableDataInfo<CommunityAssetVo> queryAdminPage(CommunityAssetQueryBo bo, PageQuery pageQuery);
    TableDataInfo<CommunityAssetVo> queryFeed(Long applicationId, Long memberId, CommunityAssetQueryBo bo, PageQuery pageQuery);
    TableDataInfo<CommunityAssetVo> queryMine(Long applicationId, Long memberId, CommunityAssetQueryBo bo, PageQuery pageQuery);
    TableDataInfo<CommunityAssetVo> queryFavorites(Long applicationId, Long memberId, CommunityAssetQueryBo bo, PageQuery pageQuery);
    CommunityAssetVo getAdminDetail(Long assetId);
    CommunityAssetVo getConsumerDetail(Long applicationId, Long memberId, Long assetId);
    CommunityAssetVo createDraft(Long applicationId, Long memberId, CommunityAssetSaveBo bo);
    CommunityAssetVo updateDraft(Long applicationId, Long memberId, Long assetId, CommunityAssetSaveBo bo);
    void submit(Long applicationId, Long memberId, Long assetId);
    void favorite(Long applicationId, Long memberId, Long assetId);
    void unfavorite(Long applicationId, Long memberId, Long assetId);
    CommunityAssetVo download(Long applicationId, Long memberId, Long releaseId);
    void review(Long assetId, CommunityReviewBo bo);
    void feature(Long assetId, boolean featured);
    void hide(Long assetId);
}
