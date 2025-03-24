package backend.academy.bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AddLinkRequest {

    @JsonProperty("chatId")
    private long chatId;
    @JsonProperty("url")
    private String url;
    @JsonProperty("tags")
    private List<TagDto> tags;
    @JsonProperty("filters")
    private List<String> filters;
}
