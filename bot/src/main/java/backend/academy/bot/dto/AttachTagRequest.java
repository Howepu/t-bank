package backend.academy.bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AttachTagRequest {

    @JsonProperty("url")
    private String url;

    @JsonProperty("tagName")
    private String tagName;
}
