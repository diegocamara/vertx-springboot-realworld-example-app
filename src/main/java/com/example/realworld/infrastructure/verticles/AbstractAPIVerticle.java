package com.example.realworld.infrastructure.verticles;

import com.example.realworld.infrastructure.Constants;
import com.example.realworld.infrastructure.context.annotation.DefaultObjectMapper;
import com.example.realworld.infrastructure.context.annotation.WrapUnwrapRootValueObjectMapper;
import com.example.realworld.infrastructure.web.exception.RequestValidationException;
import com.example.realworld.infrastructure.web.exception.mapper.BusinessExceptionMapper;
import com.example.realworld.infrastructure.web.model.response.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.serviceproxy.ServiceException;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.Set;
import java.util.function.Function;

public class AbstractAPIVerticle extends AbstractVerticle {

  final Logger logger = LoggerFactory.getLogger(getClass());

  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String AUTHORIZATION_HEADER_PREFIX = "Token ";
  public static final String USER_ID_CONTEXT_KEY = "userId";

  @Inject @WrapUnwrapRootValueObjectMapper private ObjectMapper wrapUnwrapRootValueObjectMapper;
  @Inject @DefaultObjectMapper private ObjectMapper defaultObjectMapper;
  @Inject private Validator validator;
  @Inject private BusinessExceptionMapper businessExceptionMapper;
  @Inject protected JWTAuth jwtAuth;

  protected void createHttpServer(
      Handler<HttpServerRequest> httpServerRequestHandler,
      Handler<AsyncResult<HttpServer>> handler) {

    vertx
        .createHttpServer()
        .requestHandler(httpServerRequestHandler)
        .listen(config().getInteger(Constants.SERVER_PORT_KEY, 8080), handler);
  }

  protected Handler<AsyncResult<HttpServer>> createHttpServerHandler(
      String apiName, Promise<Void> startPromise) {
    return httpServerAsyncResult -> {
      if (httpServerAsyncResult.succeeded()) {
        logger.info(apiName + " started on port " + config().getInteger(Constants.SERVER_PORT_KEY));
        startPromise.complete();
      } else {
        startPromise.fail(httpServerAsyncResult.cause());
      }
    };
  }

  protected <T> T getBody(RoutingContext routingContext, Class<T> clazz) {
    T result;
    try {
      result = wrapUnwrapRootValueObjectMapper.readValue(routingContext.getBodyAsString(), clazz);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    return result;
  }

  protected <T> T getBodyAndValid(RoutingContext routingContext, Class<T> clazz) {
    T result = getBody(routingContext, clazz);
    validateRequestBody(result);
    return result;
  }

  private void handlerRequestValidation(
      HttpServerResponse httpServerResponse,
      RequestValidationException requestValidationException) {

    httpServerResponse
        .setStatusCode(HttpResponseStatus.UNPROCESSABLE_ENTITY.code())
        .end(writeValueAsString(requestValidationException.getErrorResponse()));
  }

  private <T> void validateRequestBody(T body) {

    Set<ConstraintViolation<T>> violations = validator.validate(body);

    if (!violations.isEmpty()) {

      ErrorResponse errorResponse = new ErrorResponse();
      violations.forEach(constraint -> errorResponse.getBody().add(constraint.getMessage()));

      throw new RequestValidationException(errorResponse);
    }
  }

  protected <T> void response(RoutingContext routingContext, int statusCode, T response) {
    try {
      routingContext
          .response()
          .setStatusCode(statusCode)
          .end(wrapUnwrapRootValueObjectMapper.writeValueAsString(response));
    } catch (JsonProcessingException e) {
      routingContext.fail(e);
    }
  }

  protected <T> Handler<AsyncResult<T>> responseOrFail(
      RoutingContext routingContext, int statusCode, Function<T, Object> responseFunction) {
    return asyncResult -> {
      if (asyncResult.succeeded()) {
        T result = asyncResult.result();
        response(routingContext, statusCode, responseFunction.apply(result));
      } else {
        routingContext.fail(asyncResult.cause());
      }
    };
  }

  protected Router subRouter(Router router) {
    final Router baseRouter = Router.router(vertx);
    configApiErrorHandler(baseRouter);
    String contextPath = config().getString(Constants.CONTEXT_PATH_KEY);
    return baseRouter.mountSubRouter(contextPath, router);
  }

  private void configApiErrorHandler(Router baseRouter) {
    baseRouter
        .route()
        .failureHandler(
            failureRoutingContext -> {
              HttpServerResponse response = failureRoutingContext.response();

              if (failureRoutingContext.failure() instanceof RequestValidationException) {

                handlerRequestValidation(
                    response, (RequestValidationException) failureRoutingContext.failure());

              } else if (failureRoutingContext.failure() instanceof ServiceException) {

                ServiceException serviceException =
                    (ServiceException) failureRoutingContext.failure();

                this.businessExceptionMapper.handle(serviceException, response);

              } else {

                response.end(
                    writeValueAsString(
                        new ErrorResponse(failureRoutingContext.failure().getMessage())));
              }
            });
  }

  protected String writeValueAsString(Object value) {
    String result;
    try {
      result = wrapUnwrapRootValueObjectMapper.writeValueAsString(value);
    } catch (JsonProcessingException ex) {
      throw new RuntimeException(ex);
    }
    return result;
  }

  protected void jwtHandler(RoutingContext routingContext) {

    String authorization = routingContext.request().headers().get(AUTHORIZATION_HEADER);

    if (authorization != null && authorization.contains(AUTHORIZATION_HEADER_PREFIX)) {

      String token = authorization.replace(AUTHORIZATION_HEADER_PREFIX, "");

      jwtAuth
          .rxAuthenticate(new JsonObject().put("jwt", token))
          .subscribe(
              user -> {
                routingContext.put(USER_ID_CONTEXT_KEY, user.principal().getLong("sub"));
                routingContext.next();
              },
              throwable -> unauthorizedResponse(routingContext));
    } else {
      unauthorizedResponse(routingContext);
    }
  }

  protected void unauthorizedResponse(RoutingContext routingContext) {
    response(
        routingContext, HttpResponseStatus.UNAUTHORIZED.code(), new ErrorResponse("Unauthorized"));
  }
}
