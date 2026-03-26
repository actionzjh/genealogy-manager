package com.genealogy.controller;

import com.genealogy.entity.Genealogy;
import com.genealogy.entity.Person;
import com.genealogy.repository.GenealogyRepository;
import com.genealogy.repository.PersonRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 字辈查询控制器
 * 支持按字辈字、位置、世代范围、性别等多条件筛选查询
 */
@RestController
@RequestMapping("/api/zi")
public class ZiSearchController {

    private final PersonRepository personRepository;
    private final GenealogyRepository genealogyRepository;

    public ZiSearchController(PersonRepository personRepository, GenealogyRepository genealogyRepository) {
        this.personRepository = personRepository;
        this.genealogyRepository = genealogyRepository;
    }

    /**
     * 字辈智能查询
     * @param ziChar 要查找的字辈字（单个字符）
     * @param position 位置：1=第一个字，2=第二个字，3=第三个字，-1=最后一个字
     * @param nameContains 姓名包含关键词
     * @param genealogyId 家谱ID筛选
     * @param minGeneration 最小世代
     * @param maxGeneration 最大世代
     * @param gender 性别筛选 M/F
     * @param sortBy 排序字段 generation/name/birthYear
     * @param sortOrder 排序方向 asc/desc
     * @return 查询结果列表
     */
    @GetMapping("/search")
    public ResponseEntity<List<ZiSearchResult>> search(
            String ziChar,
            Integer position,
            String nameContains,
            Long genealogyId,
            Integer minGeneration,
            Integer maxGeneration,
            String gender,
            String sortBy,
            String sortOrder) {

        // Get all people filtered by genealogy
        List<Person> people;
        if (genealogyId != null) {
            people = personRepository.findByGenealogyId(genealogyId);
        } else {
            people = personRepository.findAll();
        }

        // Filter in-memory
        final List<ZiSearchResult> results = new ArrayList<>();

        final String finalZiChar = ziChar;
        final Integer finalPosition = position;
        final String finalNameContains = nameContains;
        final Long finalGenealogyId = genealogyId;
        final Integer finalMinGeneration = minGeneration;
        final Integer finalMaxGeneration = maxGeneration;
        final String finalGender = gender;

        for (Person p : people) {
            String name = p.getName();
            if (name == null || name.isEmpty()) continue;

            // Filter by name contains
            if (finalNameContains != null && !finalNameContains.isEmpty() && !name.contains(finalNameContains)) {
                continue;
            }

            // Filter by zi character and position
            Integer matchPosition = null;
            boolean found = false;
            if (finalZiChar != null && !finalZiChar.isEmpty()) {
                if (finalPosition != null) {
                    // Specific position check
                    int posIdx = finalPosition;
                    if (finalPosition == -1) {
                        posIdx = name.length();
                    }
                    if (name.length() >= posIdx) {
                        char c = finalPosition == -1 ? name.charAt(name.length() - 1) : name.charAt(posIdx - 1);
                        if (String.valueOf(c).equals(finalZiChar)) {
                            found = true;
                            matchPosition = finalPosition;
                        }
                    }
                } else {
                    // Search anywhere in name
                    for (int i = 0; i < name.length(); i++) {
                        if (String.valueOf(name.charAt(i)).equals(finalZiChar)) {
                            found = true;
                            matchPosition = i + 1;
                            break;
                        }
                    }
                }
                if (!found) continue;
            }

            // Filter by generation range
            Integer generation = p.getGeneration();
            if (finalMinGeneration != null && generation != null && generation < finalMinGeneration) continue;
            if (finalMaxGeneration != null && generation != null && generation > finalMaxGeneration) continue;

            // Filter by gender
            if (finalGender != null && !finalGender.isEmpty() && !finalGender.equals(p.getGender())) continue;

            // Get genealogy name
            String genealogyName = null;
            if (p.getGenealogyId() != null) {
                Genealogy g = genealogyRepository.findById(p.getGenealogyId()).orElse(null);
                if (g != null) {
                    genealogyName = g.getName();
                }
            }

            // Add result
            ZiSearchResult result = new ZiSearchResult(
                    p.getId(),
                    name,
                    p.getBirthYear() != null ? parseBirthYear(p.getBirthYear()) : null,
                    p.getGender(),
                    generation,
                    p.getGenealogyId(),
                    genealogyName,
                    matchPosition
            );
            results.add(result);
        }

        // Sort results
        sortResults(results, sortBy, sortOrder);

        return ResponseEntity.ok(results);
    }

    /**
     * Parse birthYear from String to Integer
     */
    private Integer parseBirthYear(String birthYearStr) {
        if (birthYearStr == null || birthYearStr.isEmpty()) return null;
        try {
            return Integer.parseInt(birthYearStr.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Sort results according to parameters
     */
    private void sortResults(List<ZiSearchResult> results, String sortBy, String sortOrder) {
        final String actualSortBy = sortBy == null ? "generation" : sortBy;
        final boolean asc = sortOrder == null || "asc".equals(sortOrder);

        results.sort((a, b) -> {
            int cmp = 0;
            switch (actualSortBy) {
                case "name":
                    cmp = compareString(a.getName(), b.getName());
                    break;
                case "birthYear":
                    cmp = compareInteger(a.getBirthYear(), b.getBirthYear());
                    break;
                case "generation":
                default:
                    cmp = compareInteger(a.getGeneration(), b.getGeneration());
                    break;
            }
            return asc ? cmp : -cmp;
        });
    }

    private int compareString(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }

    private int compareInteger(Integer a, Integer b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }

    /**
     * 查询结果DTO
     */
    public static class ZiSearchResult {
        private Long id;
        private String name;
        private Integer birthYear;
        private String gender;
        private Integer generation;
        private Long genealogyId;
        private String genealogyName;
        private Integer matchPosition;

        public ZiSearchResult(Long id, String name, Integer birthYear, String gender,
                              Integer generation, Long genealogyId, String genealogyName, Integer matchPosition) {
            this.id = id;
            this.name = name;
            this.birthYear = birthYear;
            this.gender = gender;
            this.generation = generation;
            this.genealogyId = genealogyId;
            this.genealogyName = genealogyName;
            this.matchPosition = matchPosition;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getBirthYear() {
            return birthYear;
        }

        public void setBirthYear(Integer birthYear) {
            this.birthYear = birthYear;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public Integer getGeneration() {
            return generation;
        }

        public void setGeneration(Integer generation) {
            this.generation = generation;
        }

        public Long getGenealogyId() {
            return genealogyId;
        }

        public void setGenealogyId(Long genealogyId) {
            this.genealogyId = genealogyId;
        }

        public String getGenealogyName() {
            return genealogyName;
        }

        public void setGenealogyName(String genealogyName) {
            this.genealogyName = genealogyName;
        }

        public Integer getMatchPosition() {
            return matchPosition;
        }

        public void setMatchPosition(Integer matchPosition) {
            this.matchPosition = matchPosition;
        }
    }
}
