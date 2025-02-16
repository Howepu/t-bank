package backend.academy.scrapper.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class GitHubRepository {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("default_branch")
    private String defaultBranch;

    @JsonProperty("pushed_at")
    private String pushedAt;

    public LocalDateTime getPushedAtAsDateTime() {
        return pushedAt != null ? Instant.parse(pushedAt).atZone(ZoneOffset.UTC).toLocalDateTime() : null;
    }
}
