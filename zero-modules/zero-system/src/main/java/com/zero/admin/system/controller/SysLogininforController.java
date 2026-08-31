package com.zero.admin.system.controller;

import com.baomidou.lock.annotation.Lock4j;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.zero.admin.base.core.constant.CacheConstants;
import com.zero.admin.base.core.domain.R;
import com.zero.admin.base.excel.utils.ExcelUtil;
import com.zero.admin.base.idempotent.annotation.RepeatSubmit;
import com.zero.admin.base.log.annotation.Log;
import com.zero.admin.base.log.enums.BusinessType;
import com.zero.admin.base.mybatis.core.page.PageQuery;
import com.zero.admin.base.mybatis.core.page.TableDataInfo;
import com.zero.admin.base.redis.utils.RedisUtils;
import com.zero.admin.base.web.core.BaseController;
import com.zero.admin.system.domain.bo.SysLogininforBo;
import com.zero.admin.system.domain.vo.SysLogininforVo;
import com.zero.admin.system.service.ISysLogininforService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 登录日志管理
 *
 * @author Akai
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/logininfor")
public class SysLogininforController extends BaseController {

    private final ISysLogininforService logininforService;

    /**
     * 获取登录日志列表
     */
    @RequiresPermissions("monitor:logininfor:list")
    @GetMapping("/list")
    public TableDataInfo<SysLogininforVo> list(SysLogininforBo logininfor, PageQuery pageQuery) {
        return logininforService.selectPageLogininforList(logininfor, pageQuery);
    }

    /**
     * 导出登录日志
     */
    @RequiresPermissions("monitor:logininfor:export")
    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SysLogininforBo logininfor, HttpServletResponse response) {
        List<SysLogininforVo> list = logininforService.selectLogininforList(logininfor);
        ExcelUtil.exportExcel(list, "登录日志", SysLogininforVo.class, response);
    }

    /**
     * 批量删除登录日志
     */
    @RequiresPermissions("monitor:logininfor:remove")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{infoIds}")
    public R<Void> remove(@PathVariable Long[] infoIds) {
        return toAjax(logininforService.deleteLogininforByIds(infoIds));
    }

    /**
     * 清空登录日志
     */
    @RequiresPermissions("monitor:logininfor:remove")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @Lock4j
    @DeleteMapping("/clean")
    public R<Void> clean() {
        logininforService.cleanLogininfor();
        return R.ok();
    }

    /**
     * 清除指定账号的密码错误次数
     */
    @RequiresPermissions("monitor:logininfor:unlock")
    @Log(title = "账户解锁", businessType = BusinessType.OTHER)
    @RepeatSubmit
    @GetMapping("/unlock/{userName}")
    public R<Void> unlock(@PathVariable String userName) {
        RedisUtils.deleteObject(CacheConstants.PWD_ERR_CNT_KEY + userName);
        return R.ok();
    }
}
