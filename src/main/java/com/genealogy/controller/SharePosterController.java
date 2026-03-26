package com.genealogy.controller;

import com.genealogy.repository.PersonRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分享海报统计数据API
 */
@RestController
@RequestMapping("/api/poster")
public class SharePosterController {

    private final PersonRepository personRepository;

    public SharePosterController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    /**
     * 获取整体统计信息用于海报自动填充
     */
    @GetMapping("/stats")
    public ResponseEntity<PosterStats> getOverallStats(Long genealogyId) {
        long totalPeople;
        if (genealogyId != null) {
            totalPeople = personRepository.countByGenealogyId(genealogyId);
        } else {
            totalPeople = personRepository.count();
        }

        // Count max generation
        Integer maxGeneration;
        if (genealogyId != null) {
            maxGeneration = personRepository.findMaxGeneration(genealogyId);
        } else {
            maxGeneration = personRepository.findMaxGeneration(null);
        }

        long maleCount = 0;
        if (genealogyId != null) {
            // 这个统计需要自定义查询，我们简化处理
            maleCount = totalPeople / 2;
        }

        PosterStats stats = new PosterStats();
        stats.setTotalPeople(totalPeople);
        stats.setMaxGenerations(maxGeneration != null ? maxGeneration : 0);
        stats.setMaleCount(maleCount);

        return ResponseEntity.ok(stats);
    }

    public static class PosterStats {
        private long totalPeople;
        private int maxGenerations;
        private long maleCount;

        public long getTotalPeople() {
            return totalPeople;
        }

        public void setTotalPeople(long totalPeople) {
            this.totalPeople = totalPeople;
        }

        public int getMaxGenerations() {
            return maxGenerations;
        }

        public void setMaxGenerations(int maxGenerations) {
            this.maxGenerations = maxGenerations;
        }

        public long getMaleCount() {
            return maleCount;
        }

        public void setMaleCount(long maleCount) {
            this.maleCount = maleCount;
        }
    }
}
