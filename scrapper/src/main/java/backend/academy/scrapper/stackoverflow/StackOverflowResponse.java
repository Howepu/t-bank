package backend.academy.scrapper.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class StackOverflowResponse {

    @JsonProperty("items")
    private List<StackOverflowQuestion> items;

}
