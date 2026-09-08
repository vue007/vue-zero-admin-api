package com.zero.admin.consumer.domain.vo;

import com.zero.admin.member.domain.vo.MemberVo;
import lombok.Builder;
import lombok.Value;

/** 面向会员本人的最小资料视图。 */
@Value
@Builder
public class ConsumerMemberProfileVo {
    Long memberId;
    String username;
    String nickname;
    String mobile;
    String email;
    String avatar;

    public static ConsumerMemberProfileVo from(MemberVo member) {
        return ConsumerMemberProfileVo.builder()
            .memberId(member.getMemberId())
            .username(member.getUsername())
            .nickname(member.getNickname())
            .mobile(member.getMobile())
            .email(member.getEmail())
            .avatar(member.getAvatar())
            .build();
    }
}
