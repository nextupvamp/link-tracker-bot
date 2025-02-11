package backend.academy.scrapper.model;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Subscription {
    private final String url;
    private final Site site;
    private final Set<Chat> subscribers = new HashSet<>();
    @Setter private boolean updated;
    // consider last update on create time because we don't observe updates before subscription
    @Setter private long lastUpdate = System.currentTimeMillis() / 1000; // to seconds

    public Subscription(String url, Site site) {
        this.url = url;
        this.site = site;
    }
}
