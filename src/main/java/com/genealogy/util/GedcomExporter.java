package com.genealogy.util;

import com.genealogy.entity.Person;
import com.genealogy.entity.Family;
import com.genealogy.service.PersonService;
import com.genealogy.service.FamilyService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * GEDCOM 5.5 格式导出工具
 */
public class GedcomExporter {

    private final PersonService personService;
    private final FamilyService familyService;
    private final StringBuilder sb = new StringBuilder();

    public GedcomExporter(PersonService personService, FamilyService familyService) {
        this.personService = personService;
        this.familyService = familyService;
    }

    /**
     * 导出整个家谱为GEDCOM格式
     */
    public String export(Long genealogyId, String title) {
        // 写入头信息
        writeHeader(title);

        // 写入所有个人
        List<Person> allPersons = personService.findAll();
        int index = 1;
        for (Person person : allPersons) {
            writeIndividual(person, index++);
        }

        // 写入所有家庭
        List<Family> allFamilies = familyService.findByGenealogyId(genealogyId);
        int familyIndex = 1;
        for (Family family : allFamilies) {
            writeFamily(family, familyIndex++);
        }

        // 写入尾信息
        writeTrailer();

        return sb.toString();
    }

    private void writeHeader(String title) {
        sb.append("0 HEAD\n");
        sb.append("1 SOUR GENEALOGY-MANAGER\n");
        sb.append("1 NAME 家谱管理系统\n");
        sb.append("1 GEDC\n");
        sb.append("2 VERS 5.5\n");
        sb.append("2 FORM LINEAGE-LINKED\n");
        sb.append("1 CHAR UTF-8\n");
        sb.append("1 FILE ").append(title).append(".ged\n");
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        sb.append("1 DATE ").append(date.toUpperCase()).append("\n");
        sb.append("1 SUBM @SUBM1@\n");
        sb.append("0 @SUBM1@ SUBM\n");
        sb.append("1 NAME 家谱管理系统用户\n");
    }

    private void writeIndividual(Person person, int index) {
        String xref = "@I" + index + "@";
        sb.append("0 ").append(xref).append(" INDI\n");

        // 姓名
        String surname = person.getName().length() > 0 ? 
            person.getName().substring(0, 1) : person.getName();
        String given = person.getName().length() > 1 ? 
            person.getName().substring(1) : "";
        sb.append("1 NAME ").append(given).append(" /").append(surname).append("/\n");

        // 字/号
        if (person.getStyleName() != null && !person.getStyleName().isEmpty()) {
            sb.append("1 NOTE 字: ").append(person.getStyleName()).append("\n");
        }
        if (person.getHao() != null && !person.getHao().isEmpty()) {
            sb.append("1 NOTE 号: ").append(person.getHao()).append("\n");
        }

        // 性别
        if (person.getGender() != null) {
            if ("M".equals(person.getGender())) {
                sb.append("1 SEX M\n");
            } else if ("F".equals(person.getGender())) {
                sb.append("1 SEX F\n");
            } else {
                sb.append("1 SEX U\n");
            }
        } else {
            sb.append("1 SEX U\n");
        }

        // 头衔/职位
        if (person.getTitle() != null && !person.getTitle().isEmpty()) {
            sb.append("1 TITL ").append(person.getTitle()).append("\n");
        }
        if (person.getOccupation() != null && !person.getOccupation().isEmpty()) {
            sb.append("1 OCCU ").append(person.getOccupation()).append("\n");
        }

        // 出生日期
        if (person.getBirthYear() != null && !person.getBirthYear().isEmpty()) {
            sb.append("1 BIRT\n");
            sb.append("2 DATE ").append(person.getBirthYear()).append("\n");
        }

        // 死亡日期
        if (person.getDeathYear() != null && !person.getDeathYear().isEmpty()) {
            sb.append("1 DEAT\n");
            sb.append("2 DATE ").append(person.getDeathYear()).append("\n");
        }

        // 父亲
        if (person.getFatherId() != null) {
            // 父亲在GEDCOM中的xref是 @I{fatherId}@ 这里简化处理，实际应该映射正确ID
            sb.append("1 FAMC @F").append(person.getFatherId()).append("@\n");
        }

        // 支系信息
        if (person.getBranch() != null && !person.getBranch().isEmpty()) {
            sb.append("1 NOTE 支系: ").append(person.getBranch()).append("\n");
        }

        // 迁徙路线
        if (person.getMigrationPath() != null && !person.getMigrationPath().isEmpty()) {
            sb.append("1 NOTE 迁徙: ").append(person.getMigrationPath()).append("\n");
        }

        // 功绩
        if (person.getAchievements() != null && !person.getAchievements().isEmpty()) {
            String[] lines = person.getAchievements().split("\n");
            sb.append("1 NOTE 功绩: ").append(lines[0]).append("\n");
            for (int i = 1; i < lines.length; i++) {
                sb.append("2 CONT ").append(lines[i]).append("\n");
            }
        }

        // 传记
        if (person.getBiography() != null && !person.getBiography().isEmpty()) {
            String[] lines = person.getBiography().split("\n");
            sb.append("1 NOTE 传记: ").append(lines[0]).append("\n");
            for (int i = 1; i < lines.length; i++) {
                sb.append("2 CONT ").append(lines[i]).append("\n");
            }
        }
    }

    private void writeFamily(Family family, int index) {
        String xref = "@F" + index + "@";
        sb.append("0 ").append(xref).append(" FAM\n");

        // 丈夫
        if (family.getFatherId() != null) {
            sb.append("1 HUSB @I").append(family.getFatherId()).append("@\n");
        }

        // 妻子
        if (family.getMotherId() != null) {
            sb.append("1 WIFE @I").append(family.getMotherId()).append("@\n");
        }

        // 结婚日期
        if (family.getMarriageDate() != null && !family.getMarriageDate().isEmpty()) {
            sb.append("1 MARR\n");
            sb.append("2 DATE ").append(family.getMarriageDate()).append("\n");
        }

        // 备注
        if (family.getRemark() != null && !family.getRemark().isEmpty()) {
            sb.append("1 NOTE ").append(family.getRemark()).append("\n");
        }
    }

    private void writeTrailer() {
        sb.append("0 TRLR\n");
    }
}
