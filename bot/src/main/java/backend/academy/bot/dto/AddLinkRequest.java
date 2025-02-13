package backend.academy.bot.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Component
public class AddLinkRequest {

    private String link;
    private List<String> tags;
    private List<String> filters;
}
