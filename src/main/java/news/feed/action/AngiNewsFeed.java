package news.feed.action;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Data
public class AngiNewsFeed {

    public static final String PAGEN_ = "PAGEN_";
    @Autowired
    private Environment env;

    Elements allInteresting = new Elements();

    List<String> relevantURLS = new ArrayList<>();

    private String url;
    private LocalDate selectedDate;
    List<Novost> novosti = new ArrayList<>();


    public void action(String dateFrom) throws IOException {
        log.info("angi action begin");
        String workingURL = url;

        outer: for (int i = 0; i <= 1000; i++) {
            if (i > 0)
                workingURL = url + String.format("%s/", i);
            Document document = Jsoup.connect(workingURL).get();
            Elements links = document.getElementsByClass("newslink");

            for (Element novost : links) {
                Elements dateValue = novost.getElementsByClass("date");
                String dateVal = dateValue.text();
                dateVal = dateValue.text().trim();
                dateVal = dateVal.substring(0, dateVal.indexOf('/'));
                String[] dateArray = dateVal.split(" ");
                String day = dateArray[0].length() == 2? dateArray[0] : "0" + dateArray[0];

                String month = extractMonth(dateArray[1]);
                String year = dateArray.length == 3 ? dateArray[2] : Integer.toString(selectedDate.getYear());

                LocalDate novostDate = LocalDate.parse(String.format("%s-%s-%s", year, month, day));

                if (novostDate.isBefore(selectedDate))
                    break outer;
                Element el = novost.select("a").first();
                String url = "http://angi.ru"+el.attr("href");

//                allInteresting.add(novost.select("a").first());
                relevantURLS.add(url);

//                int size = allInteresting.size();
            }
        }

        for (String novostURL : relevantURLS) {

            try {

                Document newsDocumentPage = Jsoup.connect(novostURL).get();
                String title = newsDocumentPage.getElementsByTag("h1").first().text();
//                Elements titleElements = newsDocumentPage.getElementsByTag("h1");
//                String title = titleElements.get(0).childNodes().get(0).toString().trim();

                String articles = newsDocumentPage.getElementsByClass("text").outerHtml();
//                List<Node> bodyParagraphs = articles.get(0).childNodes();
//
//                String body =
//                        bodyParagraphs.stream().filter(child -> child.getClass().equals(TextNode.class))
//                                .map(Node::toString).collect(Collectors.joining("\n"));
//                body = body.trim();
//
//                if (body.trim().isEmpty()) {
//                    body = getBodyTextByParsingFile(newsDocumentPage).trim(); //doc parsing body extract method
//                }
//
//                body = cleanText(body);
//
//                //body = body + "\n\n" + "Источник: " + newsURL;
//
//                novosti.add(
//                        Novost.builder()
//                                .body(body)
//                                .title(title)
//                                .url(newsURL)
//                                .build()
//                );
                Thread.sleep(200);
            } catch (Exception e) {
                log.info("Exception" + e.getMessage());
            }
        }
        log.info("news added to collection");
    }

    private String extractMonth(String s) {
        switch (s) {
            case "января":
                return "01";
            case "февраля":
                return "02";
            case "марта":
                return "03";
            case "апреля":
                return "04";
            case "мая":
                return "05";
            case "июня":
                return "06";
            case "июля":
                return "07";
            case "августа":
                return "08";
            case "сентября":
                return "09";
            case "октября":
                return "10";
            case "ноября":
                return "11";
            case "декабря":
                return "12";
            default:
                return Integer.toString(selectedDate.getMonthValue());
        }

    }

    private String cleanText(String body) {
        body = body.replace("&nbsp;", "");
        body = body.replace("  ", " ");
        body = body.replace("\n ", "\n");
        return body;
    }

    private String getBodyTextByParsingFile(Document newsDocumentPage) {
        String textDoc = newsDocumentPage.text();
        int contentStartIndex = textDoc.indexOf("[DETAIL_TEXT] => ");
        int contentEndIndex = textDoc.indexOf("[~DETAIL_TEXT] =>");

        textDoc = textDoc.substring(contentStartIndex, contentEndIndex);
        return textDoc.replace("[DETAIL_TEXT] => ", "");
    }

    private String getNovostURL(Element element) {
        String newsURL;
        Elements links = element.getElementsByClass("post-card");
        String baseURL = env.getProperty("ngvBaseURL");
        String urlToAppend = links.get(0).attributes().get("href");
        newsURL = baseURL + urlToAppend;
        return newsURL;
    }


    public void toExcel() throws IOException {

        String[] columns = {"Title", "Comment", "Body"};

        Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file

        // Create a Sheet
        Sheet sheet = workbook.createSheet(env.getProperty("ngvExcelSheetName"));
        sheet.setDefaultRowHeight((short) 600);

        CellStyle headerCellStyle = setCellStyle(workbook, 12, IndexedColors.BLUE_GREY);
        CellStyle titleCellStyle = setCellStyle(workbook, 12, IndexedColors.GREY_80_PERCENT);
        CellStyle bodyCellStyle = setCellStyle(workbook, 12, IndexedColors.GREY_80_PERCENT);

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
            Row row = sheet.createRow(rowNum++);

            Cell titleCell = createCellAndItsStyle(titleCellStyle, row, 0);
            titleCell.setCellValue(novost.getTitle());

            Cell bodyCell = createCellAndItsStyle(bodyCellStyle, row, 2);
            bodyCell.setCellValue(novost.getBody());
        }
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(Objects.requireNonNull(env.getProperty("ngvExcelExportPath")));
        workbook.write(fileOut);
        fileOut.close();

        // Closing the workbook
        workbook.close();

    }

    private Cell createCellAndItsStyle(CellStyle titleCellStyle, Row row, int column) {
        Cell titleCell = row.createCell(column);
        titleCell.setCellStyle(titleCellStyle);
        return titleCell;
    }

    private CellStyle setCellStyle(Workbook workbook, int fontHeight, IndexedColors color) {
        Font headerFont = workbook.createFont();

        headerFont.setFontHeightInPoints((short) fontHeight);
        headerFont.setColor(color.getIndex());
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        return headerCellStyle;
    }
}