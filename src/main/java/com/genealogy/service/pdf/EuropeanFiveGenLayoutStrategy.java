package com.genealogy.service.pdf;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.genealogy.entity.Genealogy;
import com.genealogy.entity.Person;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 欧氏五世表排版（欧洲式）
 * 每页五代，从上到下排列，每代一列，适合快速查阅世系
 */
public class EuropeanFiveGenLayoutStrategy implements PdfLayoutStrategy {

    @Override
    public String getName() {
        return "european-five";
    }

    @Override
    public String getDescription() {
        return "欧氏五世表（每页五代，每代一列，传统家谱查阅格式）";
    }

    @Override
    public void layout(Document document, Genealogy genealogy, List<Person> allPersons, PdfFont font) throws IOException {
        // 添加标题页
        addTitlePage(document, genealogy, font);

        // 按世代分组
        int maxGen = getMaxGeneration(allPersons);
        // 每5页一组
        int totalPages = (int) Math.ceil((double) maxGen / 5);

        // 添加目录
        addContentsPage(document, totalPages, font);

        // 每页五代
        for (int page = 1; page <= totalPages; page++) {
            int startGen = (page - 1) * 5 + 1;
            int endGen = Math.min(startGen + 4, maxGen);
            addFiveGenPage(document, allPersons, startGen, endGen, font);
            document.getPdfDocument().addNewPage();
        }

        // 添加统计页
        addStatisticsPage(document, genealogy, allPersons, maxGen, font);
    }

    /**
     * 获取最大世代数
     */
    private int getMaxGeneration(List<Person> allPersons) {
        return allPersons.stream()
                .map(Person::getGeneration)
                .filter(g -> g != null && g > 0)
                .max(Integer::compareTo)
                .orElse(1);
    }

    /**
     * 获取该世代的所有人
     */
    private List<Person> getPersonsByGeneration(List<Person> allPersons, int gen) {
        List<Person> result = new ArrayList<>();
        for (Person person : allPersons) {
            Integer g = person.getGeneration();
            if (g != null && g == gen) {
                result.add(person);
            }
        }
        result.sort((a, b) -> {
            Integer sa = a.getSortOrder();
            Integer sb = b.getSortOrder();
            return sa != null ? (sb != null ? sa - sb : 0) : (sb != null ? 0 : 0);
        });
        return result;
    }

    /**
     * 添加标题页
     */
    private void addTitlePage(Document document, Genealogy genealogy, PdfFont font) {
        Paragraph title = new Paragraph(genealogy.getName())
                .setFont(font)
                .setFontSize(28)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold();

        document.add(title);
        document.add(new Paragraph("\n\n"));

        Paragraph subTitle = new Paragraph("欧氏五世表格式")
                .setFont(font)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(subTitle);
        document.add(new Paragraph("\n"));

        if (genealogy.getSurname() != null) {
            Paragraph surname = new Paragraph(genealogy.getSurname() + "氏家谱")
                    .setFont(font)
                    .setFontSize(24)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(surname);
            document.add(new Paragraph("\n"));
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
    private void addContentsPage(Document document, int totalPages, PdfFont font) {
        Paragraph title = new Paragraph("目 录")
                .setFont(font)
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        for (int i = 1; i <= totalPages; i++) {
            int startGen = (i - 1) * 5 + 1;
            int endGen = startGen + 4;
            Paragraph p = new Paragraph(String.format("第%d-%d世 .............................. %d页",
                    startGen, endGen, i + 2))
                    .setFont(font)
                    .setFontSize(14);
            document.add(p);
            document.add(new Paragraph("\n"));
        }

        document.getPdfDocument().addNewPage();
    }

    /**
     * 添加一页（五代）
     */
    private void addFiveGenPage(Document document, List<Person> allPersons,
                                int startGen, int endGen, PdfFont font) {
        // 标题
        Paragraph title = new Paragraph(String.format("第 %d - %d 世", startGen, endGen))
                .setFont(font)
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        // 计算列数 = 世代数
        int numCols = endGen - startGen + 1;
        // 每列宽度
        float[] columnWidths = new float[numCols];
        float colWidth = 520f / numCols;
        for (int i = 0; i < numCols; i++) {
            columnWidths[i] = colWidth;
        }

        Table table = new Table(columnWidths);
        table.setWidth(520);
        table.setHorizontalAlignment(HorizontalAlignment.CENTER);

        // 表头：世代
        for (int gen = startGen; gen <= endGen; gen++) {
            Cell cell = new Cell();
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            Paragraph p = new Paragraph(String.valueOf(gen))
                    .setFont(font)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold();
            cell.add(p);
            cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            table.addCell(cell);
        }

        // 找最长列，决定行数
        int maxRows = 0;
        for (int gen = startGen; gen <= endGen; gen++) {
            List<Person> persons = getPersonsByGeneration(allPersons, gen);
            if (persons.size() > maxRows) {
                maxRows = persons.size();
            }
        }

        // 填充数据行，每行一个位置
        for (int row = 0; row < maxRows; row++) {
            for (int gen = startGen; gen <= endGen; gen++) {
                List<Person> persons = getPersonsByGeneration(allPersons, gen);
                Cell cell = new Cell();
                if (row < persons.size()) {
                    Person person = persons.get(row);
                    StringBuilder sb = new StringBuilder();
                    sb.append(person.getName());
                    if (person.getStyleName() != null && !person.getStyleName().isEmpty()) {
                        sb.append("\n").append(person.getStyleName());
                    }
                    if (person.getBirthYear() != null && person.getDeathYear() != null) {
                        sb.append("\n").append(person.getBirthYear()).append("-").append(person.getDeathYear());
                    }
                    Paragraph p = new Paragraph(sb.toString())
                            .setFont(font)
                            .setFontSize(9)
                            .setTextAlignment(TextAlignment.CENTER);
                    cell.add(p);
                }
                cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
                table.addCell(cell);
            }
        }

        document.add(table);
    }

    /**
     * 添加统计页
     */
    private void addStatisticsPage(Document document, Genealogy genealogy,
                                   List<Person> allPersons, int maxGen, PdfFont font) {
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
        addStatRow(table, "最大世代数", String.valueOf(maxGen), font);

        long maleCount = allPersons.stream().filter(p -> "M".equals(p.getGender())).count();
        long femaleCount = allPersons.stream().filter(p -> "F".equals(p.getGender())).count();
        addStatRow(table, "男性人数", String.valueOf(maleCount), font);
        addStatRow(table, "女性人数", String.valueOf(femaleCount), font);

        document.add(table);
    }

    /**
     * 添加统计行
     */
    private void addStatRow(Table table, String label, String value, PdfFont font) {
        Cell labelCell = new Cell();
        labelCell.add(new Paragraph(label).setFont(font).setFontSize(12));
        labelCell.setBold();
        table.addCell(labelCell);

        Cell valueCell = new Cell();
        valueCell.add(new Paragraph(value).setFont(font).setFontSize(12));
        table.addCell(valueCell);
    }
}
