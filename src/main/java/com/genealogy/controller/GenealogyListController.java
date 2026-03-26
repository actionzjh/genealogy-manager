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
 * 家谱列表API - 供可视化模块选择家谱
 */
@RestController
@RequestMapping("/api/genealogy")
public class GenealogyListController {

    private final GenealogyRepository genealogyRepository;
    private final PersonRepository personRepository;

    public GenealogyListController(GenealogyRepository genealogyRepository, PersonRepository personRepository) {
        this.genealogyRepository = genealogyRepository;
        this.personRepository = personRepository;
    }

    /**
     * 获取当前用户可见的家谱列表
     */
    @GetMapping("/list")
    public ResponseEntity<List<GenealogyInfo>> list() {
        // FIXME: 这里应该根据当前登录用户过滤权限
        // 暂时返回所有家谱
        List<Genealogy> all = genealogyRepository.findAll();
        List<GenealogyInfo> result = new ArrayList<>();
        for (Genealogy g : all) {
            result.add(new GenealogyInfo(g.getId(), g.getName()));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 获取某个家谱的始祖/根人物列表（没有父亲的人物）
     */
    @GetMapping("/roots")
    public ResponseEntity<List<RootPerson>> getRoots(Long genealogyId) {
        // 查找该家谱中没有父亲的人物作为始祖
        List<Person> all = personRepository.findByGenealogyId(genealogyId);
        List<RootPerson> result = new ArrayList<>();
        for (Person p : all) {
            if (p.getFatherId() == null) {
                result.add(new RootPerson(p.getId(), p.getName()));
            }
        }
        // 如果没有找到始祖，随便选第一个
        if (result.isEmpty() && !all.isEmpty()) {
            Person first = all.get(0);
            result.add(new RootPerson(first.getId(), first.getName()));
        }
        return ResponseEntity.ok(result);
    }

    public static class GenealogyInfo {
        private Long id;
        private String name;

        public GenealogyInfo(Long id, String name) {
            this.id = id;
            this.name = name;
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
    }

    public static class RootPerson {
        private Long id;
        private String name;

        public RootPerson(Long id, String name) {
            this.id = id;
            this.name = name;
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
    }
}
