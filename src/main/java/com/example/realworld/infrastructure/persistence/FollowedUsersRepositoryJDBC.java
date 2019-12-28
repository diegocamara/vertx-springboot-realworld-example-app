package com.example.realworld.infrastructure.persistence;

import com.example.realworld.domain.article.model.Article;
import com.example.realworld.domain.user.model.FollowedUsersRepository;
import com.example.realworld.infrastructure.persistence.statement.FollowedUsersStatements;
import com.example.realworld.infrastructure.persistence.statement.Statement;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FollowedUsersRepositoryJDBC extends JDBCRepository implements FollowedUsersRepository {

  private JDBCClient jdbcClient;
  private FollowedUsersStatements followedUsersStatements;

  public FollowedUsersRepositoryJDBC(
      JDBCClient jdbcClient, FollowedUsersStatements followedUsersStatements) {
    this.jdbcClient = jdbcClient;
    this.followedUsersStatements = followedUsersStatements;
  }

  @Override
  public Single<Long> countByCurrentUserIdAndFollowedUserId(
      String currentUserId, String followedUserId) {
    Statement<JsonArray> countByCurrentUserIdAndFollowedUserIdStatement =
        followedUsersStatements.countByCurrentUserIdAndFollowedUserId(
            currentUserId, followedUserId);
    return jdbcClient
        .rxQueryWithParams(
            countByCurrentUserIdAndFollowedUserIdStatement.sql(),
            countByCurrentUserIdAndFollowedUserIdStatement.params())
        .map(this::getCountFromResultSet);
  }

  @Override
  public Completable follow(String currentUserId, String followedUserId) {
    Statement<JsonArray> followStatement =
        followedUsersStatements.follow(currentUserId, followedUserId);
    return jdbcClient
        .rxUpdateWithParams(followStatement.sql(), followStatement.params())
        .flatMapCompletable(updateResult -> Completable.complete());
  }

  @Override
  public Completable unfollow(String currentUserId, String followedUserId) {
    Statement<JsonArray> unfollowStatement =
        followedUsersStatements.unfollow(currentUserId, followedUserId);
    return jdbcClient
        .rxUpdateWithParams(unfollowStatement.sql(), unfollowStatement.params())
        .flatMapCompletable(
            updateResult -> {
              return Completable.complete();
            });
  }

  @Override
  public Single<Optional<List<Article>>> findRecentArticles(
      String currentUserId, int offset, int limit) {
    Statement<JsonArray> findRecentArticlesStatement =
        followedUsersStatements.findRecentArticles(currentUserId, offset, limit);
    return jdbcClient
        .rxQueryWithParams(findRecentArticlesStatement.sql(), findRecentArticlesStatement.params())
        .map(
            resultSet -> {
              return Optional.empty();
            });
  }
}