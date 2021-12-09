package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Date;

public class SqlRuParse {
    public static void main(String[] args) throws Exception {
        SqlRuParse sqlRuParse = new SqlRuParse();
        sqlRuParse.parseHtml();
    }

    public void parseHtml() throws Exception {
        for (int i = 1; i <= 5; i++) {
            Document doc = Jsoup.connect(String.format("https://www.sql.ru/forum/job-offers/%s", i)).get();
            Elements forumTable = doc.select("table.forumTable");
            Elements rows = forumTable.select("tr");
            for (Element row : rows) {
                Element date = row.select("td.altCol").last();
                if (date == null) {
                    continue;
                }
                SqlRuDateTimeParser sqlRuDateTimeParser = new SqlRuDateTimeParser();
                System.out.println(sqlRuDateTimeParser.parse(date.text()));
            }
        }
    }
}
