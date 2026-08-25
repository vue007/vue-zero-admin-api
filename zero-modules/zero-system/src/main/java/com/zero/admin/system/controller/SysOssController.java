package com.zero.admin.system.controller;

import com.zero.admin.base.core.domain.R;
import com.zero.admin.base.log.annotation.Log;
import com.zero.admin.base.log.enums.BusinessType;
import com.zero.admin.base.mybatis.core.page.PageQuery;
import com.zero.admin.base.mybatis.core.page.TableDataInfo;
import com.zero.admin.base.web.core.BaseController;
import com.zero.admin.system.domain.bo.SysOssBo;
import com.zero.admin.system.domain.vo.SysOssUploadVo;
import com.zero.admin.system.domain.vo.SysOssVo;
import com.zero.admin.system.service.ISysOssService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * OSS 文件管理。
 *
 * @author Akai
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/oss")
public class SysOssController extends BaseController {

    private final ISysOssService ossService;

    /**
     * 查询 OSS 文件列表。
     */
    @RequiresPermissions("system:oss:list")
    @GetMapping("/list")
    public TableDataInfo<SysOssVo> list(SysOssBo bo, PageQuery pageQuery) {
        return ossService.queryPageList(bo, pageQuery);
    }

    /**
     * 根据主键批量查询 OSS 文件。
     */
    @RequiresPermissions("system:oss:query")
    @GetMapping("/listByIds/{ossIds}")
    public R<List<SysOssVo>> listByIds(@NotEmpty(message = "主键不能为空")
                                       @PathVariable Long[] ossIds) {
        return R.ok(ossService.listByIds(Arrays.asList(ossIds)));
    }

    /**
     * 上传文件到当前默认对象存储。
     */
    @RequiresPermissions("system:oss:upload")
    @Log(title = "OSS对象存储", businessType = BusinessType.INSERT)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysOssUploadVo> upload(@RequestPart("file") MultipartFile file) {
        SysOssVo oss = ossService.upload(file);
        SysOssUploadVo result = new SysOssUploadVo();
        result.setUrl(oss.getUrl());
        result.setFileName(oss.getOriginalName());
        result.setOssId(oss.getOssId().toString());
        return R.ok(result);
    }

    /**
     * 下载 OSS 文件。
     */
    @RequiresPermissions("system:oss:download")
    @GetMapping("/download/{ossId}")
    public void download(@PathVariable Long ossId, HttpServletResponse response) throws IOException {
        ossService.download(ossId, response);
    }

    /**
     * 删除 OSS 文件及其元数据。
     */
    @RequiresPermissions("system:oss:remove")
    @Log(title = "OSS对象存储", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ossIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ossIds) {
        return toAjax(ossService.deleteWithValidByIds(List.of(ossIds), true));
    }
}
