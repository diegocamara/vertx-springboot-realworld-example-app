package com.example.realworld.api;

import com.example.realworld.RealworldDataIntegrationTest;
import com.example.realworld.constants.TestsConstants;
import com.example.realworld.domain.user.model.User;
import com.example.realworld.infrastructure.web.model.response.ProfileResponse;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.example.realworld.constants.TestsConstants.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(VertxExtension.class)
public class ProfilesAPITest extends RealworldDataIntegrationTest {

  private final String PROFILES_RESOURCE_PATH = API_PREFIX + "/profiles";

  @Test
  public void givenAnPersistedUserShouldReturnProfileDataWithStatusCode200(
      VertxTestContext vertxTestContext) {

    User user = new User();
    user.setUsername("user1");
    user.setEmail("user1@mail.com");
    user.setImage("image");
    user.setBio("bio");
    user.setPassword("user1_123");

    saveUser(user);

    webClient
        .get(port, TestsConstants.HOST, PROFILES_RESOURCE_PATH + "/" + user.getUsername())
        .as(BodyCodec.string())
        .send(
            vertxTestContext.succeeding(
                response ->
                    vertxTestContext.verify(
                        () -> {
                          ProfileResponse profileResponse =
                              readValue(response.body(), ProfileResponse.class, true);
                          assertThat(profileResponse.getUsername(), is(user.getUsername()));
                          assertThat(profileResponse.getBio(), is(user.getBio()));
                          assertThat(profileResponse.getImage(), is(user.getImage()));
                          assertThat(profileResponse.isFollowing(), is(false));
                          vertxTestContext.completeNow();
                        })));
  }

  @Test
  public void
      givenAnPersistedUserWithFollowedUserShouldReturnProfileDataWithFollowingPropertyValueSettingToTrueAndStatusCode200(
          VertxTestContext vertxTestContext) {

    User user1 = new User();
    user1.setUsername("user1");
    user1.setEmail("user1@mail.com");
    user1.setImage("image");
    user1.setBio("bio");
    user1.setPassword("user1_123");

    User user2 = new User();
    user2.setUsername("user2");
    user2.setEmail("user2@mail.com");
    user2.setImage("image");
    user2.setBio("bio");
    user2.setPassword("user2_123");

    saveUsers(user1, user2);
    follow(user1, user2);

    webClient
        .get(port, TestsConstants.HOST, PROFILES_RESOURCE_PATH + "/" + user2.getUsername())
        .putHeader(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE_PREFIX + user1.getToken())
        .as(BodyCodec.string())
        .send(
            vertxTestContext.succeeding(
                response ->
                    vertxTestContext.verify(
                        () -> {
                          ProfileResponse profileResponse =
                              readValue(response.body(), ProfileResponse.class, true);
                          assertThat(profileResponse.getUsername(), is(user2.getUsername()));
                          assertThat(profileResponse.getBio(), is(user2.getBio()));
                          assertThat(profileResponse.getImage(), is(user2.getImage()));
                          assertThat(profileResponse.isFollowing(), is(true));
                          vertxTestContext.completeNow();
                        })));
  }

  @Test
  public void
      givenPersistedUserWhenExecuteFollowOperationShouldReturnProfileDataWithFollowingPropertyValueSettingToTrue(
          VertxTestContext vertxTestContext) {

    User user1 = new User();
    user1.setUsername("user1");
    user1.setEmail("user1@mail.com");
    user1.setImage("image");
    user1.setBio("bio");
    user1.setPassword("user1_123");

    User user2 = new User();
    user2.setUsername("user2");
    user2.setEmail("user2@mail.com");
    user2.setImage("image");
    user2.setBio("bio");
    user2.setPassword("user2_123");

    saveUsers(user1, user2);

    webClient
        .post(port, HOST, PROFILES_RESOURCE_PATH + "/" + user2.getUsername() + "/follow")
        .putHeader(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE_PREFIX + user1.getToken())
        .as(BodyCodec.string())
        .send(
            vertxTestContext.succeeding(
                response ->
                    vertxTestContext.verify(
                        () -> {
                          ProfileResponse profileResponse =
                              readValue(response.body(), ProfileResponse.class, true);
                          assertThat(profileResponse.getUsername(), is(user2.getUsername()));
                          assertThat(profileResponse.getBio(), is(user2.getBio()));
                          assertThat(profileResponse.getImage(), is(user2.getImage()));
                          assertThat(profileResponse.isFollowing(), is(true));
                          vertxTestContext.completeNow();
                        })));
  }

  @Test
  public void
      givenAnFollowedUserWhenExecuteUnfollowOperationShouldReturnProfileDataWithFollowingPropertyValueSettingToFalse(
          VertxTestContext vertxTestContext) {

    User user1 = new User();
    user1.setUsername("user1");
    user1.setEmail("user1@mail.com");
    user1.setImage("image");
    user1.setBio("bio");
    user1.setPassword("user1_123");

    User user2 = new User();
    user2.setUsername("user2");
    user2.setEmail("user2@mail.com");
    user2.setImage("image");
    user2.setBio("bio");
    user2.setPassword("user2_123");

    saveUsers(user1, user2);
    follow(user1, user2);

    webClient
        .delete(
            port,
            TestsConstants.HOST,
            PROFILES_RESOURCE_PATH + "/" + user2.getUsername() + "/follow")
        .putHeader(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE_PREFIX + user1.getToken())
        .as(BodyCodec.string())
        .send(
            vertxTestContext.succeeding(
                response ->
                    vertxTestContext.verify(
                        () -> {
                          ProfileResponse profileResponse =
                              readValue(response.body(), ProfileResponse.class, true);
                          assertThat(profileResponse.getUsername(), is(user2.getUsername()));
                          assertThat(profileResponse.getBio(), is(user2.getBio()));
                          assertThat(profileResponse.getImage(), is(user2.getImage()));
                          assertThat(profileResponse.isFollowing(), is(false));
                          vertxTestContext.completeNow();
                        })));
  }
}
