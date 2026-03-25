package com.genealogy.service.pdf;

import com.genealogy.entity.Genealogy;
import com.genealogy.entity.Person;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 苏式宝塔式PDF排版策略
 *
 * 特点：
 * - 始祖在最上方
 * - 每一代向下分支排列
 * - 整体形状像宝塔，故名宝塔式
 * - 适合印刷成册，一目了然
 */
@Component("pagoda")
public class PagodaLayoutStrategy implements PdfLayoutStrategy {

    @Override
    public String getName() {
        return "苏式宝塔式";
    }

    @Override
    public String getDescription() {
        return "传统宝塔式排版，始祖在上，逐代分支向下排列，适合家谱印刷";
    }

    @Override
    public void layout(Document document, Genealogy genealogy, List<Person> allPersons, PdfFont font) throws Exception {

        // 标题页
        addTitlePage(document, font, genealogy);

        // 按世代分组
        Map<Integer, List<Person>> byGeneration = new HashMap<>();
        int maxGen = 0;
        for (Person person : allPersons) {
            Integer gen = person.getGeneration();
            if (gen == null) gen = 1;
            byGeneration.computeIfAbsent(gen, k -> new ArrayList<>()).add(person);
            if (gen > maxGen) {
                maxGen = gen;
            }
        }

        // 构建父子关系树
        Map<Long, List<Person>> childrenMap = buildChildrenMap(allPersons);

        // 宝塔式表格 - 每一代人横向排列，从始祖向下分支
        addPagodaTable(document, font, byGeneration, childrenMap, maxGen);

        // 统计信息页
        addStatsPage(document, font, genealogy, allPersons.size(), maxGen);

        document.close();
    }

    private void addTitlePage(Document document, PdfFont font, Genealogy genealogy) throws Exception {
        Paragraph title = new Paragraph(genealogy.getName())
                .setFont(font)
                .setFontSize(28)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(200);
        document.add(title);

        if (genealogy.getSurname() != null && !genealogy.getSurname().isEmpty()) {
            Paragraph subtitle = new Paragraph(genealogy.getSurname() + "氏家谱")
                    .setFont(font)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20);
            document.add(subtitle);
        }

        if (genealogy.getDescription() != null && !genealogy.getDescription().isEmpty()) {
            Paragraph desc = new Paragraph(genealogy.getDescription())
                    .setFont(font)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(50);
            document.add(desc);
        }

        document.getPdfDocument().addNewPage();
    }

    private Map<Long, List<Person>> buildChildrenMap(List<Person> allPersons) {
        Map<Long, List<Person>> childrenMap = new HashMap<>();
        for (Person person : allPersons) {
            if (person.getFatherId() != null) {
                childrenMap.computeIfAbsent(person.getFatherId(), k -> new ArrayList<>()).add(person);
            } else if (person.getMotherId() != null) {
                childrenMap.computeIfAbsent(person.getMotherId(), k -> new ArrayList<>()).add(person);
            }
        }
        return childrenMap;
    }

    private void addPagodaTable(Document document, PdfFont font,
                                Map<Integer, List<Person>> byGeneration,
                                Map<Long, List<Person>> childrenMap,
                                int maxGen) throws Exception {

        Paragraph sectionTitle = new Paragraph("世系总图（宝塔式）")
                .setFont(font)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(sectionTitle);

        // 宝塔式每代占一列，从左到右（始祖在左上）
        // 每个人占用一格，下方是他的子女
        for (int gen = 1; gen <= maxGen; gen++) {
            List<Person> persons = byGeneration.getOrDefault(gen, new ArrayList<>());

            Paragraph genTitle = new Paragraph(String.format("第 %d 世", gen))
                    .setFont(font)
                    .setFontSize(14)
                    .setMarginBottom(10);
            document.add(genTitle);

            // 每个人一行，子女缩进排列在下方
            for (Person person : persons) {
                addPersonPagodaRow(document, font, person, childrenMap, 0);
            }

            document.add(new Paragraph("\n"));

            // 每页最多放5代
            if (gen % 5 == 0 && gen != maxGen) {
                document.getPdfDocument().addNewPage();
            }
        }
    }

    private void addPersonPagodaRow(Document document, PdfFont font, Person person,
                                    Map<Long, List<Person>> childrenMap, int indent) throws Exception {
        // 缩进
        String indentStr = "  ".repeat(indent * 2);

        // 基本信息
        StringBuilder sb = new StringBuilder(indentStr);
        sb.append("├─ ");
        sb.append(person.getName());
        if (person.getGender() != null) {
            sb.append(" (").append(person.getGender().equalsIgnoreCase("M") ? "男" : "女").append(")");
        }
        if (person.getBirthYear() != null && !person.getBirthYear().isEmpty()) {
            sb.append(" 生卒: ").append(person.getBirthYear());
            if (person.getDeathYear() != null && !person.getDeathYear().isEmpty()) {
                sb.append(" - ").append(person.getDeathYear());
            }
        }
        if (person.getTitle() != null && !person.getTitle().isEmpty()) {
            sb.append("  [").append(person.getTitle()).append("]");
        }

        Paragraph p = new Paragraph(sb.toString())
                .setFont(font)
                .setFontSize(10)
                .setMarginLeft(indent * 20)
                .setMarginTop(2)
                .setMarginBottom(2);
        document.add(p);

        // 递归添加子女
        List<Person> children = childrenMap.getOrDefault(person.getId(), new ArrayList<>());
        for (Person child : children) {
            addPersonPagodaRow(document, font, child, childrenMap, indent + 1);
        }
    }

    private void addStatsPage(Document document, PdfFont font, Genealogy genealogy, int totalPersons, int maxGen) throws Exception {
        document.getPdfDocument().addNewPage();

        Paragraph title = new Paragraph("统计信息")
                .setFont(font)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30);
        document.add(title);

        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        table.setMarginTop(20);

        addStatsRow(table, font, "家谱名称", genealogy.getName());
        addStatsRow(table, font, "总人数", String.valueOf(totalPersons));
        addStatsRow(table, font, "最大世代数", String.valueOf(maxGen));
        if (genealogy.getSurname() != null) {
            addStatsRow(table, font, "姓氏", genealogy.getSurname());
        }
        if (genealogy.getOriginPlace() != null) {
            addStatsRow(table, font, "起源地", genealogy.getOriginPlace());
        }

        document.add(table);
    }

    private void addStatsRow(Table table, PdfFont font, String label, String value) {
        table.addCell(new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(label).setFont(font).setFontSize(12)));
        table.addCell(new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(value).setFont(font).setFontSize(12)));
    }
}
