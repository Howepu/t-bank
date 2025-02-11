package backend.academy.bot.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Component
@Getter
public class LinkResponse {

    private long chatId;
    private String url;
    private List<String> tags;
    private List<String> filters;

}
