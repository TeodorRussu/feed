package news.feed.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class YamlConfig {

    private String ngvBaseURL;
    private String ngvNewsPath;
    private String ngvExcelExportPath;
    private String ngvExcelFilename;
    private String ngvEmailSubject;
    private String ngvEmailContent;
    private String dateFromParam;
    private String dateToParam;
    private String paramQ;
    private String ngvExcelSheetName;


    private String angiBaseURL;
    private String angiNewsPath;
    private String angiExcelExportPath;
    private String angiExcelFilename;
    private String angiEmailSubject;
    private String angiEmailContent;
    private String angiExcelSheetName;
    private String angiFilterKeywords;


}
