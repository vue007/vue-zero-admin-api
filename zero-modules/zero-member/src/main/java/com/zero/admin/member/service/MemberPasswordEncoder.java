package com.zero.admin.member.service;

import com.zero.admin.base.core.utils.StringUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

/** 会员密码 BCrypt 编解码边界。 */
@Component
public class MemberPasswordEncoder {

    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        if (StringUtils.isBlank(rawPassword) || StringUtils.isBlank(encodedPassword)
            || encodedPassword.length() != 60 || !encodedPassword.startsWith("$2a$")) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}
