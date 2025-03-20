package backend.academy.scrapper.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "link")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Link {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JsonProperty("url")
    @Column(name = "url")
    private String url;

    @JsonProperty("filters")
    @Column(name = "filters", columnDefinition = "TEXT[]")
    private List<String> filters = new ArrayList<>();

    @JsonProperty("createdAt")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JsonProperty("knownIssueIds")
    @Column(name = "known_issue_ids", columnDefinition = "BIGINT[]")
    private List<Long> knownIssueIds = new ArrayList<>();

    @JsonProperty("knownPrIds")
    @Column(name = "known_pr_ids", columnDefinition = "BIGINT[]")
    private List<Long> knownPrIds = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "chat_id", referencedColumnName = "chat_id", nullable = false)
    private Chat chat;

    @JsonProperty("tags")
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "link_tag",
        joinColumns = @JoinColumn(name = "link_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id")
    )
    private Set<Tag> assignedTags = new HashSet<>();

    public Link(Long id, String url, String tags, String[] filters) {
    }

    public Link(long id, String url, List<String> filters, LocalDateTime createdAt, Chat chatId) {
    }
}

