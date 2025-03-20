package backend.academy.scrapper.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class StackOverflowQuestionAnswer {

    @JsonProperty("question_id")
    private Long questionId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("owner")
    private StackOverflowOwner owner;

    @JsonProperty("last_activity_date")
    private Long lastActivityDate;

    @JsonProperty("creation_date")
    private Long creationDate;

    public LocalDateTime getLastActivityDateAsDateTime() {
        return lastActivityDate != null
                ? Instant.ofEpochSecond(lastActivityDate).atZone(ZoneOffset.UTC).toLocalDateTime()
                : null;
    }

    public LocalDateTime getCreationDate() {
        return creationDate != null
            ? Instant.ofEpochSecond(creationDate).atZone(ZoneOffset.UTC).toLocalDateTime()
            : null;
    }
}
