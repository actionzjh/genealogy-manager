package com.genealogy.service;

import com.genealogy.entity.Person;
import com.genealogy.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * 高级搜索服务 - 多条件组合筛选
 */
@Service
public class AdvancedSearchService {

    @Autowired
    private PersonRepository personRepository;

    /**
     * 高级搜索条件
     */
    public static class SearchFilter {
        private String keyword;      // 全文关键词
        private String surname;      // 姓氏
        private String name;          // 名字
        private Integer generation;   // 世代
        private String gender;        // 性别
        private String birthYearMin;  // 出生年份范围
        private String birthYearMax;
        private String deathYearMin;  // 逝世年份范围
        private String deathYearMax;
        private String branch;        // 支系
        private Long genealogyId;     // 所属家谱

        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }
        public String getSurname() { return surname; }
        public void setSurname(String surname) { this.surname = surname; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getGeneration() { return generation; }
        public void setGeneration(Integer generation) { this.generation = generation; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public String getBirthYearMin() { return birthYearMin; }
        public void setBirthYearMin(String birthYearMin) { this.birthYearMin = birthYearMin; }
        public String getBirthYearMax() { return birthYearMax; }
        public void setBirthYearMax(String birthYearMax) { this.birthYearMax = birthYearMax; }
        public String getDeathYearMin() { return deathYearMin; }
        public void setDeathYearMin(String deathYearMin) { this.deathYearMin = deathYearMin; }
        public String getDeathYearMax() { return deathYearMax; }
        public void setDeathYearMax(String deathYearMax) { this.deathYearMax = deathYearMax; }
        public String getBranch() { return branch; }
        public void setBranch(String branch) { this.branch = branch; }
        public Long getGenealogyId() { return genealogyId; }
        public void setGenealogyId(Long genealogyId) { this.genealogyId = genealogyId; }
    }

    /**
     * 多条件组合搜索
     */
    public List<Person> advancedSearch(SearchFilter filter, Long userId) {
        Specification<Person> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 必须匹配用户ID
            predicates.add(cb.equal(root.get("userId"), userId));

            // 家谱ID筛选
            if (filter.getGenealogyId() != null) {
                predicates.add(cb.equal(root.get("genealogyId"), filter.getGenealogyId()));
            }

            // 姓氏筛选
            if (filter.getSurname() != null && !filter.getSurname().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                    cb.lower(cb.literal("%" + filter.getSurname() + "%"))));
            }

            // 名字筛选
            if (filter.getName() != null && !filter.getName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                    cb.lower(cb.literal("%" + filter.getName() + "%"))));
            }

            // 世代筛选
            if (filter.getGeneration() != null) {
                predicates.add(cb.equal(root.get("generation"), filter.getGeneration()));
            }

            // 性别筛选
            if (filter.getGender() != null && !filter.getGender().isEmpty()) {
                predicates.add(cb.equal(root.get("gender"), filter.getGender()));
            }

            // 支系筛选
            if (filter.getBranch() != null && !filter.getBranch().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("branch")),
                    cb.lower(cb.literal("%" + filter.getBranch() + "%"))));
            }

            // 全文关键词搜索（姓名+传记+成就+职业...）
            if (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) {
                String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
                Predicate p = cb.or(
                    cb.like(cb.lower(root.get("name")), keyword),
                    cb.like(cb.lower(root.get("biography")), keyword),
                    cb.like(cb.lower(root.get("achievements")), keyword),
                    cb.like(cb.lower(root.get("occupation")), keyword),
                    cb.like(cb.lower(root.get("title")), keyword),
                    cb.like(cb.lower(root.get("cemeteryLocation")), keyword)
                );
                predicates.add(p);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return personRepository.findAll(spec);
    }

    /**
     * 全文关键字搜索（简化版）
     */
    public List<Person> keywordSearch(String keyword, Long userId) {
        SearchFilter filter = new SearchFilter();
        filter.setKeyword(keyword);
        return advancedSearch(filter, userId);
    }
}
