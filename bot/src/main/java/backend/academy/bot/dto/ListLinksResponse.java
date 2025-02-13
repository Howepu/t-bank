package backend.academy.bot.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Component
@Getter
public class ListLinksResponse {

    private List<LinkResponse> links;
    private int size;

}
