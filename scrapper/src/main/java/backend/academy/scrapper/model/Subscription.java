package backend.academy.scrapper.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class Subscription {
    private final String url;
    private final Site site;
    @Setter private long lastUpdate;
}
