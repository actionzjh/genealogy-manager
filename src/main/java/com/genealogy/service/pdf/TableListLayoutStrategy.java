package com.genealogy.service.pdf;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.genealogy.entity.Genealogy;
import com.genealogy.entity.Person;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 表格列表式排版
 * 按世代分组，每人一行表格，信息完整清晰
 */
public class TableListLayoutStrategy implements PdfLayoutStrategy {

    @Override
    public String getName() {
        return "table-list";
    }

    @Override
    public String getDescription() {
        return "表格列表式（按世代分页，每人一行，信息完整）";
    }

    @Override
    public void layout(Document document, Genealogy genealogy, List<Person> allPersons, PdfFont font) {
        // 添加标题页
        addTitlePage(document, genealogy, font);

        // 添加目录页
        List<List<Person>> generationGroups = groupByGeneration(allPersons);
        addContentsPage(document, generationGroups.size(), font);

        // 按世代排版
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

        document.getPdfDocument().addNewPage();
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

        document.getPdfDocument().addNewPage();
    }

    /**
     * 添加一个世代页面
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

        float[] columnWidths = {180, 120, 220};
        Table table = new Table(columnWidths);
        table.setWidth(520);

        addTableCell(table, "姓名", font, true);
        addTableCell(table, "基本信息", font, true);
        addTableCell(table, "简介", font, true);

        for (Person person : persons) {
            StringBuilder nameStr = new StringBuilder(person.getName());
            if (person.getStyleName() != null && !person.getStyleName().isEmpty()) {
                nameStr.append(" 字").append(person.getStyleName());
            }
            if (person.getHao() != null && !person.getHao().isEmpty()) {
                nameStr.append(" 号").append(person.getHao());
            }
            if (person.getTitle() != null && !person.getTitle().isEmpty()) {
                nameStr.append("  ").append(person.getTitle());
            }
            addTableCell(table, nameStr.toString(), font, false);

            StringBuilder infoStr = new StringBuilder();
            if (person.getGender() != null) {
                infoStr.append("性别：").append("M".equals(person.getGender()) ? "男" : "F".equals(person.getGender()) ? "女" : "未知").append("\n");
            }
            if (person.getBirthYear() != null) {
                infoStr.append("生：").append(person.getBirthYear());
            }
            if (person.getDeathYear() != null) {
                infoStr.append(" 卒：").append(person.getDeathYear());
            }
            if (person.getBranch() != null && !person.getBranch().isEmpty()) {
                infoStr.append("\n支系：").append(person.getBranch());
            }
            addTableCell(table, infoStr.toString().trim(), font, false);

            StringBuilder bioStr = new StringBuilder();
            if (person.getOccupation() != null && !person.getOccupation().isEmpty()) {
                bioStr.append(person.getOccupation()).append("\n");
            }
            if (person.getAchievements() != null && !person.getAchievements().isEmpty()) {
                bioStr.append(person.getAchievements()).append("\n");
            }
            if (person.getCemeteryLocation() != null && !person.getCemeteryLocation().isEmpty()) {
                bioStr.append("墓地：").append(person.getCemeteryLocation());
            }
            addTableCell(table, bioStr.toString().trim(), font, false);
        }

        document.add(table);
        document.getPdfDocument().addNewPage();
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
     * 添加表格单元格
     */
    protected void addTableCell(Table table, String text, PdfFont font, boolean isHeader) {
        Cell cell = new Cell();
        Paragraph p = new Paragraph(text != null ? text : "")
                .setFont(font);
        if (isHeader) {
            p.setBold();
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        }
        p.setFontSize(isHeader ? 12 : 10);
        cell.add(p);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        table.addCell(cell);
    }

    /**
     * 添加统计行
     */
    protected void addStatRow(Table table, String label, String value, PdfFont font) {
        Cell labelCell = new Cell();
        labelCell.add(new Paragraph(label).setFont(font).setFontSize(12));
        labelCell.setBold();
        table.addCell(labelCell);

        Cell valueCell = new Cell();
        valueCell.add(new Paragraph(value).setFont(font).setFontSize(12));
        table.addCell(valueCell);
    }
}
