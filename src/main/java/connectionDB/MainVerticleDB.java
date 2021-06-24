package connectionDB;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.SqlClient;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MainVerticleDB extends AbstractVerticle {

  private JsonObject json;


  public static JsonObject getOneOf(
    final String tableName
  ) {
    return new JsonObject()
      .put("const", "SELECT * FROM [" + tableName + "]")
      .put("title", tableName);
  }

  public static JsonObject getSchema(
    final JsonArray oneOf
  ) {

    return new JsonObject()
      .put("type", "object")
      .put("$schema", "https://json-schema.org/draft/2019-09/schema")
      .put("required", new JsonArray().add("query"))
      .put("additionalProperties", false)
      .put("properties", new JsonObject()
        .put("query", new JsonObject()
          .put("type", "string")
          .put("oneOf", oneOf)
        )
      );

  }


  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new MainVerticleDB());
  }


  @Override
  public void start(Promise<Void> startPromise) {
    System.out.println("init.....");

    this
      .dataConfig()
      .compose(this::getTables)
      .compose(this::getSchema)
      .compose(this::sendConnector);

  }

  @Override
  public void stop(Promise<Void> stopPromise) {

    this.execStop().onComplete(stopPromise);
  }

  private JDBCPool pool;

  public Future<JsonObject> getSchema(
    SQLRowStreamWrapper stream
  ) {

    final JsonArray array = new JsonArray();
    final Buffer buffer = Buffer.buffer();
    final String valor = null;
    return Future
      .future(promise -> {
        stream.exceptionHandler(promise::tryFail);
        stream.endHandler(v -> promise.tryComplete());
      //stream.handler(table -> array.add(getOneOf(table)));
        //stream.handler(table -> array.add(getOneOf(table)));
        //stream.handler(entries -> entries);
        // stream.handler(v -> v.toString());
      stream.handler(jsonObject1 -> System.out.println(jsonObject1.encodePrettily()));
      })

      .map(getSchema(array));

  }

  public Future<SQLRowStreamWrapper> getTables(
    @NotNull final JsonObject execution
  ) {

    System.out.println(execution.encodePrettily());
    this.pool = JDBCPool.pool(this.vertx, getConfig(execution));

    final String query = Optional
      .ofNullable(execution.getJsonObject("task"))
      .map(task -> task.getJsonObject("args"))
      .map(args -> args.getString("query"))
      .orElse(null);

    return this
      .pool
      .getConnection()
      .flatMap(connection -> connection
        .begin()
        .flatMap(tx -> {
          return connection
            .prepare(query)
            .map(ps -> ps.createStream(100))
            .map(SQLRowStreamWrapper::new);
        })
      );

  }

  // ===================================================

//  public @NotNull JDBCPool getDATA(
//    @NotNull final JsonObject execution
//  ) {
//
//    System.out.println(execution.encodePrettily());
//    this.pool = JDBCPool.pool(this.vertx, getConfig(execution));
//
//    final String query = Optional
//      .ofNullable(execution.getJsonObject("task"))
//      .map(task -> task.getJsonObject("args"))
//      .map(args -> args.getString("query"))
//      .orElse(null);
//
//    return this
//      .pool
//      .getConnection()
//      .onSuccess(sqlConnection -> {
//
//      })
//      .onFailure(throwable -> {
//
//      }).
////      .flatMap(connection -> connection
////        .begin()
////        .flatMap(tx -> {
////          return connection
////            .prepare(query)
////            .map(ps -> ps.createStream(100))
////            .map(SQLRowStreamWrapper::new);
////        })
//      );
//
//  }

  // ===================================================
  public @NotNull Future<Void> execStop() {
    return Optional
      .ofNullable(this.pool)
      .map(SqlClient::close)
      .orElseGet(Future::succeededFuture);
  }

  private @NotNull JsonObject getConfig(

    @NotNull final JsonObject execution
  ) {

    final JsonObject args = Optional
      .ofNullable(execution.getJsonObject("credential"))
      .map(credential -> credential.getJsonObject("args"))
      .orElseThrow();

    final String url = String.format(
      "jdbc:postgresql://%s:%d/%s",
      args.getString("host"),
      args.getInteger("port"),
      args.getString("database")
    );

    return new JsonObject()
      .put("url", url)
      .put("driver_class", "org.postgresql.Driver")
      .put("user", args.getString("user"))
      .put("password", args.getString("password"))
      .put("max_pool_size", 1)
      .put("initial_pool_size", 1)
      .put("min_pool_size", 1)
      .put("max_statements", 0)
      .put("max_statements_per_connection", 0)
      .put("max_idle_time", 0);
  }

  public Future<Void> sendConnector(JsonObject schema) {

    System.out.println("Voy a enviar un tipo de llamada (callType)");

//    final JsonObject json = new JsonObject()
//      .put("companyId", COMPANY_ID)
//      .put("title", TITLE)
//      .put("description", DESCRIPTION)
//      .put("schema", schema)
//      .put("layout", getLayout());
//
//    System.out.println(json.encodePrettily());




    System.out.println("He terminado");
    return Future.succeededFuture();
  }


  private Future<JsonObject> dataConfig() {

    return this
      .vertx
      .fileSystem()
      .readFile("dataDB.json")
      .map(Buffer::toJsonObject)
      .map(json -> new JsonObject()
        .put("task", new JsonObject()
          .put("args", new JsonObject()
            .put("query", "select * from prova") // insert into prova values ('MAX', 55) //select received_at, object from device_up  order by received_at  DESC limit 2// qui va la query
          )
        )
        .put("credential", new JsonObject()
          .put("args", new JsonObject()
            .put("host", json.getString("host"))
            .put("port", json.getInteger("port"))
            .put("user", json.getString("user"))
            .put("database", json.getString("database"))
            .put("password", json.getString("password"))
          )

        )
      );

  }

}




