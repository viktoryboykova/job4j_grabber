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
        Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers").get();
        Elements forumTable = doc.select("table.forumTable");
        Elements rows = forumTable.select("tr");
        for (Element row : rows) {
            Element date = row.select("td.altCol").last();
            if (date == null) {
                continue;
            }
            System.out.println(date.text());
        }
    }
}
