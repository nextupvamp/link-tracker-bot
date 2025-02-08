package backend.academy.scrapper.model;

import java.util.HashSet;
import java.util.Set;

public record Link(
    // id will be assigned later by persistence provider,
    // but now it's unused
    long id,
    String url,
    Set<String> tags,
    Set<String> filters
) {
    public Link(String url) {
        this(0, url, new HashSet<>(), new HashSet<>());
    }

    public Link(String url, Set<String> tags, Set<String> filters) {
        this(0, url, tags, filters);
    }

    @Override
    public int hashCode() {
        if (url == null) return 0;
        return url.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Link other) {
            if (url == null) return other.url == null;
            return url.equals(other.url);
        }
        return false;
    }
}
