package backend.academy.bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class TagResponse {

    @JsonProperty("chatId")
    private Long chatId;

    @JsonProperty("name")
    private String name;
}
