package ru.job4j.grabber;

import java.util.List;

public interface Parse {
    List<Post> list(String link) throws Exception;

    Post detail(String link) throws Exception;
}
