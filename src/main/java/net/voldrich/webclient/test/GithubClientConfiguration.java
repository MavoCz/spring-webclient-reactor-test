package net.voldrich.webclient.test;

public class GithubClientConfiguration {

    private final String owner;

    private final String repository;

    private final String accessToken;

    private final int ratePerSecond;

    public GithubClientConfiguration(String owner, String repository, String accessToken, int ratePerSecond) {
        this.owner = owner;
        this.repository = repository;
        this.accessToken = accessToken;
        this.ratePerSecond = ratePerSecond;
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

    public int getRatePerSecond() {
        return ratePerSecond;
    }
}
