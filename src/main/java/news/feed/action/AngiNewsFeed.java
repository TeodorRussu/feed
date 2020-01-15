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
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
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
import java.util.Iterator;
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
                newsDocumentPage.select("br").append("\n");
                newsDocumentPage.select("p").prepend("\n\n");
                String title = newsDocumentPage.getElementsByTag("h1").first().text();
//                Elements titleElements = newsDocumentPage.getElementsByTag("h1");
//                String title = titleElements.get(0).childNodes().get(0).toString().trim();

                newsDocumentPage.getElementsByClass("lightbox").remove();
                newsDocumentPage.getElementsByClass("newslink").remove();
                newsDocumentPage.getElementsByClass("banner").remove();
                newsDocumentPage.getElementsByAttributeValue("data-position", "desktop").remove();
                newsDocumentPage.getElementsByAttributeValue("style", "width:100%;").remove();

                Elements articles = newsDocumentPage.getElementsByClass("text");
                articles.get(0).getElementsByTag("a").remove();
                Elements articles1 =  articles.get(0).getElementsByTag("p");


                Document document = Jsoup.parse(articles.toString());
                document.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
                document.select("br").append("\\n");
                document.select("p").prepend("\\n");
                String s = document.html().replaceAll("\\\\n", "\n");
                String output =  Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));



                List<Node> nodes = articles.get(0).childNodes();

                StringBuilder builder = new StringBuilder();
                StringBuilder builder1 = new StringBuilder();

                for (Element node:articles1){
                    String text = Jsoup.parse(node.toString()).text();

                    builder.append(text + "\n");
                    builder1.append(node.outerHtml() + "\n");
                }

                System.out.println(articles.text());
                System.out.println(".........");
                System.out.println(output);
                System.out.println(" ----------- ");
                System.out.println();
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

    public String text(Elements elements) {
        StringBuilder sb = new StringBuilder();
        for (Element element : elements) {
            if (sb.length() != 0)
                sb.append("\n");
            sb.append(element.text());
        }
        return sb.toString();
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

class TunedElements extends Elements {

    @Override
    public String text() {
        StringBuilder sb = new StringBuilder();

        Element element;
        for(Iterator var2 = this.iterator(); var2.hasNext(); sb.append(element.text())) {
            element = (Element)var2.next();
            if (sb.length() != 0) {
                sb.append("\n\n");
            }
        }

        return sb.toString();
    }
}