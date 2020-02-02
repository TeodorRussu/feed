package news.feed.action;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import news.feed.config.YamlConfig;
import news.feed.data.StaticData;
import org.apache.poi.ss.usermodel.Workbook;
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

import javax.servlet.ServletContext;
import java.io.File;
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
    @Autowired
    private YamlConfig yamlConfig;

    private Elements allInteresting = new Elements();
    private Map<String, String> relevantURLS = new LinkedHashMap<>();
    private String url;
    private LocalDate selectedDate;
    private List<Novost> novosti = new ArrayList<>();
    private Map<String, List<Novost>> groupedNews = new HashMap<>();


    public void action() throws IOException {
        log.info("angi action begin");
        log.info("url: " + url);
        log.info("keywords: " + StaticData.keywordsAndGroups.keySet());
        String workingURL = url;
        extractNewsURLsToList(workingURL);
        extractNewsToCollection();


    }

    private void extractNewsToCollection() {
        log.info("start extract news to collections");
        for (Map.Entry<String, String> me : relevantURLS.entrySet()) {
            String novostURL = me.getKey();
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

                addNovostToCategorisedMap(novostURL, date, title, body);

                Thread.sleep(200);
            } catch (Exception e) {
                log.info("Exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
        log.info("news added to collection");
    }

    private void addNovostToCategorisedMap(String novostURL, String date, String title, String body) {
        for (int i = 0; i < StaticData.orderedKeywords.size(); i++) {
            String keyword = StaticData.orderedKeywords.get(i);
            if (!keyword.contains(" ")) {
                if (title.contains(keyword) || body.contains(keyword)) {
                    addNewToSpecificList(novostURL, title, body, date, StaticData.keywordsAndGroups.get(keyword));
                    return;
                }
            } else {
                String[] expression = keyword.split(" ");
                boolean found = true;
                for (String exprPart : expression) {
                    if (!title.contains(exprPart) && !body.contains(exprPart)) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    addNewToSpecificList(novostURL, title, body, date, StaticData.keywordsAndGroups.get(keyword));
                    return;
                }
            }
        }
        if (novostURL.contains("90886") ||
                novostURL.contains("%D0%9D%D0%B5%D1%84%D1%82%D1%8C") ||
                novostURL.contains("%D0%B3%D0%B0%D0%B7")) {
            addNewToSpecificList(novostURL, title, body, date, "НЕФТЬ И ГАЗ");
        } else {
            addNewToSpecificList(novostURL, title, body, date, "другие новости");
        }
    }

    private void addNewToSpecificList(String novostURL, String title, String body, String date, String keyword) {
        List<Novost> novosti = groupedNews.get(keyword);
        if (novosti == null) {
            novosti = new ArrayList<>();
        }
        log.info(String.format("keyword %s found, novost: %s", keyword, title));
        createAndAddNewToList(novostURL, title, body, date, novosti);

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

                if (novostDate.isBefore(selectedDate)) {
                    log.info("added news URLs to bulk list");
                    return;
                }

                novost.getElementsByClass("section_link").remove();

                //create novost url
                Element el = novost.select("a").first();
                String novostURL = yamlConfig.getAngiBaseURL() + el.attr("href");

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


    public void toExcel() throws IOException {

        log.info("start exporting to excel");
        Workbook workbook = new XSSFWorkbook();

        String columns[] = {"Title", "Comment", "Body", "Date"};

        for (String group : groupedNews.keySet()) {
            List<Novost> novosti = groupedNews.get(group);
            performToExcel(workbook, group, novosti, columns);
        }

        // Write the output to a file
        String file=new File(yamlConfig.getExcelPathRoot()+ yamlConfig.getAngiExcelFilename()).getAbsolutePath();
        File file2=new File(file);

        FileOutputStream
                fileOut =
                new FileOutputStream(file2);
        workbook.write(fileOut);
        fileOut.close();

        // Closing the workbook
        workbook.close();
        log.info(String.format("exported to excel %d news", StaticData.newsCounter));
    }

}
