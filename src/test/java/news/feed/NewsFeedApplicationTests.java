package news.feed;

import news.feed.action.NvgNewsFeed;
import news.feed.action.URLParameter;
import news.feed.logic.NvgFeedHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URISyntaxException;

@SpringBootTest
class NewsFeedApplicationTests {

    @Autowired
    NvgFeedHandler creator;

    @Test
    void contextLoads() {
    }

    @Test
    void createAndTestURL() throws URISyntaxException {
        String expected = "http://www.ngv.ru/news/?DATE_ACTIVE_FROM=01.01.2020&DATE_ACTIVE_TO=01.02.2020&q=";
        String path = "http://www.ngv.ru/news/";
        URLParameter parOne = URLParameter.builder().name("DATE_ACTIVE_FROM").value("01.01.2020").build();
        URLParameter parTwo = URLParameter.builder().name("DATE_ACTIVE_TO").value("01.02.2020").build();
        URLParameter parThree = URLParameter.builder().name("q").value("").build();
        NvgNewsFeed feed = creator.nvgCreate(path, parOne, parTwo, parThree);

        Assertions.assertEquals(feed.getUrl(), expected);
    }

}
