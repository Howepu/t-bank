package backend.academy.bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LinkResponse {

    @JsonProperty("chatId")
    private long chatId;

    @JsonProperty("url")
    private String url;

    @JsonProperty("tags")
    private Set<TagDto> tags;

    @JsonProperty("filters")
    private List<String> filters;
}
