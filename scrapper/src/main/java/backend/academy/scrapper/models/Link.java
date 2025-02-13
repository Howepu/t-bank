package backend.academy.scrapper.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Link {
    @JsonProperty("chatId")
    private long chatId;

    @JsonProperty("url")
    private String url;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("filters")
    private List<String> filters;


}
