package com.genealogy.service.pdf;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.genealogy.entity.Genealogy;
import com.genealogy.entity.Person;
import com.genealogy.service.pdf.PdfLayoutStrategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 牒记式排版（传统传记式）
 * 按世代顺序，每个人物一段文字传记，符合传统家谱阅读习惯
 */
public class BiographyLayoutStrategy implements PdfLayoutStrategy {

    @Override
    public String getName() {
        return "biography";
    }

    @Override
    public String getDescription() {
        return "牒记传记式（按世代，每人一段传记文字，传统家谱格式）";
    }

    @Override
    public void layout(Document document, Genealogy genealogy, List<Person> allPersons, PdfFont font) throws IOException {
        // 添加标题页
        addTitlePage(document, genealogy, font);

        // 按世代分组
        List<List<Person>> generationGroups = groupByGeneration(allPersons);

        // 添加目录
        addContentsPage(document, generationGroups.size(), font);

        // 按世代逐页排版，每个人物一段
        int generationNum = 1;
        for (List<Person> generation : generationGroups) {
            addGenerationPage(document, generation, generationNum, font);
            generationNum++;
        }

        // 添加统计页
        addStatisticsPage(document, genealogy, allPersons, generationGroups.size(), font);
    }

    /**
     * 按世代分组
     */
    protected List<List<Person>> groupByGeneration(List<Person> allPersons) {
        List<List<Person>> groups = new ArrayList<>();

        int maxGen = allPersons.stream()
                .map(Person::getGeneration)
                .filter(g -> g != null && g > 0)
                .max(Integer::compareTo)
                .orElse(1);

        for (int i = 0; i <= maxGen; i++) {
            groups.add(new ArrayList<>());
        }

        for (Person person : allPersons) {
            Integer gen = person.getGeneration();
            if (gen == null || gen <= 0 || gen > maxGen) {
                if (groups.size() < 2) {
                    groups.add(new ArrayList<>());
                }
                groups.get(1).add(person);
            } else {
                groups.get(gen).add(person);
            }
        }

        groups.removeIf(List::isEmpty);

        if (groups.isEmpty()) {
            groups.add(allPersons);
        }

        for (List<Person> group : groups) {
            group.sort(Comparator.comparingInt(p -> p.getSortOrder() != null ? p.getSortOrder() : 0));
        }

        return groups;
    }

    /**
     * 添加标题页
     */
    protected void addTitlePage(Document document, Genealogy genealogy, PdfFont font) {
        Paragraph title = new Paragraph(genealogy.getName())
                .setFont(font)
                .setFontSize(28)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold();

        document.add(title);
        document.add(new Paragraph("\n\n"));

        if (genealogy.getSurname() != null) {
            Paragraph surname = new Paragraph(genealogy.getSurname() + "氏家谱")
                    .setFont(font)
                    .setFontSize(24)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(surname);
            document.add(new Paragraph("\n"));
        }

        if (genealogy.getOriginPlace() != null) {
            Paragraph origin = new Paragraph("起源地：" + genealogy.getOriginPlace())
                    .setFont(font)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(origin);
        }

        if (genealogy.getDescription() != null && !genealogy.getDescription().isEmpty()) {
            document.add(new Paragraph("\n\n"));
            Paragraph desc = new Paragraph(genealogy.getDescription())
                    .setFont(font)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(desc);
        }

        document.add(new Paragraph("\n\n\n\n"));
        Paragraph footer = new Paragraph("家谱管理系统自动生成")
                .setFont(font)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY);
        document.add(footer);

        document.newPage();
    }

    /**
     * 添加目录页
     */
    protected void addContentsPage(Document document, int generationCount, PdfFont font) {
        Paragraph title = new Paragraph("目 录")
                .setFont(font)
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        for (int i = 1; i <= generationCount; i++) {
            Paragraph p = new Paragraph(String.format("第%d世代 .............................. %d页",
                    i, i + 2))
                    .setFont(font)
                    .setFontSize(14);
            document.add(p);
            document.add(new Paragraph("\n"));
        }

        document.newPage();
    }

    /**
     * 添加一个世代的页面
     */
    protected void addGenerationPage(Document document, List<Person> persons,
                                      int generationNum, PdfFont font) {
        Paragraph title = new Paragraph(String.format("第 %d 世", generationNum))
                .setFont(font)
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        // 每个人物一段传记
        for (Person person : persons) {
            addPersonBiography(document, person, font);
            document.add(new Paragraph("\n"));
        }

        document.newPage();
    }

    /**
     * 添加一个人物的传记
     */
    protected void addPersonBiography(Document document, Person person, PdfFont font) {
        StringBuilder sb = new StringBuilder();

        // 开头：某某公，讳某某，字某某，号某某
        sb.append(person.getName());
        if (person.getStyleName() != null && !person.getStyleName().isEmpty()) {
            sb.append("，字").append(person.getStyleName());
        }
        if (person.getHao() != null && !person.getHao().isEmpty()) {
            sb.append("，号").append(person.getHao());
        }
        if (person.getTitle() != null && !person.getTitle().isEmpty()) {
            sb.append("，").append(person.getTitle());
        }
        if (person.getOccupation() != null && !person.getOccupation().isEmpty()) {
            sb.append("，").append(person.getOccupation());
        }
        sb.append("。");

        // 生卒年份
        if (person.getBirthYear() != null || person.getDeathYear() != null) {
            sb.append("\n");
            if (person.getBirthYear() != null) {
                sb.append("生于").append(person.getBirthYear());
            }
            if (person.getDeathYear() != null) {
                if (person.getBirthYear() != null) {
                    sb.append("，");
                }
                sb.append("卒于").append(person.getDeathYear());
            }
            sb.append("。");
        }

        // 父母配偶
        if (person.getFatherId() != null || person.getMotherId() != null || person.getSpouseIds() != null) {
            sb.append("\n");
            if (person.getFatherId() != null) {
                sb.append("父ID：").append(person.getFatherId());
            }
            if (person.getMotherId() != null) {
                if (person.getFatherId() != null) sb.append("，");
                sb.append("母ID：").append(person.getMotherId());
            }
            if (person.getSpouseIds() != null && !person.getSpouseIds().isEmpty()) {
                if (person.getFatherId() != null || person.getMotherId() != null) sb.append("，");
                sb.append("配：ID").append(person.getSpouseIds());
            }
            sb.append("。");
        }

        // 支系迁徙
        if (person.getBranch() != null && !person.getBranch().isEmpty()) {
            sb.append("\n");
            sb.append("属").append(person.getBranch()).append("支");
            if (person.getMigrationPath() != null && !person.getMigrationPath().isEmpty()) {
                sb.append("，迁徙路线：").append(person.getMigrationPath());
            }
            sb.append("。");
        }

        // 功绩
        if (person.getAchievements() != null && !person.getAchievements().isEmpty()) {
            sb.append("\n");
            sb.append("功绩：").append(person.getAchievements()).append("。");
        }

        // 传记
        if (person.getBiography() != null && !person.getBiography().isEmpty()) {
            sb.append("\n");
            sb.append("传记：").append(person.getBiography());
            if (!person.getBiography().endsWith("。")) sb.append("。");
        }

        // 墓地
        if (person.getCemeteryLocation() != null && !person.getCemeteryLocation().isEmpty()) {
            sb.append("\n");
            sb.append("葬于：").append(person.getCemeteryLocation()).append("。");
        }

        // 来源
        if (person.getSource() != null && !person.getSource().isEmpty()) {
            sb.append("\n");
            sb.append("资料来源：").append(person.getSource()).append("。");
        }

        Paragraph p = new Paragraph(sb.toString())
                .setFont(font)
                .setFontSize(12)
                .setFirstLineIndent(24); // 首行缩进
        document.add(p);
    }

    /**
     * 添加统计页
     */
    protected void addStatisticsPage(Document document, Genealogy genealogy,
                                      List<Person> allPersons, int generations, PdfFont font) {
        Paragraph title = new Paragraph("统计信息")
                .setFont(font)
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        Table table = new Table(2);
        table.setWidth(400);

        addStatRow(table, "家谱名称", genealogy.getName(), font);
        addStatRow(table, "姓氏", genealogy.getSurname() != null ? genealogy.getSurname() : "-", font);
        addStatRow(table, "总人数", String.valueOf(allPersons.size()), font);
        addStatRow(table, "总世代数", String.valueOf(generations), font);

        long maleCount = allPersons.stream().filter(p -> "M".equals(p.getGender())).count();
        long femaleCount = allPersons.stream().filter(p -> "F".equals(p.getGender())).count();
        addStatRow(table, "男性人数", String.valueOf(maleCount), font);
        addStatRow(table, "女性人数", String.valueOf(femaleCount), font);

        document.add(table);
    }

    /**
     * 添加统计行
     */
    protected void addStatRow(Table table, String label, String value, PdfFont font) {
        com.itextpdf.layout.element.Cell labelCell = new com.itextpdf.layout.element.Cell();
        labelCell.add(new Paragraph(label).setFont(font).setFontSize(12));
        labelCell.setBold();
        table.addCell(labelCell);

        com.itextpdf.layout.element.Cell valueCell = new com.itextpdf.layout.element.Cell();
        valueCell.add(new Paragraph(value).setFont(font).setFontSize(12));
        table.addCell(valueCell);
    }
}
