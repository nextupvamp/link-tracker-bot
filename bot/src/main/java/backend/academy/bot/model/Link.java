package backend.academy.bot.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record Link(Long id, String url, Set<String> tags, Map<String, String> filters) {
    public Link(String url) {
        this(null, url, new HashSet<>(), new HashMap<>());
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
            filters.forEach((k, v) ->
                    sb.append("    ").append(k).append(" = ").append(v).append('\n'));
        } else {
            sb.append("    no filters\n");
        }

        return sb.toString();
    }
}
