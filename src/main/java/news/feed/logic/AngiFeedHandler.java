package news.feed.logic;

import news.feed.action.AngiNewsFeed;
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
public class AngiFeedHandler {

    @Autowired
    Environment env;

    @Autowired
    EmailSender emailSender;

    @Autowired
    ApplicationContext springContext;

    public AngiNewsFeed nvgCreate(String url, LocalDate selectedDate) throws URISyntaxException {
        AngiNewsFeed angiNewsFeed = springContext.getBean(AngiNewsFeed.class);
        angiNewsFeed.setUrl(url);
        angiNewsFeed.setSelectedDate(selectedDate);
        return angiNewsFeed;
    }

    public void nvgAction(String dateFrom) {
        LocalDate selectedDate = LocalDate.parse(dateFrom, DateTimeFormatter.ISO_LOCAL_DATE);
        dateFrom = LocalDate.parse(dateFrom, DateTimeFormatter.ISO_LOCAL_DATE).format(DateTimeFormatter.ofPattern("dd.MM.uuuu"));


        try {
            String url = "http://angi.ru/section/90886-%D0%9D%D0%B5%D1%84%D1%82%D1%8C-%D0%B8-%D0%B3%D0%B0%D0%B7/";
            URLParameter parOne = URLParameter.builder().name(env.getProperty("dateFromParam")).value(dateFrom).build();
            AngiNewsFeed feed = nvgCreate(url, selectedDate);
            feed.action(dateFrom);
            feed.toExcel();
            emailSender.sendEmailWithAttachment(StaticData.emailTo, env.getProperty("ngvEmailSubject"), env.getProperty("ngvEmailContent"), env.getProperty("ngvExcelFilename"), env.getProperty("ngvExcelExportPath"));
        } catch (IOException | URISyntaxException | MessagingException e) {
            e.printStackTrace();
        }
    }

}
