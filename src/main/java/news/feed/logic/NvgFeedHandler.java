package news.feed.logic;

import news.feed.action.NvgNewsFeed;
import news.feed.action.URLParameter;
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
public class NvgFeedHandler {

    @Autowired
    YamlConfig env;

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
        dateFrom =
            LocalDate.parse(dateFrom, DateTimeFormatter.ISO_LOCAL_DATE)
                .format(DateTimeFormatter.ofPattern("dd.MM.uuuu"));
        dateTo =
            LocalDate.parse(dateTo, DateTimeFormatter.ISO_LOCAL_DATE).format(DateTimeFormatter.ofPattern("dd.MM.uuuu"));

        try {
            String url = env.getNgvNewsPath();
            URLParameter parOne = URLParameter.builder().name(env.getDateFromParam()).value(dateFrom).build();
            URLParameter parTwo = URLParameter.builder().name(env.getDateToParam()).value(dateTo).build();
            URLParameter parThree = URLParameter.builder().name("q").value(q).build();
            NvgNewsFeed feed = nvgCreate(url, parOne, parTwo, parThree);
            feed.action();
            feed.toExcel();
            emailSender.sendEmailWithAttachment(StaticData.emailTo, env.getNgvEmailSubject(),
                                                env.getNgvEmailContent(), env.getNgvExcelFilename(),
                                                env.getNgvExcelExportPath());
        } catch (IOException | URISyntaxException | MessagingException e) {
            e.printStackTrace();
        }
    }

}
