package net.voldrich.webclient.test.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import net.voldrich.webclient.test.GithubClient;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueComment {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("user")
    private User user;

    @JsonProperty("created_at")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern= GithubClient.DATE_PATTERN)
    private Date createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern=GithubClient.DATE_PATTERN)
    private Date updatedAt;

    @JsonProperty("body")
    private String body;

    @JsonProperty("url")
    private String url;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("issue_url")
    private String issueUrl;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getIssueUrl() {
        return issueUrl;
    }

    public void setIssueUrl(String issueUrl) {
        this.issueUrl = issueUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Long getIssueNumber() {
        return getNumberFromUrl(issueUrl);
    }

    protected Long getNumberFromUrl(String url) {
        if (url != null) {
            return Long.parseLong(url.substring(url.lastIndexOf('/') + 1));
        }
        return null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("user", user)
                .add("createdAt", createdAt)
                .add("updatedAt", updatedAt)
                .add("body", body.length())
                .add("url", url)
                .add("htmlUrl", htmlUrl)
                .add("issueUrl", issueUrl)
                .toString();
    }
}
