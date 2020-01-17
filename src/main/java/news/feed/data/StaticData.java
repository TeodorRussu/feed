package news.feed.data;

import java.util.Map;

import static java.util.Map.entry;

public class StaticData {
    public static final Map<String, String> keywordsAndGroups;

    static {
        keywordsAndGroups = Map.ofEntries(
                entry("Александр Новак", "Александр Новак"),

                entry("SOCAR", "SOCAR"),
                entry("Сокар", "SOCAR"),
                entry("СОКАР", "SOCAR"),

                entry("баррель", "Цена нефть"),
                entry("цен нефт", "Цена нефть"),
                entry("Цен нефт", "Цена нефть"),
                entry("Цен Нефт", "Цена нефть"),

                entry("Лукойл", "ЛУКОЙЛ"),
                entry("ЛУКОЙЛ", "ЛУКОЙЛ"),
                entry("Lukoil", "ЛУКОЙЛ"),

                entry("Роснефт", "Роснефть"),

                entry("Газпром нефт", "Газпром нефть"),
                entry("Газпром нефть", "Газпром нефть"),

                entry("Башнефт", "Башнефть"),

                entry("НОВАТЭК", "НОВАТЭК"),
                entry("Новатэк", "НОВАТЭК"),

                entry("Татнефть", "Татнефть"),
                entry("Татанефть", "Татнефть"),
                entry("Татнефт", "Татнефть"),


                entry("СИБУР", "СИБУР"),
                entry("Сибур", "СИБУР"),

                entry("Казмунайгаз", "КазМунайГаз"),
                entry("КМГ", "КазМунайГаз"),
                entry("КазМунайГаз", "КазМунайГаз")
        );
    }
}
