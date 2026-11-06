package backend.academy.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TagResponse {

    @JsonProperty("chatId")
    private Long chatId;

    @JsonProperty("name")
    private String name;
}
