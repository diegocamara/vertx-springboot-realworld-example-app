package com.example.realworld.infrastructure.configuration;

import com.example.realworld.application.ArticleServiceImpl;
import com.example.realworld.application.ProfileServiceImpl;
import com.example.realworld.application.TagServiceImpl;
import com.example.realworld.application.UserServiceImpl;
import com.example.realworld.domain.article.model.ArticleRepository;
import com.example.realworld.domain.article.model.CommentRepository;
import com.example.realworld.domain.article.model.FavoritesRepository;
import com.example.realworld.domain.article.model.SlugProvider;
import com.example.realworld.domain.article.service.ArticleService;
import com.example.realworld.domain.profile.model.UsersFollowedRepository;
import com.example.realworld.domain.profile.service.ProfileService;
import com.example.realworld.domain.tag.model.ArticlesTagsRepository;
import com.example.realworld.domain.tag.model.TagRepository;
import com.example.realworld.domain.tag.service.TagService;
import com.example.realworld.domain.user.model.HashProvider;
import com.example.realworld.domain.user.model.ModelValidator;
import com.example.realworld.domain.user.model.TokenProvider;
import com.example.realworld.domain.user.model.UserRepository;
import com.example.realworld.domain.user.service.UserService;
import com.example.realworld.infrastructure.vertx.configuration.VertxConfiguration;
import com.example.realworld.infrastructure.vertx.proxy.ArticleOperations;
import com.example.realworld.infrastructure.vertx.proxy.ProfileOperations;
import com.example.realworld.infrastructure.vertx.proxy.TagsOperations;
import com.example.realworld.infrastructure.vertx.proxy.UserOperations;
import com.example.realworld.infrastructure.vertx.proxy.impl.ArticleOperationsImpl;
import com.example.realworld.infrastructure.vertx.proxy.impl.ProfileOperationsImpl;
import com.example.realworld.infrastructure.vertx.proxy.impl.TagsOperationsImpl;
import com.example.realworld.infrastructure.vertx.proxy.impl.UserOperationsImpl;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.slugify.Slugify;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RealworldApplicationConfiguration {

  @Bean
  public UserService userService(
      UserRepository userRepository,
      UsersFollowedRepository usersFollowedRepository,
      HashProvider cryptographyService,
      TokenProvider tokenProvider,
      ModelValidator modelValidator) {
    return new UserServiceImpl(
        userRepository,
        usersFollowedRepository,
        cryptographyService,
        tokenProvider,
        modelValidator);
  }

  @Bean
  public ProfileService profileService(UserService userService) {
    return new ProfileServiceImpl(userService);
  }

  @Bean
  public ArticleService articleService(
      ArticleRepository articleRepository,
      UsersFollowedRepository usersFollowedRepository,
      FavoritesRepository favoritesRepository,
      CommentRepository commentRepository,
      SlugProvider slugProvider,
      ModelValidator modelValidator,
      ProfileService profileService,
      TagService tagService,
      UserService userService) {
    return new ArticleServiceImpl(
        articleRepository,
        usersFollowedRepository,
        favoritesRepository,
        commentRepository,
        slugProvider,
        modelValidator,
        profileService,
        tagService,
        userService);
  }

  @Bean
  public TagService tagService(
      TagRepository tagRepository,
      ArticlesTagsRepository articlesTagsRepository,
      ModelValidator modelValidator) {
    return new TagServiceImpl(tagRepository, articlesTagsRepository, modelValidator);
  }

  @Bean
  public Vertx vertx() {
    return Vertx.vertx();
  }

  @Bean
  public JDBCClient jdbcClient(Vertx vertx, VertxConfiguration vertxConfiguration) {
    VertxConfiguration.Database database = vertxConfiguration.getDatabase();
    JsonObject dataBaseConfig = new JsonObject();
    dataBaseConfig.put("url", database.getUrl());
    dataBaseConfig.put("driver_class", database.getDriverClass());
    dataBaseConfig.put("max_pool_size", database.getMaxPoolSize());
    dataBaseConfig.put("user", database.getUser());
    dataBaseConfig.put("password", database.getPassword());
    return JDBCClient.createShared(vertx, dataBaseConfig);
  }

  @Bean
  public JWTAuth jwtAuth(Vertx vertx, VertxConfiguration vertxConfiguration) {
    VertxConfiguration.Jwt jwt = vertxConfiguration.getJwt();
    PubSecKeyOptions pubSecKeyOptions = new PubSecKeyOptions();
    pubSecKeyOptions.setAlgorithm(jwt.getAlgorithm());
    pubSecKeyOptions.setPublicKey(jwt.getSecret());
    pubSecKeyOptions.setSymmetric(true);
    JWTAuthOptions jwtAuthOptions = new JWTAuthOptions();
    jwtAuthOptions.addPubSecKey(pubSecKeyOptions);
    return JWTAuth.create(vertx, jwtAuthOptions);
  }

  @Bean("wrapUnwrapRootValueObjectMapper")
  public ObjectMapper wrapUnwrapRootValueObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
    objectMapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  @Bean("defaultObjectMapper")
  public ObjectMapper defaultObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  @Bean
  public Slugify slugify() {
    return new Slugify();
  }

  @Bean
  public UserOperations userOperations(
      Vertx vertx,
      UserService userService,
      @Qualifier("defaultObjectMapper") ObjectMapper objectMapper) {
    return registerServiceAndCreateProxy(
        vertx,
        UserOperations.class,
        UserOperations.SERVICE_ADDRESS,
        new UserOperationsImpl(userService, objectMapper));
  }

  @Bean
  public ProfileOperations profileOperations(
      Vertx vertx,
      ProfileService profileService,
      @Qualifier("defaultObjectMapper") ObjectMapper objectMapper) {
    return registerServiceAndCreateProxy(
        vertx,
        ProfileOperations.class,
        ProfileOperations.SERVICE_ADDRESS,
        new ProfileOperationsImpl(profileService, objectMapper));
  }

  @Bean
  public ArticleOperations articleOperations(
      Vertx vertx,
      ArticleService articleService,
      @Qualifier("defaultObjectMapper") ObjectMapper objectMapper) {
    return registerServiceAndCreateProxy(
        vertx,
        ArticleOperations.class,
        ArticleOperations.SERVICE_ADDRESS,
        new ArticleOperationsImpl(articleService, objectMapper));
  }

  @Bean
  public TagsOperations tagsOperations(
      Vertx vertx,
      TagService tagService,
      @Qualifier("defaultObjectMapper") ObjectMapper objectMapper) {
    return registerServiceAndCreateProxy(
        vertx,
        TagsOperations.class,
        TagsOperations.SERVICE_ADDRESS,
        new TagsOperationsImpl(tagService, objectMapper));
  }

  private <T> T registerServiceAndCreateProxy(
      Vertx vertx, Class<T> clazz, String address, T instance) {
    registerProxy(vertx, clazz, address, instance);
    return createProxy(vertx, clazz, address);
  }

  private <T> void registerProxy(Vertx vertx, Class<T> clazz, String address, T instance) {
    new ServiceBinder(vertx.getDelegate()).setAddress(address).register(clazz, instance);
  }

  private <T> T createProxy(Vertx vertx, Class<T> clazz, String address) {
    return new ServiceProxyBuilder(vertx.getDelegate()).setAddress(address).build(clazz);
  }
}
