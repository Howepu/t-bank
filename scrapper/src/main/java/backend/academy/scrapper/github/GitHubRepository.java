package backend.academy.scrapper.github;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class GitHubRepository {

    private long id;
    private String name;
    private String fullName;
    private String description;
    private String htmlUrl;
    private String language;
    private int stargazersCount;
    private String updatedAt;

}
