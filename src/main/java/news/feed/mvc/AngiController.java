package news.feed.mvc;

import news.feed.env.StaticData;
import news.feed.logic.AngiFeedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/angi")
public class AngiController {

    @Autowired
    private AngiFeedHandler angiFeedHandler;


    @GetMapping("")
    public String showFilterPage() {
        return "angi-filter-page";
    }

    @GetMapping("/export")
    public String processNvgNewsFeed(@RequestParam("${dateFromParam}") String dateFrom,
                                     @RequestParam("email") String email) {

        StaticData.emailTo = email;
        angiFeedHandler.nvgAction(dateFrom);
        return "success";
    }

}