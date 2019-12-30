package com.example.realworld.infrastructure.web.route;

import com.example.realworld.infrastructure.vertx.proxy.ArticleOperations;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import org.springframework.stereotype.Component;

@Component
public class ArticlesRoute extends AbstractHttpRoute {

  private static final String FEED = "/feed";
  public static final String OFFSET = "offset";
  public static final String LIMIT = "limit";
  private ArticleOperations articleOperations;

  public ArticlesRoute(ArticleOperations articleOperations) {
    this.articleOperations = articleOperations;
  }

  @Override
  public Router configure(Vertx vertx) {

    String articlesPath = "/articles";

    final Router articlesRouter = Router.router(vertx);

    articlesRouter.route().handler(BodyHandler.create());

    articlesRouter
        .route(articlesPath + "/*")
        .handler(routingContext -> jwtHandler(routingContext, true));

    articlesRouter.get(articlesPath + FEED).handler(this::feed);

    return articlesRouter;
  }

  private void feed(RoutingContext routingContext) {
    userId(
        routingContext,
        false,
        (String userId) -> {
          MultiMap queryParams = routingContext.queryParams();
          int offset = getQueryParam(queryParams, OFFSET, 0);
          int limit = getQueryParam(queryParams, LIMIT, 20);
          articleOperations.findRecentArticles(
              userId,
              offset,
              limit,
              responseOrFail(routingContext, HttpResponseStatus.OK.code(), false));
        });
  }

  private int getQueryParam(MultiMap queryParams, String name, int defaultValue) {
    String queryParam = queryParams.get(name);
    return queryParam != null ? Integer.parseInt(queryParam) : defaultValue;
  }
}
