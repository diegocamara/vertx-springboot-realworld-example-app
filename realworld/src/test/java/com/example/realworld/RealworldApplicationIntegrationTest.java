package com.example.realworld;

import com.example.realworld.infrastructure.persistence.statement.UserStatements;
import com.example.realworld.infrastructure.vertx.configuration.VertxConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.web.client.WebClient;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.function.Supplier;

public class RealworldApplicationIntegrationTest {

  protected static ConfigurableApplicationContext configurableApplicationContext =
      getApplicationContext();
  protected static Vertx vertx = getVertx();
  protected static WebClient webClient = getWebClient(vertx);
  protected static JWTAuth jwtAuth = getJWTAuth();
  protected static JDBCClient jdbcClient = getJDBCClient();
  protected static ObjectMapper wrapUnwrapRootValueObjectMapper =
      getWrapUnwrapRootValueObjectMapper();
  protected static VertxConfiguration vertxConfiguration = getVertxConfiguration();
  protected static UserStatements userStatements = getUserStatements();

  private static ConfigurableApplicationContext getApplicationContext() {
    return getObject(
        configurableApplicationContext, () -> SpringApplication.run(RealworldApplication.class));
  }

  private static WebClient getWebClient(Vertx vertx) {
    return getObject(webClient, () -> WebClient.create(vertx));
  }

  private static Vertx getVertx() {
    return getObject(vertx, () -> getBean(Vertx.class));
  }

  private static JWTAuth getJWTAuth() {
    return getObject(jwtAuth, () -> getBean(JWTAuth.class));
  }

  private static JDBCClient getJDBCClient() {
    return getObject(jdbcClient, () -> getBean(JDBCClient.class));
  }

  private static ObjectMapper getWrapUnwrapRootValueObjectMapper() {
    return getObject(
        wrapUnwrapRootValueObjectMapper,
        () -> getBean("wrapUnwrapRootValueObjectMapper", ObjectMapper.class));
  }

  private static VertxConfiguration getVertxConfiguration() {
    return getObject(
        vertxConfiguration, () -> getBean("vertxConfiguration", VertxConfiguration.class));
  }

  private static UserStatements getUserStatements() {
    return configurableApplicationContext.getBean(UserStatements.class);
  }

  private static <T> T getBean(Class<T> clazz) {
    return configurableApplicationContext.getBean(clazz);
  }

  private static <T> T getBean(String name, Class<T> clazz) {
    return configurableApplicationContext.getBean(name, clazz);
  }

  private static <T> T getObject(T object, Supplier<T> supplier) {
    if (object != null) {
      return object;
    } else {
      return supplier.get();
    }
  }
}