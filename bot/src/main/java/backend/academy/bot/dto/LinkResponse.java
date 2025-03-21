package backend.academy.bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micrometer.core.instrument.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LinkResponse {

    @JsonProperty("chatId")
    private long chatId;

    @JsonProperty("url")
    private String url;

    @JsonProperty("tags")
    private Set<Tag> tags;

    @JsonProperty("filters")
    private List<String> filters;
}
