package com.zero.admin.member.domain.bo;

import com.zero.admin.base.core.xss.Xss;
import com.zero.admin.member.domain.Member;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 密码注册请求。租户从可信应用配置取得，不接受客户端提交。 */
@Data
@AutoMapper(target = Member.class, reverseConvertGenerate = false)
public class MemberRegisterBo {

    @Xss(message = "会员账号不能包含脚本字符")
    @NotBlank(message = "会员账号不能为空")
    @Size(min = 4, max = 30, message = "会员账号长度必须在{min}到{max}个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度必须在{min}到{max}个字符之间")
    private String password;

    @Xss(message = "会员昵称不能包含脚本字符")
    @Size(max = 30, message = "会员昵称长度不能超过{max}个字符")
    private String nickname;

    @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号码格式不正确")
    private String mobile;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过{max}个字符")
    private String email;
}
