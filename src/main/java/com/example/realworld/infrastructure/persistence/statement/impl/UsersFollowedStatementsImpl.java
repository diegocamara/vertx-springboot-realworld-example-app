package com.example.realworld.infrastructure.persistence.statement.impl;

import com.example.realworld.infrastructure.persistence.statement.Statement;
import com.example.realworld.infrastructure.persistence.statement.UsersFollowedStatements;
import io.vertx.core.json.JsonArray;
import org.springframework.stereotype.Component;

@Component
public class UsersFollowedStatementsImpl extends AbstractStatements
    implements UsersFollowedStatements {

  @Override
  public Statement<JsonArray> countByCurrentUserIdAndUserFollowedId(
      String currentUserId, String userFollowedId) {

    String sql = "SELECT COUNT(*) FROM USERS_FOLLOWED WHERE USER_ID = ? AND FOLLOWED_ID = ?";

    JsonArray params = new JsonArray().add(currentUserId).add(userFollowedId);

    return new JsonArrayStatement(sql, params);
  }

  @Override
  public Statement<JsonArray> follow(String currentUserId, String userFollowedId) {

    String sql = "INSERT INTO USERS_FOLLOWED (USER_ID, FOLLOWED_ID) VALUES (?, ?)";

    JsonArray params = new JsonArray().add(currentUserId).add(userFollowedId);

    return new JsonArrayStatement(sql, params);
  }

  @Override
  public Statement<JsonArray> unfollow(String currentUserId, String userFollowedId) {

    String sql = "DELETE FROM USERS_FOLLOWED WHERE USER_ID = ? AND FOLLOWED_ID = ?";

    JsonArray params = new JsonArray().add(currentUserId).add(userFollowedId);

    return new JsonArrayStatement(sql, params);
  }

  @Override
  public Statement<JsonArray> findRecentArticles(String currentUserId, int offset, int limit) {

    String sql =
        "SELECT articles.ID, "
            + "articles.TITLE, "
            + "articles.DESCRIPTION, "
            + "articles.BODY, "
            + "articles.SLUG, "
            + "articles.CREATED_AT, "
            + "articles.UPDATED_AT, "
            + "users2.ID AS AUTHOR_ID, "
            + "users2.USERNAME AS AUTHOR_USERNAME "
            + "FROM USERS_FOLLOWED users_followed "
            + "INNER JOIN USERS users1 ON users_followed.USER_ID = users1.ID AND (users1.ID = ?) "
            + "INNER JOIN USERS users2 ON users_followed.FOLLOWED_ID = users2.ID "
            + "INNER JOIN ARTICLES articles ON users2.ID = articles.AUTHOR_ID ORDER BY articles.CREATED_AT DESC LIMIT ? OFFSET ?";

    JsonArray params = new JsonArray().add(currentUserId).add(limit).add(offset);

    return new JsonArrayStatement(sql, params);
  }

  @Override
  public Statement<JsonArray> totalUserArticlesFollowed(String currentUserId) {

    String sql =
        "SELECT COUNT(DISTINCT articles.ID) "
            + "FROM USERS_FOLLOWED users_followed "
            + "INNER JOIN USERS users1 ON users_followed.USER_ID = users1.ID AND (users1.ID = ?) "
            + "INNER JOIN USERS users2 ON users_followed.FOLLOWED_ID = users2.ID "
            + "INNER JOIN ARTICLES articles ON users2.ID = articles.AUTHOR_ID";

    JsonArray params = new JsonArray().add(currentUserId);

    return new JsonArrayStatement(sql, params);
  }
}
