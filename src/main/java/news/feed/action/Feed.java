package news.feed.action;

import news.feed.data.StaticData;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Feed {

    public static final String HTML_SPACE = "&nbsp;";
    public static final String SPACE = " ";
    public static final String DOUBLE_SPACE = "  ";
    public static final String NEW_LINE_SPACE = "\n ";
    public static final String NEW_LINE = "\n";
    public static final String TRIPLE_NEW_LINE = "\n\n\n";
    public static final String DOUBLE_NEW_LINE = "\n\n";

    protected String cleanText(String body) {
        body = body.replace(HTML_SPACE, SPACE);
        for (int i = 0; i < 5; i++) {
            body = body.replace(DOUBLE_SPACE, SPACE);
        }
        for (int i = 0; i < 5; i++) {
            body = body.replace(NEW_LINE_SPACE, NEW_LINE);
        }
        for (int i = 0; i < 5; i++) {
            body = body.replace(TRIPLE_NEW_LINE, DOUBLE_NEW_LINE);
        }
        body = body.trim();
        body = body + DOUBLE_NEW_LINE;

        return body;
    }

    protected void createAndAddNewToList(String novostURL, String title, String body, String date,
                                         List<Novost> novosti) {
        novosti.add(
                Novost.builder()
                        .body(body)
                        .title(title)
                        .url(novostURL)
                        .date(date)
                        .build()
        );
    }

    public void performToExcel(Workbook workbook, String sheetName, List<Novost> novosti, String... columns)
            throws IOException {

        // Create a Sheet
        Sheet sheet = workbook.createSheet(sheetName);
        sheet.setDefaultRowHeight((short) 600);

        CellStyle headerCellStyle = setCellStyle(workbook, IndexedColors.BLUE_GREY);
        CellStyle cellStyle = setCellStyle(workbook, IndexedColors.GREY_80_PERCENT);

        // Create a Row
        Row headerRow = sheet.createRow(0);

        // Create cells
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Create rows with data
        int rowNum = 1;
        for (Novost novost : novosti) {
            StaticData.newsCounter++;
            Row row = sheet.createRow(rowNum++);

            Cell titleCell = createCellAndItsStyle(cellStyle, row, 0);
            titleCell.setCellValue(novost.getTitle());

            Cell bodyCell = createCellAndItsStyle(cellStyle, row, 2);
            bodyCell.setCellValue(novost.getBody());

            if (Arrays.asList(columns).contains("Date")) {
                Cell dateCell = createCellAndItsStyle(cellStyle, row, 3);
                dateCell.setCellValue(novost.getDate());
            }
        }
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

    }

    protected Cell createCellAndItsStyle(CellStyle titleCellStyle, Row row, int column) {
        Cell titleCell = row.createCell(column);
        titleCell.setCellStyle(titleCellStyle);
        return titleCell;
    }

    protected CellStyle setCellStyle(Workbook workbook, IndexedColors color) {
        Font headerFont = workbook.createFont();

        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(color.getIndex());
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        return headerCellStyle;
    }
}
