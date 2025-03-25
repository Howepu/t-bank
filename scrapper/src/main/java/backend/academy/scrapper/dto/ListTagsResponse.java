package backend.academy.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ListTagsResponse {

    @JsonProperty("tags")
    private List<TagResponse> tags;

    @JsonProperty("size")
    private int size;
}
