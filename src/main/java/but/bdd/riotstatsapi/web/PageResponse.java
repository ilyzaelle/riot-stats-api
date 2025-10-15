package but.bdd.riotstatsapi.web;

import java.util.List;
import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private String sort;
}
