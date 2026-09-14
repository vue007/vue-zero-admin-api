package com.zero.admin.partner.domain.bo;

import com.zero.admin.base.core.xss.Xss;
import com.zero.admin.base.mybatis.core.domain.BaseEntity;
import com.zero.admin.partner.domain.Partner;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 合作客户查询及维护对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Partner.class, reverseConvertGenerate = false)
public class PartnerBo extends BaseEntity {

    private Long partnerId;

    @Xss(message = "合作客户编码不能包含脚本字符")
    @NotBlank(message = "合作客户编码不能为空")
    @Size(max = 32, message = "合作客户编码长度不能超过{max}个字符")
    private String partnerCode;

    @Xss(message = "合作客户名称不能包含脚本字符")
    @NotBlank(message = "合作客户名称不能为空")
    @Size(max = 100, message = "合作客户名称长度不能超过{max}个字符")
    private String partnerName;

    @Size(max = 32, message = "统一社会信用代码长度不能超过{max}个字符")
    private String creditCode;

    @Xss(message = "联系人不能包含脚本字符")
    @Size(max = 50, message = "联系人长度不能超过{max}个字符")
    private String contactName;

    @Size(max = 30, message = "联系电话长度不能超过{max}个字符")
    private String contactPhone;

    @Email(message = "联系邮箱格式不正确")
    @Size(max = 100, message = "联系邮箱长度不能超过{max}个字符")
    private String contactEmail;

    @Xss(message = "地址不能包含脚本字符")
    @Size(max = 255, message = "地址长度不能超过{max}个字符")
    private String address;

    @Pattern(regexp = "[01]", message = "合作客户状态只能为0或1")
    private String status;

    @Size(max = 500, message = "备注长度不能超过{max}个字符")
    private String remark;
}
