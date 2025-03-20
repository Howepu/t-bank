package backend.academy.scrapper.repositories.jdbc;


import backend.academy.scrapper.models.Link;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LinkRowMapper implements RowMapper<Link> {


    @Override
    public Link mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("id");
        String url = rs.getString("url");
        String tags = rs.getString("tags");
        String filtersArray = rs.getString("filters");

        // Check if filtersArray is null before accessing it
        String[] filters = (filtersArray != null) ? filtersArray.split(",") : new String[0];

        Link link = new Link(id, url, tags, filters);
        return link;
    }
}
