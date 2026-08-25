package com.zero.admin.system.controller;

import cn.hutool.core.bean.BeanUtil;
import com.zero.admin.base.core.constant.CacheConstants;
import com.zero.admin.base.core.domain.R;
import com.zero.admin.base.core.domain.dto.UserOnlineDTO;
import com.zero.admin.base.core.domain.model.LoginUser;
import com.zero.admin.base.core.exception.ServiceException;
import com.zero.admin.base.core.utils.StringUtils;
import com.zero.admin.base.idempotent.annotation.RepeatSubmit;
import com.zero.admin.base.log.annotation.Log;
import com.zero.admin.base.log.enums.BusinessType;
import com.zero.admin.base.mybatis.core.page.PageQuery;
import com.zero.admin.base.mybatis.core.page.TableDataInfo;
import com.zero.admin.base.redis.utils.RedisUtils;
import com.zero.admin.base.shiro.session.ShiroSessionStore;
import com.zero.admin.base.shiro.utils.LoginHelper;
import com.zero.admin.system.domain.SysUserOnline;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.session.Session;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 在线用户监控。基于当前系统的 Shiro Redis 会话实现，不依赖 Sa-Token。
 *
 * @author Akai
 */
@Validated
@RestController
@RequestMapping("/monitor/online")
public class SysUserOnlineController {

    /**
     * 查询在线用户。非超级管理员即使获得权限，也只能查看当前租户会话。
     */
    @RequiresPermissions("monitor:online:list")
    @GetMapping("/list")
    public TableDataInfo<SysUserOnline> list(String ipaddr, String userName, PageQuery pageQuery) {
        List<SysUserOnline> users = new ArrayList<>();
        for (Serializable sessionId : ShiroSessionStore.activeSessionIds()) {
            String tokenId = sessionId.toString();
            Session session = ShiroSessionStore.read(sessionId);
            if (session == null) {
                RedisUtils.deleteObject(CacheConstants.ONLINE_TOKEN_KEY + tokenId);
                continue;
            }
            UserOnlineDTO dto = RedisUtils.getCacheObject(CacheConstants.ONLINE_TOKEN_KEY + tokenId);
            if (dto == null || !canView(dto)) {
                continue;
            }
            if (StringUtils.isNotBlank(ipaddr)
                && !StringUtils.containsIgnoreCase(dto.getIpaddr(), ipaddr.trim())) {
                continue;
            }
            if (StringUtils.isNotBlank(userName)
                && !StringUtils.containsIgnoreCase(dto.getUserName(), userName.trim())) {
                continue;
            }
            SysUserOnline online = BeanUtil.copyProperties(dto, SysUserOnline.class);
            if (session.getLastAccessTime() != null) {
                online.setLastAccessTime(session.getLastAccessTime().getTime());
                if (session.getTimeout() > 0) {
                    online.setExpireTime(session.getLastAccessTime().getTime() + session.getTimeout());
                }
            }
            users.add(online);
        }
        users.sort(Comparator.comparing(SysUserOnline::getLoginTime,
            Comparator.nullsLast(Comparator.reverseOrder())));
        return page(users, pageQuery);
    }

    /**
     * 强制退出单个在线会话。
     */
    @RequiresPermissions("monitor:online:forceLogout")
    @Log(title = "在线用户", businessType = BusinessType.FORCE,
        excludeParamNames = {"tokenId"})
    @RepeatSubmit
    @DeleteMapping("/force")
    public R<Void> forceLogout(@Valid @RequestBody ForceLogoutBody body) {
        forceLogout(body.tokenId());
        return R.ok();
    }

    /**
     * 批量强制退出在线会话。
     */
    @RequiresPermissions("monitor:online:batchLogout")
    @Log(title = "在线用户", businessType = BusinessType.FORCE,
        excludeParamNames = {"tokenIds"})
    @RepeatSubmit
    @DeleteMapping("/batch")
    public R<Void> batchLogout(@Valid @RequestBody BatchLogoutBody body) {
        body.tokenIds().stream().distinct().forEach(this::forceLogout);
        return R.ok();
    }

    private boolean canView(UserOnlineDTO dto) {
        return LoginHelper.isSuperAdmin()
            || Objects.equals(LoginHelper.getTenantId(), dto.getTenantId());
    }

    private void forceLogout(String tokenId) {
        LoginUser target = LoginHelper.getLoginUser(tokenId);
        if (target != null && !LoginHelper.isSuperAdmin()
            && !Objects.equals(LoginHelper.getTenantId(), target.getTenantId())) {
            throw new ServiceException("无权强退其他租户的在线用户");
        }
        ShiroSessionStore.delete(tokenId);
        RedisUtils.deleteObject(CacheConstants.ONLINE_TOKEN_KEY + tokenId);
    }

    private TableDataInfo<SysUserOnline> page(List<SysUserOnline> users, PageQuery pageQuery) {
        int pageNo = pageQuery.getPageNo() == null ? 1 : Math.max(pageQuery.getPageNo(), 1);
        int pageSize = pageQuery.getPageSize() == null ? 10 : Math.clamp(pageQuery.getPageSize(), 1, 100);
        long offset = (long) (pageNo - 1) * pageSize;
        int fromIndex = (int) Math.min(offset, users.size());
        int toIndex = Math.min(fromIndex + pageSize, users.size());
        return new TableDataInfo<>(users.subList(fromIndex, toIndex), users.size());
    }

    public record ForceLogoutBody(
        @NotBlank(message = "会话编号不能为空") String tokenId
    ) {
    }

    public record BatchLogoutBody(
        @NotEmpty(message = "会话编号不能为空") List<@NotBlank String> tokenIds
    ) {
    }
}
