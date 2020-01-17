package news.feed;

import lombok.extern.slf4j.Slf4j;
import news.feed.action.NvgNewsFeed;
import news.feed.data.StaticData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
@Slf4j
public class NewsFeedApplication implements CommandLineRunner {

    @Autowired
    NvgNewsFeed feed;

    public static void main(String[] args) {
        SpringApplication.run(NewsFeedApplication.class, args);
    }

    @Override
    public void run(String... args) throws IOException {
        log.info("EXECUTING : command line runner");
        log.info(StaticData.orderedKeywords.toString());
    }

}