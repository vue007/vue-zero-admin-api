package com.zero.admin.member.domain.model;

import lombok.Builder;
import lombok.Value;

/** 已由第三方平台验证通过的会员身份。 */
@Value
@Builder
public class MemberSocialIdentity {
    String source;
    String openId;
    String unionId;
    String username;
    String nickname;
    String avatar;
}
