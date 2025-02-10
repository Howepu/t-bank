package backend.academy.bot.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class LinkResponse {

    private long chatId;
    private String link;
    private String url;
    private List<String> tags;
    private List<String> filters;

    public String getUrl() {
        return url;
    }
}
