package news.feed.action;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.feed.config.YamlConfig;
import org.apache.http.client.utils.URIBuilder;
import org.apache.poi.ss.usermodel.Workbook;
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
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Data
@RequiredArgsConstructor
public class NvgNewsFeed extends Feed {

    public static final String PAGEN_ = "PAGEN_";
    @Autowired
    private final YamlConfig yamlConfig;
    private Elements allInteresting = new Elements();
    private List<Novost> novosti = new ArrayList<>();
    private int first = Integer.MAX_VALUE;
    private int last = Integer.MIN_VALUE;
    private String url;

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
                body = body + "Источник: " + newsURL;

                createAndAddNewToList(newsURL, title, body, null, novosti);

                Thread.sleep(200);
            } catch (Exception e) {
                log.info("Exception" + e.getMessage());
            }
        }
        log.info("news added to collection");
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
        String baseURL = yamlConfig.getNgvBaseURL();
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
        log.info("start exporting to excel");
        Workbook workbook = new XSSFWorkbook();

        String[] columns = {"Title", "Comment", "Body"};

        performToExcel(workbook, yamlConfig.getNgvExcelSheetName(), novosti, columns);

        // Write the output to a file
        String file = new File(yamlConfig.getExcelPathRoot() + yamlConfig.getNgvExcelFilename()).getAbsolutePath();
        File file2 = new File(file);
        FileOutputStream fileOut = new FileOutputStream(file2);
        workbook.write(fileOut);
        fileOut.close();

        // Closing the workbook
        workbook.close();
    }

}