package backend.academy.scrapper.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Link {

    private String url;
    private List<String> tags;
    private List<String> filters;
}
