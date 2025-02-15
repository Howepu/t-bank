package backend.academy.bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Component
public class AddLinkRequest {

    @JsonProperty("url")
    private String url;
    @JsonProperty("tags")
    private List<String> tags;
    @JsonProperty("filters")
    private List<String> filters;

}
