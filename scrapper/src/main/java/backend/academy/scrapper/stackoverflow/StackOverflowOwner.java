package backend.academy.scrapper.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class StackOverflowOwner {

    @JsonProperty("display_name")
    private String name;
}
