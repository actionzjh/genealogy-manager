package com.genealogy.service;

import com.genealogy.entity.UserFavorite;
import com.genealogy.repository.UserFavoriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户收藏关注服务 - 关注家谱/寻根启事，有更新主动通知
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFavoriteService {

    private final UserFavoriteRepository favoriteRepository;

    /**
     * 添加收藏
     */
    @Transactional
    public Result add(Long userId, String type, Long targetId) {
        Optional<UserFavorite> existing = favoriteRepository.findByUserIdAndTargetId(userId, targetId);
        if (existing.isPresent()) {
            return Result.error("已经收藏过了");
        }
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setType(type);
        favorite.setTargetId(targetId);
        favoriteRepository.save(favorite);
        log.info("用户收藏成功: userId={}, type={}, targetId={}", userId, type, targetId);
        return Result.success(favorite);
    }

    /**
     * 取消收藏
     */
    @Transactional
    public Result remove(Long userId, Long targetId) {
        favoriteRepository.deleteByUserIdAndTargetId(userId, targetId);
        log.info("用户取消收藏成功: userId={}, targetId={}", userId, targetId);
        return Result.success(null);
    }

    /**
     * 查询用户收藏列表
     */
    public List<UserFavorite> list(Long userId, String type) {
        return favoriteRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type);
    }

    /**
     * 判断用户是否已收藏
     */
    public boolean isFavorite(Long userId, Long targetId) {
        return favoriteRepository.findByUserIdAndTargetId(userId, targetId).isPresent();
    }

    @lombok.Data
    public static class Result {
        private boolean success;
        private String message;
        private Object data;

        public static Result success(Object data) {
            Result r = new Result();
            r.setSuccess(true);
            r.setMessage("成功");
            r.setData(data);
            return r;
        }

        public static Result error(String message) {
            Result r = new Result();
            r.setSuccess(false);
            r.setMessage(message);
            return r;
        }
    }
}
