
package net.voldrich.webclient.test.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.voldrich.webclient.test.GithubClient;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequest {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("number")
    private Long number;
    @JsonProperty("title")
    private String title;
    @JsonProperty("state")
    private String state;
    @JsonProperty("body")
    private String body;

    @JsonProperty("updated_at")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern= GithubClient.DATE_PATTERN)
    private Date updatedAt;

    @JsonProperty("merged_at")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern=GithubClient.DATE_PATTERN)
    private Date mergedAt;

    @JsonProperty("diff_url")
    private String diffUrl;

    @JsonProperty("head")
    private CommitReference head;
    @JsonProperty("base")
    private CommitReference base;

    @JsonProperty("number")
    public Long getNumber() {
        return number;
    }

    @JsonProperty("number")
    public void setNumber(Long number) {
        this.number = number;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("state")
    public String getState() {
        return state;
    }

    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("body")
    public String getBody() {
        return body;
    }

    @JsonProperty("body")
    public void setBody(String body) {
        this.body = body;
    }

    public CommitReference getHead() {
        return head;
    }

    public void setHead(CommitReference head) {
        this.head = head;
    }

    public CommitReference getBase() {
        return base;
    }

    public void setBase(CommitReference base) {
        this.base = base;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty("diff_url")
    public String getDiffUrl() {
        return diffUrl;
    }
    @JsonProperty("diff_url")
    public void setDiffUrl(String diffUrl) {
        this.diffUrl = diffUrl;
    }

    @JsonProperty("updated_at")
    public Date getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updated_at")
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getMergedAt() {
        return mergedAt;
    }

    public void setMergedAt(Date mergedAt) {
        this.mergedAt = mergedAt;
    }
}
