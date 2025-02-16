package backend.academy.scrapper.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ListLinksResponse {

    @JsonProperty("links")
    private List<LinkResponse> links;
    @JsonProperty("size")
    private int size;

}
