package net.voldrich.webclient.test;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import net.voldrich.webclient.test.dto.UserDetail;

public class GithubClientTest {

    public static final String OWNER = "facebook";
    public static final String REPOSITORY = "fresco";
    public static final String ACCESS_TOKEN = "d37a195e442b80dcc14aa31caf6afee49f724ed1";

    public static final GithubClientConfiguration CONFIG = new GithubClientConfiguration(
            OWNER, REPOSITORY,
            ACCESS_TOKEN);

    protected GithubClient githubClient = new GithubClient(CONFIG);

    @ParameterizedTest
    @EnumSource(GithubClient.Paging.class)
    public void testPaging(GithubClient.Paging paging) throws Exception {
        List<UserDetail> details = githubClient.loadContributorsPaged(paging)
                .collectList()
                .block();
        System.out.println(details.size());
        details.forEach(System.out::println);
    }

    @Test
    public void testPagingSingle() throws Exception {
        List<UserDetail> details = githubClient.loadContributorsPaged(GithubClient.Paging.PARALEL_CONCAT_MAP).collectList().block();
        System.out.println(details.size());
        details.forEach(System.out::println);
    }

}
