package net.voldrich.webclient.test.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequestComment extends IssueComment {
    @JsonProperty("pull_request_review_id")
    private Long pullRequestReviewId;
    @JsonProperty("diff_hunk")
    private String diffHunk;
    @JsonProperty("path")
    private String path;
    @JsonProperty("position")
    private Long position;
    @JsonProperty("original_position")
    private Long originalPosition;
    @JsonProperty("commit_id")
    private String commitId;
    @JsonProperty("original_commit_id")
    private String originalCommitId;
    @JsonProperty("pull_request_url")
    private String pullRequestUrl;

    public Long getPullRequestReviewId() {
        return pullRequestReviewId;
    }

    public void setPullRequestReviewId(Long pullRequestReviewId) {
        this.pullRequestReviewId = pullRequestReviewId;
    }

    public String getDiffHunk() {
        return diffHunk;
    }

    public void setDiffHunk(String diffHunk) {
        this.diffHunk = diffHunk;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public Long getOriginalPosition() {
        return originalPosition;
    }

    public void setOriginalPosition(Long originalPosition) {
        this.originalPosition = originalPosition;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getOriginalCommitId() {
        return originalCommitId;
    }

    public void setOriginalCommitId(String originalCommitId) {
        this.originalCommitId = originalCommitId;
    }

    public String getPullRequestUrl() {
        return pullRequestUrl;
    }

    public void setPullRequestUrl(String pullRequestUrl) {
        this.pullRequestUrl = pullRequestUrl;
    }

    @Override
    public Long getIssueNumber() {
        return getNumberFromUrl(pullRequestUrl);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("super", super.toString())
                .add("pullRequestReviewId", pullRequestReviewId)
                .add("diffHunk", diffHunk.length())
                .add("path", path)
                .add("position", position)
                .add("originalPosition", originalPosition)
                .add("commitId", commitId)
                .add("originalCommitId", originalCommitId)
                .add("pullRequestUrl", pullRequestUrl)
                .toString();
    }
}
