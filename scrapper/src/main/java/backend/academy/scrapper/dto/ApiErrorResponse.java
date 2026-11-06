package backend.academy.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApiErrorResponse {

    @JsonProperty("description")
    private String description;

    @JsonProperty("code")
    private String code;

    @JsonProperty("exceptionName")
    private String exceptionName;

    @JsonProperty("exceptionMessage")
    private String exceptionMessage;

    @JsonProperty("stacktrace")
    private List<String> stacktrace;
}
