package backend.academy.bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ListLinksResponse {

    @JsonProperty("links")
    private List<LinkResponse> links;

    @JsonProperty("size")
    private int size;
}
