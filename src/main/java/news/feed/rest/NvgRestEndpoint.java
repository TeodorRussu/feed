package news.feed.rest;

import news.feed.logic.NvgFeedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("nvg-api")
public class NvgRestEndpoint {

    @Autowired
    private NvgFeedHandler nvgFeedHandler;

    @GetMapping
    void processNvgNewsFeed(@RequestParam("${dateFromParam}") String dateFrom,
                            @RequestParam("${dateToParam}") String dateTo,
                            @RequestParam("${paramQ}") String q) {

        nvgFeedHandler.nvgAction(dateFrom, dateTo, q);
    }
}
