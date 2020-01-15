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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Data
public class NvgNewsFeed extends Feed{

    public static final String PAGEN_ = "PAGEN_";
    @Autowired
    private Environment env;

    Elements allInteresting = new Elements();

    private int first = Integer.MAX_VALUE;
    private int last = Integer.MIN_VALUE;
    private String url;
    List<Novost> novosti = new ArrayList<>();


    public void init(String baseURL, URLParameter... parameters) throws URISyntaxException {
        URIBuilder builder = new URIBuilder(baseURL);
        for (URLParameter param : parameters) {
            builder.addParameter(param.getName(), param.getValue());
        }
        url = builder.build().toString();
    }

    public void action() throws IOException {
        log.info("nvg action begin");
        Document document = Jsoup.connect(url).get();

        setPageIndexes(document);


        for (int i = first; i <= last; i++) {
            String pagedURL = url + String.format("&PAGEN_%d=%d", first, i);
            Document doc = Jsoup.connect(pagedURL).get();
            Elements links = doc.getElementsByClass("post-card");
            allInteresting.addAll(links);
        }

        for (Element element : allInteresting) {

            try {

                String newsURL = getNovostURL(element).trim();
                Document newsDocumentPage = Jsoup.connect(newsURL).get();

                Elements titleElements = newsDocumentPage.getElementsByClass("page-title__title");
                String title = titleElements.get(0).childNodes().get(0).toString().trim();

                Elements articles = newsDocumentPage.getElementsByTag("article");
                List<Node> bodyParagraphs = articles.get(0).childNodes();

                String body =
                        bodyParagraphs.stream().filter(child -> child.getClass().equals(TextNode.class))
                                .map(Node::toString).collect(Collectors.joining("\n"));
                body = body.trim();

                if (body.trim().isEmpty()) {
                    body = getBodyTextByParsingFile(newsDocumentPage).trim(); //doc parsing body extract method
                }

                body = cleanText(body);

                body = body + "\n\n" + "Источник: " + newsURL;

                novosti.add(
                        Novost.builder()
                                .body(body)
                                .title(title)
                                .url(newsURL)
                                .build()
                );
                Thread.sleep(200);
            } catch (Exception e) {
                log.info("Exception" + e.getMessage());
            }
        }
        log.info("news added to collection");
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

    private void setPageIndexes(Document doc) {
        Elements pages = doc.getElementsByClass("pagination__nav");
        if (pages.isEmpty()) {
            first = 1;
            last = 1;
            return;
        }
        pages = pages.get(0).children();

        for (Element page : pages) {
            String element = page.toString();

            if (element.contains(PAGEN_)) {
                String pagen = element.substring(element.indexOf(PAGEN_));
                pagen = pagen.substring(0, pagen.indexOf("\""));
                pagen = pagen.replace(PAGEN_, "");
                String[] values = pagen.split("=");
                try {
                    int from = Integer.parseInt(values[0]);
                    int to = Integer.parseInt(values[1]);
                    if (from < first) {
                        first = from;
                    }
                    if (to > last) {
                        last = to;
                    }
                } catch (NumberFormatException exception) {
                    log.info(exception.getMessage());
                }

            }
        }
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