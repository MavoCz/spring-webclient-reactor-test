package net.voldrich.webclient.test;

public class GithubClientConfiguration {

    private final String owner;

    private final String repository;

    private final String accessToken;

    public GithubClientConfiguration(String owner, String repository, String accessToken) {
        this.owner = owner;
        this.repository = repository;
        this.accessToken = accessToken;
    }

    public String getOwner() {
        return owner;
    }

    public String getRepository() {
        return repository;
    }

    public String getAccessToken() {
        return accessToken;
    }

}
