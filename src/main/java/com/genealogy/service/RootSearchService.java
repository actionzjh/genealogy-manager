package com.genealogy.service;

import com.genealogy.entity.Genealogy;
import com.genealogy.entity.RootSearch;
import com.genealogy.repository.GenealogyRepository;
import com.genealogy.repository.RootSearchRepository;
import com.genealogy.service.RootSearchNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 寻根匹配服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RootSearchService {

    private final RootSearchRepository rootSearchRepository;
    private final GenealogyRepository genealogyRepository;

    public Optional<RootSearch> findById(Long id) {
        return rootSearchRepository.findById(id);
    }
    private final RootSearchNotificationService notificationService;

    /**
     * 匹配结果DTO
     */
    @Data
    @AllArgsConstructor
    public static class MatchResult {
        private RootSearch search;
        private double score;
        private String matchReason;
    }

    /**
     * 发布寻根启事
     */
    @Transactional
    public Result create(RootSearch search, Long userId) {
        search.setUserId(userId);
        if (search.getSurname() == null || search.getSurname().trim().isEmpty()) {
            return Result.error("姓氏不能为空");
        }
        RootSearch saved = rootSearchRepository.save(search);
        log.info("发布寻根启事成功: id={}, 用户={}", saved.getId(), userId);
        return Result.success(saved);
    }

    /**
     * 更新寻根启事状态
     */
    @Transactional
    public Result updateStatus(Long id, Long userId, String status) {
        Optional<RootSearch> opt = rootSearchRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.error("寻根启事不存在");
        }
        RootSearch search = opt.get();
        if (!search.getUserId().equals(userId)) {
            return Result.error("无权修改此寻根启事");
        }
        search.setStatus(status);
        rootSearchRepository.save(search);
        return Result.success(search);
    }

    /**
     * 删除寻根启事
     */
    @Transactional
    public Result delete(Long id, Long userId) {
        Optional<RootSearch> opt = rootSearchRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.error("寻根启事不存在");
        }
        RootSearch search =opt.get();
        if (!search.getUserId().equals(userId)) {
            return Result.error("无权删除此寻根启事");
        }
        rootSearchRepository.deleteById(id);
        return Result.success(null);
    }

    /**
     * 获取公开的寻根列表（分页）
     */
    public Page<RootSearch> listPublic(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return rootSearchRepository.findByIsPublicTrueAndStatusOrderByCreatedAtDesc("open", pageable);
    }

    /**
     * 获取用户自己发布的
     */
    public List<RootSearch> listMy(Long userId) {
        return rootSearchRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 智能匹配：对当前寻根启事，匹配可能的家谱
     * 匹配维度：
     * 1. 姓氏完全匹配（权重 40%）
     * 2. 字辈相似度（权重 30%）
     * 3. 祖籍地名相似度（权重 20%）
     * 4. 始迁祖姓名匹配（权重 10%）
     */
    public List<MatchResult> match(RootSearch search) {
        List<MatchResult> results = new ArrayList<>();

        // 先找同姓氏的家谱
        List<Genealogy> candidates;
        if (search.getSurname() != null && !search.getSurname().isEmpty()) {
            candidates = genealogyRepository.findBySurnameContainingIgnoreCase(search.getSurname());
        } else {
            candidates = genealogyRepository.findAll();
        }

        // 对每个家谱计算匹配分数
        for (Genealogy g : candidates) {
            // 只匹配公开可访问的家谱（简化：所有用户创建的都可匹配）
            double score = calculateMatchScore(search, g);
            if (score >= 30) { // 只返回分数>=30的
                // 找到这个家谱对应的寻根启事（如果有的话）
                Optional<RootSearch> matchingSearch = rootSearchRepository.findById(g.getId());
                RootSearch resultSearch = matchingSearch.orElse(null);
                if (resultSearch == null) {
                    // 创建一个虚拟的寻根结果
                    resultSearch = new RootSearch();
                    resultSearch.setId(g.getId());
                    resultSearch.setUserId(g.getUserId());
                    resultSearch.setSurname(g.getSurname());
                    resultSearch.setDescription(g.getDescription());
                    resultSearch.setOriginPlace(g.getOriginPlace());
                }

                String reason = buildMatchReason(search, g, score);
                results.add(new MatchResult(resultSearch, score, reason));
            }
        }

        // 按分数降序排序
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // 找到高分匹配 (>60分) 给发布者发送通知
        if (search.getId() != null) {
            for (MatchResult mr : results) {
                if (mr.getScore() >= 60) {
                    // 给寻根启事发布者发送通知
                    RootSearch resultSearch = mr.getSearch();
                    if (!resultSearch.getUserId().equals(search.getUserId())) {
                        // 对方家谱找到了你的寻根，通知你
                        notificationService.createNotification(
                                search.getUserId(),
                                search.getId(),
                                null,
                                mr.getScore()
                        );
                        // 同时给对方也发通知，说你匹配到了他
                        notificationService.createNotification(
                                resultSearch.getUserId(),
                                resultSearch.getId(),
                                search.getId(),
                                mr.getScore()
                        );
                    }
                }
            }
        }

        // 只返回前20个
        if (results.size() > 20) {
            return results.subList(0, 20);
        }
        return results;
    }

    /**
     * 计算匹配分数 0-100
     */
    private double calculateMatchScore(RootSearch search, Genealogy genealogy) {
        double score = 0;

        // 1. 姓氏匹配（40分）
        if (isSameSurname(search.getSurname(), genealogy.getSurname())) {
            score += 40;
        } else if (contains(search.getSurname(), genealogy.getSurname())) {
            score += 20; // 部分匹配减半
        }

        // 2. 字辈相似度（最多30分）
        if (search.getGenerations() != null && !search.getGenerations().isEmpty()
            && genealogy.getDescription() != null) {
            double ziScore = calculateZipperSimilarity(search.getGenerations(), genealogy.getDescription());
            score += ziScore * 30;
        }

        // 3. 祖籍地名匹配（最多20分）
        if (search.getOriginPlace() != null && !search.getOriginPlace().isEmpty()
            && genealogy.getOriginPlace() != null && !genealogy.getOriginPlace().isEmpty()) {
            double placeScore = calculatePlaceSimilarity(search.getOriginPlace(), genealogy.getOriginPlace());
            score += placeScore * 20;
        }

        // 4. 始迁祖姓名匹配（最多10分）
        if (search.getAncestorName() != null && !search.getAncestorName().isEmpty()
            && genealogy.getFounderId() != null) {
            // 简化：直接比较名称包含
            // 完整版需要查始迁祖人物姓名，这里简化处理
            if (contains(search.getAncestorName(), genealogy.getName())) {
                score += 10;
            }
        }

        return score;
    }

    /**
     * 判断姓氏是否相同
     */
    private boolean isSameSurname(String s1, String s2) {
        if (s1 == null || s2 == null) return false;
        return s1.trim().equalsIgnoreCase(s2.trim());
    }

    private boolean contains(String a, String b) {
        if (a == null || b == null) return false;
        return a.toLowerCase().contains(b.toLowerCase()) || b.toLowerCase().contains(a.toLowerCase());
    }

    /**
     * 计算字辈相似度 - 共同字辈越长分数越高
     */
    private double calculateZipperSimilarity(String searchZipper, String genealogyText) {
        // 分词：searchZipper 是空格分隔的字辈
        List<String> searchWords = Arrays.asList(searchZipper.trim().split("\\s+"));
        int matchCount = 0;
        for (String word : searchWords) {
            if (genealogyText.toLowerCase().contains(word.toLowerCase())) {
                matchCount++;
            }
        }
        if (searchWords.isEmpty()) return 0;
        return (double) matchCount / searchWords.size();
    }

    /**
     * 计算地名相似度
     */
    private double calculatePlaceSimilarity(String place1, String place2) {
        // 简化：只要互相包含就给高分
        if (place1.equalsIgnoreCase(place2)) {
            return 1.0;
        }
        if (place1.contains(place2) || place2.contains(place1)) {
            return 0.8;
        }
        // 检查省/市是否匹配
        String p1Province = extractProvince(place1);
        String p2Province = extractProvince(place2);
        if (p1Province != null && p2Province != null && p1Province.equalsIgnoreCase(p2Province)) {
            return 0.5;
        }
        return 0;
    }

    /**
     * 提取省份名（简化）
     */
    private String extractProvince(String place) {
        if (place == null || place.isEmpty()) return null;
        // 取第一个词（一般是省）
        String[] parts = place.split("[省|市|县|区]");
        if (parts.length > 0) {
            return parts[0];
        }
        return null;
    }

    private String buildMatchReason(RootSearch search, Genealogy g, double score) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("匹配总分: %.1f", score));
        sb.append(" - ");

        if (isSameSurname(search.getSurname(), g.getSurname())) {
            sb.append("姓氏匹配");
        } else if (contains(search.getSurname(), g.getSurname())) {
            sb.append("姓氏部分匹配");
        } else {
            sb.append("姓氏不匹配");
        }

        return sb.toString();
    }

    // 返回结果封装
    @Data
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
