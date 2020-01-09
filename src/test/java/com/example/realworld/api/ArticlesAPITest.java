package com.example.realworld.api;

import com.example.realworld.RealworldDataIntegrationTest;
import com.example.realworld.domain.tag.model.NewTag;
import com.example.realworld.domain.user.model.User;
import com.example.realworld.infrastructure.web.model.response.ArticlesFeedResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.example.realworld.constants.TestsConstants.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(VertxExtension.class)
public class ArticlesAPITest extends RealworldDataIntegrationTest {

  private final String ARTICLES_PATH = API_PREFIX + "/articles";
  private final String FEED_PATH = ARTICLES_PATH + "/feed";

  @Test
  public void shouldReturn401WhenExecuteFeedOperationWithoutAuthorizationHeader(
      VertxTestContext vertxTestContext) {

    webClient
        .get(port, HOST, FEED_PATH)
        .as(BodyCodec.string())
        .send(
            vertxTestContext.succeeding(
                response ->
                    vertxTestContext.verify(
                        () -> {
                          assertThat(
                              response.statusCode(), is(HttpResponseStatus.UNAUTHORIZED.code()));
                          vertxTestContext.completeNow();
                        })));
  }

  @Test
  public void
      given10ArticlesForLoggedUser_whenExecuteFeedEndpointWithOffset0AndLimit5_shouldReturnListOf5Articles(
          VertxTestContext vertxTestContext) {

    User loggedUser = new User();
    loggedUser.setUsername("loggedUser");
    loggedUser.setEmail("loggedUser@mail.com");
    loggedUser.setPassword("loggedUser_123");

    User userFollowed = new User();
    userFollowed.setUsername("userFollowed");
    userFollowed.setEmail("userFollowed@mail.com");
    userFollowed.setPassword("userFollowed_123");

    NewTag tag1 = new NewTag();
    tag1.setName("tag1");

    NewTag tag2 = new NewTag();
    tag2.setName("tag2");

    createUser(loggedUser)
        .flatMap(
            createdLoggedUser ->
                createUser(userFollowed)
                    .flatMap(
                        createdUserFollowed ->
                            follow(createdLoggedUser, createdUserFollowed)
                                .flatMap(
                                    createdLoggedUserWithFollow ->
                                        createTags(tag1, tag2)
                                            .toList()
                                            .flatMap(
                                                tags ->
                                                    createArticles(
                                                            createdUserFollowed,
                                                            "title",
                                                            "description",
                                                            "body",
                                                            10,
                                                            tags)
                                                        .toList()
                                                        .map(articles -> createdLoggedUser)))))
        .subscribe(
            currentUser ->
                webClient
                    .get(port, HOST, FEED_PATH)
                    .putHeader(
                        AUTHORIZATION_HEADER,
                        AUTHORIZATION_HEADER_VALUE_PREFIX + currentUser.getToken())
                    .addQueryParam("offset", "0")
                    .addQueryParam("limit", "5")
                    .as(BodyCodec.string())
                    .send(
                        vertxTestContext.succeeding(
                            response ->
                                vertxTestContext.verify(
                                    () -> {
                                      ArticlesFeedResponse articlesFeedResponse =
                                          readValue(
                                              response.body(), ArticlesFeedResponse.class, false);
                                      assertThat(
                                          response.statusCode(), is(HttpResponseStatus.OK.code()));
                                      assertThat(articlesFeedResponse.getArticles().size(), is(5));
                                      assertThat(articlesFeedResponse.getArticlesCount(), is(10L));
                                      vertxTestContext.completeNow();
                                    }))));
  }
}
