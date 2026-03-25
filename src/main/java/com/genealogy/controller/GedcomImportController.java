package com.genealogy.controller;

import com.genealogy.entity.Family;
import com.genealogy.entity.Genealogy;
import com.genealogy.entity.Person;
import com.genealogy.repository.FamilyRepository;
import com.genealogy.repository.GenealogyRepository;
import com.genealogy.repository.PersonRepository;
import com.genealogy.service.GenealogyService;
import com.genealogy.util.GedcomImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * GEDCOM文件导入控制器
 */
@RestController
@RequestMapping("/api/gedcom")
@CrossOrigin(origins = "*")
public class GedcomImportController {

    @Autowired
    private GenealogyRepository genealogyRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private FamilyRepository familyRepository;

    @Autowired
    private GenealogyService genealogyService;

    /**
     * 上传导入GEDCOM文件
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importGedcom(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String genealogyName,
            @RequestParam(value = "surname", required = false) String surname,
            Authentication authentication) {

        Map<String, Object> result = new HashMap<>();

        // 验证登录
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        Long userId = (Long) authentication.getPrincipal();

        try {
            // 创建家谱
            Genealogy genealogy = new Genealogy();
            genealogy.setName(genealogyName);
            genealogy.setSurname(surname);
            genealogy.setUserId(userId);
            genealogy = genealogyRepository.save(genealogy);

            // 解析GEDCOM
            GedcomImporter importer = new GedcomImporter(userId, genealogy);
            GedcomImporter.ParseResult parseResult = importer.parse(file.getInputStream());

            // 保存所有人物
            for (Person person : parseResult.getPersons()) {
                person.setGenealogyId(genealogy.getId());
                person.setUserId(userId);
                personRepository.save(person);
            }

            // 保存所有家庭
            for (Family family : parseResult.getFamilies()) {
                family.setGenealogyId(genealogy.getId());
                family.setUserId(userId);
                familyRepository.save(family);
            }

            // 更新家谱统计
            genealogy.setTotalPeople(parseResult.getTotalPersons());
            genealogy.setMaxGeneration(genealogy.getMaxGeneration());
            genealogyRepository.save(genealogy);

            result.put("code", 0);
            result.put("message", "导入成功");
            result.put("genealogyId", genealogy.getId());
            result.put("totalPersons", parseResult.getTotalPersons());
            result.put("totalFamilies", parseResult.getTotalFamilies());
            result.put("maxGeneration", genealogy.getMaxGeneration());
            result.put("errors", parseResult.getErrors());

            return ResponseEntity.ok(result);

        } catch (IOException e) {
            result.put("code", 500);
            result.put("message", "文件读取失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "导入失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取导入页面信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("supportedVersion", "GEDCOM 5.5");
        result.put("description", "支持从其他家谱软件导入GEDCOM格式文件");
        return ResponseEntity.ok(result);
    }
}
