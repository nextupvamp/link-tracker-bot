package backend.academy.scrapper.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "link")
@Entity
@Data
@NoArgsConstructor
public class Link {
    @Id
    @GeneratedValue
    private Long id;

    private String url;

    @ElementCollection
    @JoinTable(
            name = "link_tags",
            joinColumns = {@JoinColumn(name = "link")})
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @ElementCollection
    @JoinTable(
            name = "link_filters",
            joinColumns = {@JoinColumn(name = "link")})
    @Column(name = "filter")
    private Set<String> filters = new HashSet<>();

    public Link(String url, Set<String> tags, Set<String> filters) {
        this.url = url;
        this.tags = tags;
        this.filters = filters;
    }

    public Link(String url) {
        this.url = url;
    }
}
