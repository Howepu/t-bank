package backend.academy.bot.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class AddLinkRequest {

    private String link;
    private List<String> tags;
    private List<String> filters;
}
