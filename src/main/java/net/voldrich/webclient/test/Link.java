package net.voldrich.webclient.test;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Link {

    protected static final Logger logger = LoggerFactory.getLogger(Link.class);

    private URI uri;

    private final Map<String, String> params = new HashMap<>();

    public String getRel() {
        return params.get("rel");
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    private void setParam(String name, String value) {
        this.params.put(name, value);
    }

    // following code was taken from Jersey, just refactored to not use anything special
    // seems that Webclient does not have such parsing utilities. Example of HTTP links:
    // <https://api.github.com/repositories/31533997/contributors?per_page=10&page=2>; rel="next", <https://api
    // .github.com/repositories/31533997/contributors?per_page=10&page=10>; rel="last"

    public static Set<Link> parseLinks(List<String> links) {
        if (links == null || links.isEmpty()) {
            return Collections.emptySet();
        }

        try {
            Set<Link> result = new HashSet<Link>(links.size());
            StringBuilder linkString;
            for (String link : links) {
                linkString = new StringBuilder();
                StringTokenizer st = new StringTokenizer(link, "<>,", true);
                boolean linkOpen = false;
                while (st.hasMoreTokens()) {
                    String n = st.nextToken();
                    if (n.equals("<")) {
                        linkOpen = true;
                    } else if (n.equals(">")) {
                        linkOpen = false;
                    } else if (!linkOpen && n.equals(",")) {
                        result.add(Link.parseLink(linkString.toString().trim()));
                        linkString = new StringBuilder();
                        continue; // don't add the ","
                    }

                    linkString.append(n);
                }

                if (linkString.length() > 0) {
                    result.add(Link.parseLink(linkString.toString().trim()));
                }
            }
            return result;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to parse links", e);
        }
    }

    static Link parseLink(String value) {
        Link link = new Link();

        try {
            value = value.trim();
            if (!value.startsWith("<")) {
                throw new IllegalArgumentException("Missing starting token < in " + value);
            }

            int gtIndex = value.indexOf(62);
            if (gtIndex == -1) {
                throw new IllegalArgumentException("Missing token > in " + value);
            }

            link.setUri(new URI(value.substring(1, gtIndex).trim()));
            String params = value.substring(gtIndex + 1).trim();

            String n;
            String v;
            for (StringTokenizer st = new StringTokenizer(params, ";=\"", true); st.hasMoreTokens(); link.setParam(n,
                    v)) {
                checkToken(st, ";");
                n = st.nextToken().trim();
                checkToken(st, "=");
                v = nextNonEmptyToken(st);
                if (v.equals("\"")) {
                    v = st.nextToken();
                    checkToken(st, "\"");
                }
            }
        } catch (Throwable ex) {
            logger.warn("Error parsing link value '" + value + "'", ex);
            link = null;
        }

        if (link == null) {
            throw new IllegalArgumentException("Unable to parse link " + value);
        } else {
            return link;
        }
    }

    private static String nextNonEmptyToken(StringTokenizer st) throws IllegalArgumentException {
        String token;
        do {
            token = st.nextToken().trim();
        } while (token.length() == 0);

        return token;
    }

    private static void checkToken(StringTokenizer st, String expected) throws IllegalArgumentException {
        String token;
        do {
            token = st.nextToken().trim();
        } while (token.length() == 0);

        if (!token.equals(expected)) {
            throw new IllegalArgumentException("Expected token " + expected + " but found " + token);
        }
    }
}
