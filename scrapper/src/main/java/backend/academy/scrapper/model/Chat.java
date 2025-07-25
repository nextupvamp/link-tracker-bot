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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "chat")
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class Chat {

    @Id
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    private ChatState state;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "curr_edited_link")
    private Link currentEditedLink;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "link_id"),
            name = "chat_links")
    private Set<Link> links = new HashSet<>();

    public Chat(Long id, ChatState state) {
        this.id = id;
        this.state = state;
    }

    public Optional<Link> findLink(String url) {
        Link example = new Link(url);
        return links.stream().filter(it -> it.equals(example)).findFirst();
    }
}
