package com.efh.knowledge.constant;

public final class KnowledgeConstants {
    public static final int USER_TYPE_ADMIN = 9;

    /** 0免费 1积分 2付费 3积分或付费任选 */
    public static final int ACCESS_FREE = 0;
    public static final int ACCESS_POINTS = 1;
    public static final int ACCESS_PAID = 2;
    public static final int ACCESS_BOTH = 3;

    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_OFFLINE = 2;

    public static final String UNLOCK_FREE = "free";
    public static final String UNLOCK_POINTS = "points";
    public static final String UNLOCK_ALIPAY = "alipay";

    private KnowledgeConstants() {
    }
}
