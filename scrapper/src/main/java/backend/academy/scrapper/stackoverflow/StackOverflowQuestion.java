package backend.academy.scrapper.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class StackOverflowQuestion {

    @JsonProperty("question_id")
    private Long questionId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("last_activity_date")
    private Long lastActivityDate;

    public LocalDateTime getLastActivityDateAsDateTime() {
        return lastActivityDate != null ? Instant.ofEpochSecond(lastActivityDate).atZone(ZoneOffset.UTC).toLocalDateTime() : null;
    }
}
