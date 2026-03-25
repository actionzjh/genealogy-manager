package com.genealogy.util;

import com.genealogy.entity.Genealogy;
import com.genealogy.entity.Person;
import com.genealogy.entity.Family;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GEDCOM 5.5 文件导入解析器
 */
@Slf4j
public class GedcomImporter {

    private final Long userId;
    private final Genealogy genealogy;
    private final Map<String, Person> indiMap = new HashMap<>(); // XREF -> Person
    private final List<Family> families = new ArrayList<>();
    private final Map<String, String> nameMap = new HashMap<>(); // 姓名 -> ID映射

    public GedcomImporter(Long userId, Genealogy genealogy) {
        this.userId = userId;
        this.genealogy = genealogy;
    }

    /**
     * 解析GEDCOM文件
     */
    public ParseResult parse(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        List<GedcomLine> lines = new ArrayList<>();

        String line;
        int level = 0;
        String tag = "";
        String xref = "";
        String value = "";

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // 解析GEDCOM行格式: level [xref] tag [value]
            String[] parts = line.split("\\s+", 3);
            if (parts.length < 2) continue;

            try {
                level = Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) {
                continue;
            }

            GedcomLine gl = new GedcomLine();
            gl.level = level;

            if (parts[1].startsWith("@")) {
                // 这是xref
                gl.xref = parts[1];
                if (parts.length > 2) {
                    gl.tag = parts[2];
                }
            } else {
                gl.tag = parts[1];
                if (parts.length > 2) {
                    gl.value = parts[2];
                    for (int i = 3; i < parts.length; i++) {
                        gl.value += " " + parts[i];
                    }
                }
            }

            lines.add(gl);
        }

        // 解析结构
        parseStructure(lines);

        // 处理关系映射
        mapRelationships();

        ParseResult result = new ParseResult();
        result.genealogy = genealogy;
        result.persons = new ArrayList<>(indiMap.values());
        result.families = families;
        result.totalPersons = indiMap.size();
        result.totalFamilies = families.size();

        return result;
    }

    private void parseStructure(List<GedcomLine> lines) {
        int i = 0;
        while (i < lines.size()) {
            GedcomLine line = lines.get(i);

            if (line.level == 0 && "INDI".equals(line.tag) && line.xref != null) {
                // 个人记录
                Person person = parseIndividual(line.xref, lines, i);
                if (person != null) {
                    indiMap.put(line.xref, person);
                    if (person.getName() != null && !person.getName().isEmpty()) {
                        nameMap.put(person.getName().trim(), person.getId().toString());
                    }
                }
                // 跳到下一个顶级记录
                while (i < lines.size() && lines.get(i).level != 0) {
                    i++;
                }
            } else if (line.level == 0 && "FAM".equals(line.tag) && line.xref != null) {
                // 家庭记录
                Family family = parseFamily(line.xref, lines, i);
                if (family != null) {
                    families.add(family);
                }
                // 跳到下一个顶级记录
                while (i < lines.size() && lines.get(i).level != 0) {
                    i++;
                }
            } else if (line.level == 0 && "HEAD".equals(line.tag)) {
                // 文件头，跳过
                i++;
            } else if (line.level == 0 && "TRLR".equals(line.tag)) {
                // 文件尾，结束
                break;
            } else {
                i++;
            }
        }
    }

    private Person parseIndividual(String xref, List<GedcomLine> lines, int startIndex) {
        Person person = new Person();
        person.setUserId(userId);
        person.setGenealogyId(genealogy.getId());

        int i = startIndex + 1;
        while (i < lines.size() && lines.get(i).level > 0) {
            GedcomLine line = lines.get(i);

            switch (line.tag.toUpperCase()) {
                case "NAME":
                    // GEDCOM name format: /Surname/ Given /Middle/
                    String nameStr = line.value;
                    // 提取姓氏和名
                    Pattern pattern = Pattern.compile("/([^/]+)/");
                    Matcher matcher = pattern.matcher(nameStr);
                    StringBuilder surname = new StringBuilder();
                    String given = nameStr.replaceAll("/[^/]+/", "").trim();
                    while (matcher.find()) {
                        if (surname.length() > 0) surname.append(" ");
                        surname.append(matcher.group(1));
                    }
                    String fullName = given;
                    if (surname.length() > 0) {
                        fullName = surname + " " + given;
                    }
                    person.setName(fullName.trim());
                    break;
                case "SEX":
                    String sex = line.value;
                    if ("M".equals(sex)) {
                        person.setGender("M");
                    } else if ("F".equals(sex)) {
                        person.setGender("F");
                    } else {
                        person.setGender("U");
                    }
                    break;
                case "BIRT":
                    // 出生事件，找下一级DATE
                    person = parseEventDate(person, "birth", lines, i);
                    break;
                case "DEAT":
                    // 死亡事件
                    person = parseEventDate(person, "death", lines, i);
                    break;
                case "NOTE":
                    // 注释，加到biography
                    if (person.getBiography() == null) {
                        person.setBiography("");
                    }
                    person.setBiography(person.getBiography() + "\n" + line.value);
                    break;
                case "OCCU":
                    person.setOccupation(line.value);
                    break;
                case "TITL":
                    person.setTitle(line.value);
                    break;
                case "PLAC":
                    // 地点，先存到迁徙路径
                    if (person.getMigrationPath() == null) {
                        person.setMigrationPath(line.value);
                    } else {
                        person.setMigrationPath(person.getMigrationPath() + " -> " + line.value);
                    }
                    break;
                default:
                    break;
            }

            i++;
        }

        // 设置生成世代暂时为0，后续计算
        if (person.getGeneration() == null) {
            person.setGeneration(0);
        }

        return person;
    }

    private Person parseEventDate(Person person, String type, List<GedcomLine> lines, int startIndex) {
        int i = startIndex + 1;
        while (i < lines.size() && lines.get(i).level > 1) {
            if (lines.get(i).level == 2 && "DATE".equals(lines.get(i).tag)) {
                String dateStr = lines.get(i).value;
                // 尝试解析日期
                try {
                    // 简化处理，只保存年份字符串
                    if ("birth".equals(type)) {
                        person.setBirthYear(dateStr);
                    } else if ("death".equals(type)) {
                        person.setDeathYear(dateStr);
                    }
                } catch (Exception e) {
                    log.debug("无法解析日期: {}", dateStr);
                }
            }
            i++;
        }
        return person;
    }

    private Family parseFamily(String xref, List<GedcomLine> lines, int startIndex) {
        Family family = new Family();
        family.setUserId(userId);
        family.setGenealogyId(genealogy.getId());

        int i = startIndex + 1;
        while (i < lines.size() && lines.get(i).level > 0) {
            GedcomLine line = lines.get(i);

            switch (line.tag.toUpperCase()) {
                case "HUSB":
                    // 丈夫
                    String husbandXref = line.value;
                    if (indiMap.containsKey(husbandXref)) {
                        family.setFatherId(getIdByXref(husbandXref));
                    }
                    break;
                case "WIFE":
                    // 妻子
                    String wifeXref = line.value;
                    if (indiMap.containsKey(wifeXref)) {
                        family.setMotherId(getIdByXref(wifeXref));
                    }
                    break;
                case "CHIL":
                    // 子女，多个CHIL就是多个子女
                    String childXref = line.value;
                    String childIds = family.getChildIds();
                    if (childIds == null || childIds.isEmpty()) {
                        family.setChildIds(getIdByXref(childXref).toString());
                    } else {
                        family.setChildIds(childIds + "," + getIdByXref(childXref));
                    }
                    // 更新子女的父母ID
                    if (indiMap.containsKey(childXref)) {
                        Person child = indiMap.get(childXref);
                        if (family.getFatherId() != null) {
                            child.setFatherId(family.getFatherId());
                        }
                        if (family.getMotherId() != null) {
                            child.setMotherId(family.getMotherId());
                        }
                    }
                    break;
                case "MARR":
                    // 婚姻事件，找日期
                    i = parseMarriageDate(family, lines, i);
                    break;
                default:
                    break;
            }

            i++;
        }

        return family;
    }

    private int parseMarriageDate(Family family, List<GedcomLine> lines, int startIndex) {
        int i = startIndex + 1;
        while (i < lines.size() && lines.get(i).level > 1) {
            if (lines.get(i).level == 2 && "DATE".equals(lines.get(i).tag)) {
                family.setMarriageDate(lines.get(i).value);
            }
            i++;
        }
        return i - 1; // 返回当前位置，外层会继续i++
    }

    private Long getIdByXref(String xref) {
        // GEDCOM的xref是 @I1@ 这种格式，提取数字
        String num = xref.replace("@", "").replaceAll("[^0-9]", "");
        try {
            return Long.parseLong(num);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void mapRelationships() {
        // 为每个子女设置父母ID
        for (Family family : families) {
            if (family.getChildIds() != null && !family.getChildIds().isEmpty()) {
                String[] childIds = family.getChildIds().split(",");
                for (String childIdStr : childIds) {
                    try {
                        Long childId = Long.parseLong(childIdStr.trim());
                        // 查找person
                        for (Map.Entry<String, Person> entry : indiMap.entrySet()) {
                            Person p = entry.getValue();
                            if (p.getId() != null && p.getId().equals(childId)) {
                                if (family.getFatherId() != null && p.getFatherId() == null) {
                                    p.setFatherId(family.getFatherId());
                                }
                                if (family.getMotherId() != null && p.getMotherId() == null) {
                                    p.setMotherId(family.getMotherId());
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }
        }

        // 计算世代（从根开始）
        calculateGenerations();
    }

    private void calculateGenerations() {
        // 找到没有父亲的作为始祖，世代设为1
        for (Person person : indiMap.values()) {
            if (person.getFatherId() == null && person.getMotherId() == null) {
                person.setGeneration(1);
                propagateGeneration(person);
            }
        }

        // 还有一些孤立节点，世代设为1
        for (Person person : indiMap.values()) {
            if (person.getGeneration() == null || person.getGeneration() == 0) {
                person.setGeneration(1);
            }
        }

        // 更新家谱最大世代数
        int maxGen = 0;
        for (Person person : indiMap.values()) {
            if (person.getGeneration() != null && person.getGeneration() > maxGen) {
                maxGen = person.getGeneration();
            }
        }
        genealogy.setMaxGeneration(maxGen);
        genealogy.setTotalPeople(indiMap.size());
    }

    private void propagateGeneration(Person person) {
        // 找到这个人的子女，世代+1
        for (Person child : findChildren(person.getId())) {
            if (child.getGeneration() == null || child.getGeneration() <= person.getGeneration()) {
                child.setGeneration(person.getGeneration() + 1);
                propagateGeneration(child);
            }
        }
    }

    private List<Person> findChildren(Long parentId) {
        List<Person> result = new ArrayList<>();
        for (Person person : indiMap.values()) {
            if (parentId.equals(person.getFatherId()) || parentId.equals(person.getMotherId())) {
                result.add(person);
            }
        }
        return result;
    }

    /**
     * 解析结果
     */
    @Data
    public static class ParseResult {
        private Genealogy genealogy;
        private List<Person> persons;
        private List<Family> families;
        private int totalPersons;
        private int totalFamilies;
        private List<String> errors = new ArrayList<>();
    }

    /**
     * GEDCOM行
     */
    @Data
    private static class GedcomLine {
        int level;
        String xref;
        String tag;
        String value;
    }
}
