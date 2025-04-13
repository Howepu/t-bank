package backend.academy.scrapper.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Chat implements Serializable {

    @Id
    @Column(name = "chat_id")
    @JsonProperty("chatId")
    private Long chatId;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Link> links = new ArrayList<>();

    public Chat(Long chatId) {
        this.chatId = chatId;
    }
}
