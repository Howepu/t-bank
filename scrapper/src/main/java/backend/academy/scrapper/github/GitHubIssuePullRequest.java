package backend.academy.scrapper.github;


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
public class GitHubIssuePullRequest {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("user")
    private GitHubUser gitHubUser;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("body")
    private String body;

    public LocalDateTime getCreatedAtAsDateTime() {
        return createdAt != null ? Instant.parse(createdAt).atZone(ZoneOffset.UTC).toLocalDateTime() : null;
    }

}
