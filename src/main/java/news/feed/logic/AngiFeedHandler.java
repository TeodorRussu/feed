package news.feed.logic;

import lombok.extern.slf4j.Slf4j;
import news.feed.action.AngiNewsFeed;
import news.feed.config.YamlConfig;
import news.feed.email.EmailSender;
import news.feed.env.StaticData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.mail.MessagingException;

@Component
@Slf4j
public class AngiFeedHandler {

    @Autowired
    EmailSender emailSender;
    @Autowired
    ApplicationContext springContext;
    @Autowired
    private YamlConfig yamlConfig;

    public AngiNewsFeed nvgCreate(String url, LocalDate selectedDate) throws URISyntaxException {
        AngiNewsFeed angiNewsFeed = springContext.getBean(AngiNewsFeed.class);
        angiNewsFeed.setUrl(url);
        angiNewsFeed.setSelectedDate(selectedDate);
        return angiNewsFeed;
    }

    public void nvgAction(String dateFrom) {
        LocalDate selectedDate = LocalDate.parse(dateFrom, DateTimeFormatter.ISO_LOCAL_DATE);

        try {
            String url = yamlConfig.getAngiNewsPath();
            AngiNewsFeed feed = nvgCreate(url, selectedDate);
            feed.action();
            feed.toExcel();
            emailSender.sendEmailWithAttachment(StaticData.emailTo, yamlConfig.getAngiEmailSubject(),
                                                yamlConfig.getAngiEmailContent(), yamlConfig.getAngiExcelFilename(),
                                                yamlConfig.getAngiExcelExportPath());
        } catch (IOException | URISyntaxException | MessagingException e) {
            log.error(e.getMessage());
        }
    }

}
