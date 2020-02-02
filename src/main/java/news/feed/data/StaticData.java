package news.feed.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class StaticData {
    public static final Map<String, String> keywordsAndGroups;
    public static final List<String> orderedKeywords;
    public static int newsCounter = 0;

    static {
        keywordsAndGroups = new LinkedHashMap<>();
        keywordsAndGroups.put("Александр Новак", "Александр Новак");

        keywordsAndGroups.put("SOCAR", "SOCAR");
        keywordsAndGroups.put("Сокар", "SOCAR");
        keywordsAndGroups.put("СОКАР", "SOCAR");

        keywordsAndGroups.put("Лукойл", "ЛУКОЙЛ");
        keywordsAndGroups.put("ЛУКОЙЛ", "ЛУКОЙЛ");
        keywordsAndGroups.put("Lukoil", "ЛУКОЙЛ");

        keywordsAndGroups.put("Газпром нефт", "Газпром нефть");
        keywordsAndGroups.put("Газпром нефть", "Газпром нефть");

        keywordsAndGroups.put("Башнефт", "Башнефть");

        keywordsAndGroups.put("НОВАТЭК", "НОВАТЭК");
        keywordsAndGroups.put("Новатэк", "НОВАТЭК");

        keywordsAndGroups.put("Татнефть", "Татнефть");
        keywordsAndGroups.put("Татанефть", "Татнефть");
        keywordsAndGroups.put("Татнефт", "Татнефть");


        keywordsAndGroups.put("СИБУР", "СИБУР");
        keywordsAndGroups.put("Сибур", "СИБУР");

        keywordsAndGroups.put("Казмунайгаз", "КазМунайГаз");
        keywordsAndGroups.put("КМГ", "КазМунайГаз");
        keywordsAndGroups.put("КазМунайГаз", "КазМунайГаз");

        keywordsAndGroups.put("Роснефт", "Роснефть");


        keywordsAndGroups.put("Казахстан", "Казахстан");

        keywordsAndGroups.put("бар", "Цена нефть");
        keywordsAndGroups.put("баррель", "Цена нефть");
        keywordsAndGroups.put("цен нефт", "Цена нефть");
        keywordsAndGroups.put("Цен нефт", "Цена нефть");
        keywordsAndGroups.put("Цен Нефт", "Цена нефть");

        orderedKeywords = new ArrayList<>(keywordsAndGroups.keySet());







    }
}
