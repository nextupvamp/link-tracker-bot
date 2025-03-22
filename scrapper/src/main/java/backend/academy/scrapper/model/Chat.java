package backend.academy.scrapper.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "chat")
@Entity
@Data
@NoArgsConstructor
public class Chat {
    @Id
    private Long id;

    @OneToMany(orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinTable(
            name = "chat_links",
            inverseJoinColumns = @JoinColumn(name = "link_id"),
            joinColumns = {@JoinColumn(name = "chat_id")})
    private Set<Link> links = new HashSet<>();

    public Chat(Long id) {
        this.id = id;
    }

    public Optional<Link> findLink(String url) {
        Link example = new Link(url);
        return links.stream().filter(it -> it.equals(example)).findFirst();
    }

    // @EqualsAndHashcode doesn't work in a set of chats
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Chat chat = (Chat) object;
        return Objects.equals(id, chat.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
