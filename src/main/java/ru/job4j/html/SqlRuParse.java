package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.Parse;
import ru.job4j.grabber.Post;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.SqlRuDateTimeParser;

import java.util.ArrayList;
import java.util.List;

public class SqlRuParse implements Parse {

    private final DateTimeParser dateTimeParser;

    public SqlRuParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) throws Exception {
        SqlRuDateTimeParser sqlRuDateTimeParser = new SqlRuDateTimeParser();
        SqlRuParse sqlRuParse = new SqlRuParse(sqlRuDateTimeParser);
        sqlRuParse.parseHtml();
    }

    public void parseHtml() throws Exception {
        for (int i = 1; i <= 5; i++) {
            List<Post> posts = list(String.format("https://www.sql.ru/forum/job-offers/%s", i));
            for (Post post : posts) {
                System.out.println(post + "\n");
            }
        }
    }

    @Override
    public List<Post> list(String link) throws Exception {
        List<Post> posts = new ArrayList<>();
        Document doc = Jsoup.connect(link).get();
        Elements forumTable = doc.select("table.forumTable");
        Elements rows = forumTable.select("tr");
        for (Element row : rows) {
            Element dateUpdate = row.select("td.altCol").last();
            if (dateUpdate == null) {
                continue;
            }
            Element linkPost = row.selectFirst("td.postslisttopic");
            if (linkPost.text().startsWith("Важно")) {
                continue;
            }
            posts.add(detail(linkPost.child(0).attr("href")));
        }
        return posts;
    }

    @Override
    public Post detail(String link) throws Exception {
        Post post = new Post();
        Document doc = Jsoup.connect(link).get();
        Element msgTable = doc.select("table.msgTable").first();
        Elements row = msgTable.select("tr");
        Element createdDate = row.last().selectFirst("td.msgFooter");
        String[] elements = createdDate.text().split(" \\[");
        String data = elements[0];
        post.setTitle(row.get(0).select("td.messageHeader").text());
        post.setLink(link);
        post.setDescription(row.get(1).select("td.msgBody").get(1).text());
        post.setCreated(dateTimeParser.parse(data));
        return post;
    }
}
