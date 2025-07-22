package backend.academy.scrapper.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "link")
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "link_seq")
    @SequenceGenerator(name = "link_seq", sequenceName = "link_seq", allocationSize = 1)
    private Long id;

    @EqualsAndHashCode.Include
    private String url;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(
            name = "link_tags",
            joinColumns = {@JoinColumn(name = "link")})
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(
            name = "link_filters",
            joinColumns = {@JoinColumn(name = "link")})
    @Column(name = "value")
    @MapKeyColumn(name = "key")
    private Map<String, String> filters = new HashMap<>();

    public Link(String url, Set<String> tags, Map<String, String> filters) {
        this.url = url;
        this.tags = tags;
        this.filters = filters;
    }

    public Link(String url) {
        this.url = url;
    }
}
