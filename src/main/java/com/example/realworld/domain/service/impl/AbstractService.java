package com.example.realworld.domain.service.impl;

import com.example.realworld.domain.service.error.Error;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.sql.ResultSet;
import io.vertx.serviceproxy.ServiceException;

import java.util.function.Consumer;

class AbstractService {

  ObjectMapper objectMapper;

  AbstractService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  protected <T> Handler<AsyncResult<T>> result(
      Consumer<T> consumer, Handler<AsyncResult<?>> handler) {
    return (AsyncResult<T> asyncResult) -> {
      if (asyncResult.succeeded()) {
        consumer.accept(asyncResult.result());
      } else {
        handler.handle(Future.failedFuture(asyncResult.cause()));
      }
    };
  }

  <T> AsyncResult<T> error(Throwable throwable) {
    String error;
    try {
      error =
          objectMapper.writeValueAsString(new Error<>(throwable.getClass().getName(), throwable));
    } catch (JsonProcessingException ex) {
      error = ex.getMessage();
    }
    return ServiceException.fail(1, error);
  }

  protected boolean isCountResultGreaterThanZero(ResultSet resultSet) {
    return resultSet.getRows().get(0).getLong("COUNT(*)") > 0;
  }
}
