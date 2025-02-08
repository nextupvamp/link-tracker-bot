package backend.academy.bot.model;

import java.util.HashSet;
import java.util.Set;

public record Link(
    String url,
    Set<String> tags,
    Set<String> filters
) {
    public Link(String url) {
        this(url, new HashSet<>(), new HashSet<>());
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("URL: ").append(url).append("\n");

        sb.append("Tags:\n");
        if (tags != null && !tags.isEmpty()) {
            tags.forEach(tag -> sb.append("    ").append(tag).append('\n'));
        } else {
            sb.append("    no tags\n");
        }

        sb.append("Filters:\n");
        if (filters != null && !filters.isEmpty()) {
            filters.forEach(filter -> sb.append("    ").append(filter).append('\n'));
        } else {
            sb.append("    no filters\n");
        }

        return sb.toString();
    }
}
