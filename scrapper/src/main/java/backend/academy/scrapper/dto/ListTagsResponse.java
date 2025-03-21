package backend.academy.scrapper.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ListTagsResponse {

    @JsonProperty("tags")
    private List<TagResponse> tags;

    @JsonProperty("size")
    private int size;
}
