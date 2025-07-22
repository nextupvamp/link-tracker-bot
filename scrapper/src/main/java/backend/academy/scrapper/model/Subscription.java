package backend.academy.scrapper.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "subscription")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Subscription {

    @Id
    private String url;

    @Enumerated(value = EnumType.STRING)
    private Site site;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "subscriber",
            inverseJoinColumns = @JoinColumn(name = "chat_id"),
            joinColumns = @JoinColumn(name = "subscription"))
    private final Set<Chat> subscribers = new HashSet<>();

    private boolean updated;

    // consider last update on create time because we don't observe updates before subscription
    private long lastUpdate = System.currentTimeMillis() / 1000; // to seconds

    public Subscription(String url, Site site) {
        this.url = url;
        this.site = site;
    }

    public void update() {
        lastUpdate = System.currentTimeMillis() / 1000;
    }
}
