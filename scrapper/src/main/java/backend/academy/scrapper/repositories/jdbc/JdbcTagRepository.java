package backend.academy.scrapper.repositories.jdbc;

import backend.academy.scrapper.models.Tag;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@AllArgsConstructor
@Transactional
public class JdbcTagRepository {

    private final JdbcClient jdbcClient;

    public void add(String name) {
        String sql = "INSERT INTO tag(name) values (?)";
        jdbcClient.sql(sql)
            .param(1, name)
            .update();
    }

    public Tag getByName(String name) {
        String sql = "SELECT * FROM tag WHERE name = ?";
        return jdbcClient.sql(sql)
            .param(1, name)
            .query(Tag.class)
            .single();
    }
}
