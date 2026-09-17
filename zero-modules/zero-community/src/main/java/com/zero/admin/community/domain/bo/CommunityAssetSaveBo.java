package com.zero.admin.community.domain.bo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 创建或修改社区草稿。作者、租户和应用均由服务端会话提供。 */
@Data
public class CommunityAssetSaveBo {
    @NotBlank(message = "标题不能为空")
    @Size(max = 120, message = "标题不能超过120个字符")
    private String title;

    @Size(max = 1000, message = "摘要不能超过1000个字符")
    private String summary;

    @Size(max = 500, message = "封面地址不能超过500个字符")
    private String coverUrl;

    @NotBlank(message = "配置类型不能为空")
    private String kind;

    @NotBlank(message = "设备类型不能为空")
    private String deviceType;

    private String visibility;

    @NotNull(message = "配置内容不能为空")
    private Object payload;

    @Min(value = 1, message = "协议版本必须大于0")
    @Max(value = 10000, message = "协议版本无效")
    private Integer schemaVersion = 1;

    @NotBlank(message = "来源平台不能为空")
    @Size(max = 64)
    private String sourcePlatformCode;

    @NotBlank(message = "来源产品不能为空")
    @Size(max = 128)
    private String sourceProductCode;

    @Size(max = 64)
    private String sourceCapabilityVersion;

    @Size(max = 500)
    private String changelog;
}
