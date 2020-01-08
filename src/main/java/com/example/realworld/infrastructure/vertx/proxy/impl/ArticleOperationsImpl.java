package com.example.realworld.infrastructure.vertx.proxy.impl;

import com.example.realworld.domain.article.service.ArticleService;
import com.example.realworld.domain.profile.service.ProfileService;
import com.example.realworld.domain.tag.service.TagService;
import com.example.realworld.infrastructure.vertx.proxy.ArticleOperations;
import com.example.realworld.infrastructure.web.model.response.ArticleResponse;
import com.example.realworld.infrastructure.web.model.response.ArticlesResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class ArticleOperationsImpl extends AbstractOperations implements ArticleOperations {

  private ArticleService articleService;
  private ProfileService profileService;
  private TagService tagService;

  public ArticleOperationsImpl(
      ArticleService articleService,
      ProfileService profileService,
      TagService tagService,
      ObjectMapper objectMapper) {
    super(objectMapper);
    this.articleService = articleService;
    this.profileService = profileService;
    this.tagService = tagService;
  }

  @Override
  public void findRecentArticles(
      String currentUserId, int offset, int limit, Handler<AsyncResult<ArticlesResponse>> handler) {
    articleService
        .findRecentArticles(currentUserId, offset, limit)
        .flattenAsFlowable(articles -> articles)
        .map(ArticleResponse::new)
        .toList()
        .flatMap(
            articleResponses ->
                articleService
                    .totalUserArticlesFollowed(currentUserId)
                    .map(articlesCount -> new ArticlesResponse(articleResponses, articlesCount)))
        .subscribe(
            articlesResponse -> handler.handle(Future.succeededFuture(articlesResponse)),
            throwable -> handler.handle(error(throwable)));
  }
}
