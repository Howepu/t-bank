package backend.academy.bot.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ListLinkTagResponse {

    @JsonProperty("links")
    private List<LinkTagResponse> links;

    @JsonProperty("size")
    private int size;
}
