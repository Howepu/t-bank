package backend.academy.scrapper.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Component
@Getter
public class LinkResponse {

    @JsonProperty("chatId")
    private long chatId;

    @JsonProperty("url")
    private String url;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("filters")
    private List<String> filters;

}
