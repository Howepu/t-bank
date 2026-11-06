package backend.academy.scrapper.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@Getter
public class StackOverflowResponse {

    @JsonProperty("items")
    private List<StackOverflowQuestionAnswer> items;
}
