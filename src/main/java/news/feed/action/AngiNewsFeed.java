package news.feed.action;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Data
public class AngiNewsFeed extends Feed {

    @Autowired
    private Environment env;
    private Elements allInteresting = new Elements();
    private Map<String, String> relevantURLS = new LinkedHashMap<>();
    private String url;
    private LocalDate selectedDate;
    private List<Novost> novosti = new ArrayList<>();
    private Map<String, List<Novost>> groupedNews = new HashMap<>();
    private List<String> keywords = List.of("SOCAR", "баррель", "Роснефт", "Лукойл", "ЛУКОЙЛ", "Газпром нефт", "Башнефт", "Новатэк", "Татнефт", "Сибур", "КазМунайГаз", "Сокар", "Александр Новак", "Цен нефт", "цен нефт");

    public void action() throws IOException {
        log.info("angi action begin");
        log.info("url: " + url);
        log.info("keywords: " + keywords);
        String workingURL = url;
        extractNewsURLsToList(workingURL);
        extractNewsToCollection();



    }

    private void extractNewsToCollection() {
        log.info("start extract news to collections");
        for (Map.Entry<String, String>me: relevantURLS.entrySet()) {
            String novostURL  = me.getKey();
            String date = me.getValue();
            try {

                Document newsDocumentPage = Jsoup.connect(novostURL).get();



                //extract title
                String title = newsDocumentPage.getElementsByTag("h1").first().text();

                //extract novosti section
                newsDocumentPage.getElementsByClass("lightbox").remove();
                newsDocumentPage.getElementsByClass("newslink").remove();
                newsDocumentPage.getElementsByClass("banner").remove();
                newsDocumentPage.getElementsByAttributeValue("data-position", "desktop").remove();
                newsDocumentPage.getElementsByAttributeValue("style", "width:100%;").remove();

                Elements articles = newsDocumentPage.getElementsByClass("text");
                articles.get(0).getElementsByTag("a").remove();

                Document document = Jsoup.parse(articles.toString());
                document.outputSettings(new Document.OutputSettings().prettyPrint(false));
                document.select("br").append("\\n");
                document.select("p").prepend("\\n");
                String s = document.html().replaceAll("\\\\n", "\n");
                String body = Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));

                body = cleanText(body);
                body = body + "Источник: " + novostURL.trim();



                outer: for (String keyword : keywords) {
                    if (!keyword.contains(" ")) {
                        if (title.contains(keyword) || body.contains(keyword)) {
                            addNewToSpecificList(novostURL, title, body, date, keyword);
                        }
                        else {
                            String [] expression = keyword.split(" ");
                            for (String exprPart: expression){
                                if (!title.contains(exprPart) && !body.contains(exprPart)){
                                    continue outer;
                                }
                            }
                            List<Novost> novosti = groupedNews.get(keyword);
                            if (novosti == null) {
                                novosti = new ArrayList<>();
                            }
                            addNewToSpecificList(novostURL, title, body, date, keyword);
                        }
                    }
                }

                //create nvost object

                Thread.sleep(200);
            } catch (Exception e) {
                log.info("Exception" + e.getMessage());
            }
        }
        log.info("news added to collection");
    }

    private void addNewToSpecificList(String novostURL, String title, String body, String date, String keyword) {
        List<Novost> novosti = groupedNews.get(keyword);
        if (novosti == null) {
            novosti = new ArrayList<>();
        }
        log.info(String.format("keyword %s found", keyword));
        novosti.add(
                Novost.builder()
                        .body(body)
                        .title(title)
                        .url(novostURL)
                        .date(date)
                        .build()
        );
        groupedNews.put(keyword, novosti);
    }

    private void extractNewsURLsToList(String workingURL) throws IOException {
        log.info("start adding news URLs to bulk list");
        int i = 0;
        while (true) {

            if (i > 0) {
                workingURL = url + String.format("%s/", i);
            }
            Document document = Jsoup.connect(workingURL).get();
            Elements links = document.getElementsByClass("newslink");

            for (Element novost : links) {
                Elements dateValue = novost.getElementsByClass("date");
                String dateVal = dateValue.text().trim();
                LocalDate novostDate = getNovostDate(dateVal);

                if (novostDate.isBefore(selectedDate)){
                    log.info("added news URLs to bulk list");
                    return;
                }

                //create novost url
                Element el = novost.select("a").first();
                String novostURL = env.getProperty("angiBaseURL") + el.attr("href");

                relevantURLS.put(novostURL, novostDate.toString());
            }
            i++;
        }

    }

    private LocalDate getNovostDate(String dateVal) {

        dateVal = dateVal.substring(0, dateVal.indexOf('/'));
        String[] dateArray = dateVal.split(" ");
        String day = dateArray[0].length() == 2 ? dateArray[0] : "0" + dateArray[0];

        String month = extractMonth(dateArray[1]);
        String year = dateArray.length == 3 ? dateArray[2] : Integer.toString(LocalDate.now().getYear());

        return LocalDate.parse(String.format("%s-%s-%s", year, month, day));
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
        body = body.replace("&nbsp;", " ");
        for (int i = 0; i < 5; i++) {
            body = body.replace("  ", " ");
        }
        for (int i = 0; i < 5; i++) {
            body = body.replace("\n ", "\n");
        }
        for (int i = 0; i < 5; i++) {
            body = body.replace("\n\n\n", "\n\n");
        }
        body = body.trim();
        body = body + "\n\n";

        return body;
    }

    public void toExcel() throws IOException {
        log.info("start exporting to excel");
        Workbook workbook = new XSSFWorkbook();

        for (String group : groupedNews.keySet()) {
            List<Novost> novosti = groupedNews.get(group);
            performToExcel(workbook, group, novosti);
        }

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(Objects.requireNonNull(env.getProperty("angiExcelExportPath")));
        workbook.write(fileOut);
        fileOut.close();

        // Closing the workbook
        workbook.close();
    }

    public void performToExcel( Workbook workbook, String sheetName, List<Novost> novosti) throws IOException {


        String[] columns = {"Title", "Comment", "Body", "Date"};

         // new HSSFWorkbook() for generating `.xls` file

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
            Row row = sheet.createRow(rowNum++);

            Cell titleCell = createCellAndItsStyle(cellStyle, row, 0);
            titleCell.setCellValue(novost.getTitle());

            Cell bodyCell = createCellAndItsStyle(cellStyle, row, 2);
            bodyCell.setCellValue(novost.getBody());

            Cell dateCell = createCellAndItsStyle(cellStyle, row, 3);
            dateCell.setCellValue(novost.getDate());
        }
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

    }

    private Cell createCellAndItsStyle(CellStyle titleCellStyle, Row row, int column) {
        Cell titleCell = row.createCell(column);
        titleCell.setCellStyle(titleCellStyle);
        return titleCell;
    }

    private CellStyle setCellStyle(Workbook workbook, IndexedColors color) {
        Font headerFont = workbook.createFont();

        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(color.getIndex());
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        return headerCellStyle;
    }

}
