package com.zero.admin.system.controller;

import com.baomidou.lock.annotation.Lock4j;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.zero.admin.base.core.domain.R;
import com.zero.admin.base.excel.utils.ExcelUtil;
import com.zero.admin.base.log.annotation.Log;
import com.zero.admin.base.log.enums.BusinessType;
import com.zero.admin.base.mybatis.core.page.PageQuery;
import com.zero.admin.base.mybatis.core.page.TableDataInfo;
import com.zero.admin.base.web.core.BaseController;
import com.zero.admin.system.domain.bo.SysOperLogBo;
import com.zero.admin.system.domain.vo.SysOperLogVo;
import com.zero.admin.system.service.ISysOperLogService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 操作日志管理
 *
 * @author Akai
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/operlog")
public class SysOperlogController extends BaseController {

    private final ISysOperLogService operLogService;

    /**
     * 获取操作日志列表
     */
    @RequiresPermissions("monitor:operlog:list")
    @GetMapping("/list")
    public TableDataInfo<SysOperLogVo> list(SysOperLogBo operLog, PageQuery pageQuery) {
        return operLogService.selectPageOperLogList(operLog, pageQuery);
    }

    /**
     * 导出操作日志
     */
    @RequiresPermissions("monitor:operlog:export")
    @Log(title = "操作日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(SysOperLogBo operLog, HttpServletResponse response) {
        List<SysOperLogVo> list = operLogService.selectOperLogList(operLog);
        ExcelUtil.exportExcel(list, "操作日志", SysOperLogVo.class, response);
    }

    /**
     * 批量删除操作日志
     */
    @RequiresPermissions("monitor:operlog:remove")
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{operIds}")
    public R<Void> remove(@PathVariable Long[] operIds) {
        return toAjax(operLogService.deleteOperLogByIds(operIds));
    }

    /**
     * 清空操作日志
     */
    @RequiresPermissions("monitor:operlog:remove")
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @Lock4j
    @DeleteMapping("/clean")
    public R<Void> clean() {
        operLogService.cleanOperLog();
        return R.ok();
    }
}
