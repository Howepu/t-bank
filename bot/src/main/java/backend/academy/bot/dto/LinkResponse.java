package backend.academy.bot.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LinkResponse {

    private long chatId;
    private String url;
    private List<String> tags;
    private List<String> filters;

}
