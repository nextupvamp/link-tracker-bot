package backend.academy.scrapper.model;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class Chat {
    private final long id;
    private Set<Link> links = new HashSet<>();

    public Optional<Link> findLink(String url) {
        Link example = new Link(url);
        return links.stream().filter(it -> it.equals(example)).findFirst();
    }
}
