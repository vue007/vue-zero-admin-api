package com.zero.admin.community.constant;

import java.util.Set;

/** 社区领域固定协议。 */
public final class CommunityConstants {

    public static final String KIND_DPI = "dpi";
    public static final String KIND_MACRO = "macro";
    public static final String KIND_LIGHTING = "lighting";
    public static final String KIND_PROFILE = "profile";
    public static final Set<String> KINDS = Set.of(KIND_DPI, KIND_MACRO, KIND_LIGHTING, KIND_PROFILE);

    public static final Set<String> DEVICE_TYPES = Set.of(
        "mouse", "keyboard", "gamepad", "headset", "speaker", "any"
    );

    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_PUBLISHED = "published";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_HIDDEN = "hidden";
    public static final Set<String> STATUSES = Set.of(
        STATUS_DRAFT, STATUS_PENDING, STATUS_PUBLISHED, STATUS_REJECTED, STATUS_HIDDEN
    );

    public static final String VISIBILITY_PUBLIC = "public";
    public static final String VISIBILITY_PRIVATE = "private";
    public static final int MAX_PAYLOAD_BYTES = 1024 * 1024;

    private CommunityConstants() {
    }
}
