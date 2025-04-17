package backend.academy.scrapper.dto;

import backend.academy.scrapper.models.Filter;
import backend.academy.scrapper.models.Tag;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private Set<Tag> tags;

    @JsonProperty("filters")
    private Set<Filter> filters;
}
