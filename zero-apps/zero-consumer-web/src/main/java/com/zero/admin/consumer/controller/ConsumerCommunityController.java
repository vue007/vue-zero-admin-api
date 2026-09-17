package com.zero.admin.consumer.controller;

import com.zero.admin.base.core.domain.R;
import com.zero.admin.base.core.exception.ServiceException;
import com.zero.admin.base.mybatis.core.page.PageQuery;
import com.zero.admin.base.mybatis.core.page.TableDataInfo;
import com.zero.admin.base.shiro.utils.LoginHelper;
import com.zero.admin.community.domain.bo.CommunityAssetQueryBo;
import com.zero.admin.community.domain.bo.CommunityAssetSaveBo;
import com.zero.admin.community.domain.vo.CommunityAssetVo;
import com.zero.admin.community.service.ICommunityService;
import com.zero.admin.member.domain.model.MemberLoginUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Web Hub 设备配置社区接口。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/community")
public class ConsumerCommunityController {

    private final ICommunityService communityService;

    @GetMapping("/feed")
    public TableDataInfo<CommunityAssetVo> feed(CommunityAssetQueryBo bo, PageQuery pageQuery) {
        MemberLoginUser member = currentMember();
        return communityService.queryFeed(member.getApplicationId(), member.getUserId(), bo, pageQuery);
    }

    @GetMapping("/assets/{assetId}")
    public R<CommunityAssetVo> detail(@NotNull @PathVariable Long assetId) {
        MemberLoginUser member = currentMember();
        return R.ok(communityService.getConsumerDetail(member.getApplicationId(), member.getUserId(), assetId));
    }

    @GetMapping("/me/assets")
    public TableDataInfo<CommunityAssetVo> mine(CommunityAssetQueryBo bo, PageQuery pageQuery) {
        MemberLoginUser member = currentMember();
        return communityService.queryMine(member.getApplicationId(), member.getUserId(), bo, pageQuery);
    }

    @GetMapping("/me/favorites")
    public TableDataInfo<CommunityAssetVo> favorites(CommunityAssetQueryBo bo, PageQuery pageQuery) {
        MemberLoginUser member = currentMember();
        return communityService.queryFavorites(member.getApplicationId(), member.getUserId(), bo, pageQuery);
    }

    @PostMapping("/assets")
    public R<CommunityAssetVo> create(@Valid @RequestBody CommunityAssetSaveBo bo) {
        MemberLoginUser member = currentMember();
        return R.ok(communityService.createDraft(member.getApplicationId(), member.getUserId(), bo));
    }

    @PutMapping("/assets/{assetId}")
    public R<CommunityAssetVo> update(
        @NotNull @PathVariable Long assetId, @Valid @RequestBody CommunityAssetSaveBo bo) {
        MemberLoginUser member = currentMember();
        return R.ok(communityService.updateDraft(member.getApplicationId(), member.getUserId(), assetId, bo));
    }

    @PostMapping("/assets/{assetId}/submit")
    public R<Void> submit(@NotNull @PathVariable Long assetId) {
        MemberLoginUser member = currentMember();
        communityService.submit(member.getApplicationId(), member.getUserId(), assetId);
        return R.ok();
    }

    @PostMapping("/assets/{assetId}/favorite")
    public R<Void> favorite(@NotNull @PathVariable Long assetId) {
        MemberLoginUser member = currentMember();
        communityService.favorite(member.getApplicationId(), member.getUserId(), assetId);
        return R.ok();
    }

    @DeleteMapping("/assets/{assetId}/favorite")
    public R<Void> unfavorite(@NotNull @PathVariable Long assetId) {
        MemberLoginUser member = currentMember();
        communityService.unfavorite(member.getApplicationId(), member.getUserId(), assetId);
        return R.ok();
    }

    @PostMapping("/releases/{releaseId}/download")
    public R<CommunityAssetVo> download(@NotNull @PathVariable Long releaseId) {
        MemberLoginUser member = currentMember();
        return R.ok(communityService.download(member.getApplicationId(), member.getUserId(), releaseId));
    }

    private MemberLoginUser currentMember() {
        if (LoginHelper.getLoginUser() instanceof MemberLoginUser member) return member;
        throw new ServiceException("当前会话不是会员会话");
    }
}
