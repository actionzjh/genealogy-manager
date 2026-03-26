package com.genealogy.controller;

import com.genealogy.entity.Person;
import com.genealogy.repository.PersonRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 家谱数据分析控制器
 * 提供全量人物数据用于交互式仪表盘分析
 */
@RestController
@RequestMapping("/api/analysis")
public class GenealogyAnalysisController {

    private final PersonRepository personRepository;

    public GenealogyAnalysisController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    /**
     * 获取所有人数据（可按家谱过滤）
     */
    @GetMapping("/all-people")
    public ResponseEntity<List<PersonAnalysisDTO>> getAllPeople(Long genealogyId) {
        List<Person> people;
        if (genealogyId != null) {
            people = personRepository.findByGenealogyId(genealogyId);
        } else {
            people = personRepository.findAll();
        }

        List<PersonAnalysisDTO> result = new ArrayList<>();
        for (Person p : people) {
            // Try to parse birthYear and deathYear to Integer if possible
            Integer birthYearInt = null;
            Integer deathYearInt = null;
            if (p.getBirthYear() != null && !p.getBirthYear().isEmpty()) {
                try {
                    birthYearInt = Integer.parseInt(p.getBirthYear().trim());
                } catch (NumberFormatException e) {
                    // Keep null if not a valid integer
                }
            }
            if (p.getDeathYear() != null && !p.getDeathYear().isEmpty()) {
                try {
                    deathYearInt = Integer.parseInt(p.getDeathYear().trim());
                } catch (NumberFormatException e) {
                    // Keep null if not a valid integer
                }
            }

            result.add(new PersonAnalysisDTO(
                    p.getId(),
                    p.getName(),
                    p.getGender(),
                    p.getGeneration(),
                    birthYearInt,
                    deathYearInt,
                    p.getGenealogyId()
            ));
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 简化的Person DTO用于数据分析
     */
    public static class PersonAnalysisDTO {
        private Long id;
        private String name;
        private String gender;
        private Integer generation;
        private Integer birthYear;
        private Integer deathYear;
        private Long genealogyId;

        public PersonAnalysisDTO(Long id, String name, String gender,
                                  Integer generation, Integer birthYear,
                                  Integer deathYear, Long genealogyId) {
            this.id = id;
            this.name = name;
            this.gender = gender;
            this.generation = generation;
            this.birthYear = birthYear;
            this.deathYear = deathYear;
            this.genealogyId = genealogyId;
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

        public Integer getBirthYear() {
            return birthYear;
        }

        public void setBirthYear(Integer birthYear) {
            this.birthYear = birthYear;
        }

        public Integer getDeathYear() {
            return deathYear;
        }

        public void setDeathYear(Integer deathYear) {
            this.deathYear = deathYear;
        }

        public Long getGenealogyId() {
            return genealogyId;
        }

        public void setGenealogyId(Long genealogyId) {
            this.genealogyId = genealogyId;
        }
    }
}
