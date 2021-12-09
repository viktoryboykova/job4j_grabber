package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

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

                Element link = row.selectFirst("td.postslisttopic");
                if (link.text().startsWith("Важно")) {
                    continue;
                }
                Post post = new Post();
                post.setTitle(link.text());
                post.setLink(link.child(0).attr("href"));
                parseDetails(post.getLink(), post);
                System.out.println(post + "\n");
            }
        }
    }

    public static void parseDetails(String link, Post post) throws IOException {
        Document doc = Jsoup.connect(link).get();
        Element msgTable = doc.select("table.msgTable").first();
        Elements row = msgTable.select("tr");
        Element description = row.get(1).select("td.msgBody").get(1);
        post.setDescription(description.text());
        Element createdDate = row.last().selectFirst("td.msgFooter");
        String[] elements = createdDate.text().split(" \\[");
        String data = elements[0];
        SqlRuDateTimeParser parseDate = new SqlRuDateTimeParser();
        post.setCreated(parseDate.parse(data));
    }
}
