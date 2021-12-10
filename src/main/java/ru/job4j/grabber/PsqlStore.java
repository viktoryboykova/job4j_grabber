package ru.job4j.grabber;

import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.SqlRuDateTimeParser;
import ru.job4j.html.SqlRuParse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(PsqlStore.class.getName());

    private Connection connection;

    public PsqlStore(Properties properties) {
        try {
            Class.forName(properties.getProperty("driver-class-name"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        try {
            connection = DriverManager.getConnection(properties.getProperty("url"), properties.getProperty("username"), properties.getProperty("password"));
        } catch (SQLException exception) {
            throw new RuntimeException("Connection failed", exception);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement =
                     connection.prepareStatement("insert into post(name, text, link, created) values (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            LOG.error("", e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("select * from post")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Post post = new Post();
                    post.setId(resultSet.getInt("id"));
                    post.setTitle(resultSet.getString("name"));
                    post.setDescription(resultSet.getString("text"));
                    post.setLink(resultSet.getString("link"));
                    post.setCreated(resultSet.getTimestamp("created").toLocalDateTime());
                    posts.add(post);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement = connection.prepareStatement("select * from post where id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    post = new Post();
                    post.setId(resultSet.getInt("id"));
                    post.setTitle(resultSet.getString("name"));
                    post.setDescription(resultSet.getString("text"));
                    post.setLink(resultSet.getString("link"));
                    post.setCreated(resultSet.getTimestamp("created").toLocalDateTime());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
}
