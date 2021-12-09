package ru.job4j.grabber.utils;

import ru.job4j.grabber.utils.DateTimeParser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.util.Map.entry;

public class SqlRuDateTimeParser implements DateTimeParser {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MM yy");
    private DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("d MM yy, HH:mm");

    private static final Map<String, String> MONTHS = Map.ofEntries(
            entry("янв", "01"),
            entry("фев", "02"),
            entry("мар", "03"),
            entry("апр", "04"),
            entry("май", "05"),
            entry("июн", "06"),
            entry("июл", "07"),
            entry("авг", "08"),
            entry("сен", "09"),
            entry("окт", "10"),
            entry("ноя", "11"),
            entry("дек", "12"));

    @Override
    public LocalDateTime parse(String parse) {
        if (parse.contains("сегодня")) {
            parse = parse.replace("сегодня", formatter.format(LocalDateTime.now()));
        } else if (parse.contains("вчера")) {
            parse = parse.replace("вчера", formatter.format(LocalDateTime.now().minusDays(1)));
        } else {
            String[] words = parse.split(" ");
            String month = words[1];
            parse = parse.replace(month, MONTHS.get(month));
        }
        return LocalDateTime.parse(parse, formatter2);
    }
}
