package com.efh.community.service;

/**
 * 社区行为积分奖励
 */
public interface PointsRewardService {

    int POST_CREATE = 10;
    int COMMENT_CREATE = 5;
    int POST_RECEIVED_LIKE = 2;
    int COMMENT_RECEIVED_LIKE = 1;

    void reward(Long userId, int points, String reason);
}
