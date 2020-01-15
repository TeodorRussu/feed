package news.feed.logic;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

        try {
            String url = env.getProperty("angiNewsPath");
            AngiNewsFeed feed = nvgCreate(url, selectedDate);
            feed.action();
            feed.toExcel();
            emailSender.sendEmailWithAttachment(StaticData.emailTo, env.getProperty("angiEmailSubject"), env.getProperty("angiEmailContent"), env.getProperty("angiExcelFilename"), env.getProperty("angiExcelExportPath"));
        } catch (IOException | URISyntaxException | MessagingException e) {
            log.error(e.getMessage());
        }
    }

}
