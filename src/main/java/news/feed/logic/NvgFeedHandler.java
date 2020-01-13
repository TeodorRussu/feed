package news.feed.logic;

import news.feed.action.NvgNewsFeed;
import news.feed.action.URLParameter;
import news.feed.email.EmailSender;
import news.feed.env.StaticData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class NvgFeedHandler {

    @Autowired
    Environment env;

    @Autowired
    EmailSender emailSender;

    @Autowired
    ApplicationContext springContext;

    public NvgNewsFeed nvgCreate(String url, URLParameter... parameters) throws URISyntaxException {
        NvgNewsFeed nvgNewsFeed = springContext.getBean(NvgNewsFeed.class);
        nvgNewsFeed.init(url, parameters);
        return nvgNewsFeed;
    }

    public void nvgAction(String dateFrom, String dateTo, String q) {
        dateFrom = LocalDate.parse(dateFrom, DateTimeFormatter.ISO_LOCAL_DATE).format(DateTimeFormatter.ofPattern("dd.MM.uuuu"));
        dateTo = LocalDate.parse(dateTo, DateTimeFormatter.ISO_LOCAL_DATE).format(DateTimeFormatter.ofPattern("dd.MM.uuuu"));

        try {
            String url = env.getProperty("ngvNewsPath");
            URLParameter parOne = URLParameter.builder().name(env.getProperty("dateFromParam")).value(dateFrom).build();
            URLParameter parTwo = URLParameter.builder().name(env.getProperty("dateToParam")).value(dateTo).build();
            URLParameter parThree = URLParameter.builder().name("q").value(q).build();
            NvgNewsFeed feed = nvgCreate(url, parOne, parTwo, parThree);
            feed.action();
            feed.toExcel();
            emailSender.sendEmailWithAttachment(StaticData.emailTo, env.getProperty("ngvEmailSubject"), env.getProperty("ngvEmailContent"), env.getProperty("ngvExcelFilename"), env.getProperty("ngvExcelExportPath"));
        } catch (IOException | URISyntaxException | MessagingException e) {
            e.printStackTrace();
        }
    }

}
