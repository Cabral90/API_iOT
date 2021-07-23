package API;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgException;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;


import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

import static API.ValidateData.*;


public class IOT_API extends AbstractVerticle {
  private PgPool pool;
  private Router router;
  private static final UUID getUUID = UUID.randomUUID();
  private ValidateData validar = null;

  public static void main(String[] args) {

    Vertx.vertx().deployVerticle(new IOT_API());
  }

  /**
   *
   */
  @Override

  public void start() { // Promise<Void> startPromise

    validar = new ValidateData();


    System.out.println(" Init.....");
    clientDB()

      .compose(this::setUpInitialData);

    System.out.println(" Init.....");

  }


  /**
   * @param vd
   * @return
   */
  public Future<Void> setUpInitialData(Void vd) {
    System.out.println(" Init2 Router.....");

    RouterBuilder.create(vertx, "src/main/resources/openApi_v11_210721_.yaml")
      .onSuccess(routerBuilder -> {
        System.out.println(" Init3 call function .....");

        // call all endpoints
        // TODO: comprobar si hay sessione primero. caso contrario no se hara ningua operacion
   /*     this.haveSession()
          .compose(this::allFunction)
        .onFailure( " message error login");*/
        // TODO: antes añadir el tema de la seguridad // https://jwt.io/
        allFunction(routerBuilder);

        //Router router = Router.router(vertx)
        this.router = Router.router(vertx)
          .errorHandler(400, rc -> sendError(rc, 400, rc.failure().getMessage()));

        router.mountSubRouter("/v1", routerBuilder.createRouter());
        router.errorHandler(500, rc -> {
          rc.failure().printStackTrace();
          rc.end(rc.failure().getMessage());
        });

        vertx
          .createHttpServer()
          .requestHandler(router)
          .listen(8080);

        System.out.println("pasamos");
      })
      .onFailure(Throwable::printStackTrace);
    return Future.succeededFuture();

  }

  /**
   * @param router
   */
  private void allFunction(RouterBuilder router) {


    // Company

    router.operation("createCompany").handler(this::createCompany);
    router.operation("getAllCompany").handler(this::getAllCompany);
    router.operation("updateCompany").handler(this::updateCompany);
    router.operation("deleteCompany").handler(this::deleteCompany);
    router.operation("getCompanyById").handler(this::getCompanyById);
    router.operation("getAllAppByIdCompany").handler(this::getAllAppByIdCompany);
    router.operation("addAppToCompany").handler(this::addAppToCompany);
    router.operation("removeAppToCompany").handler(this::removeAppToCompany);


    // R Application
    router.operation("getAllApplication").handler(this::getAllApplication);
    router.operation("getApplicationById").handler(this::getApplicationById);
    router.operation("getAllDeviceByIdApplication").handler(this::getAllDeviceByIdApplication);

    // CRUD supervisor
    router.operation("createSupervisor").handler(this::createSupervisor);
    router.operation("getAllCompanySupervisor").handler(this::getAllCompanySupervisor);
    router.operation("getSupervisorById").handler(this::getSupervisorById);
    router.operation("updateSupervisor").handler(this::updateSupervisor);
    router.operation("deleteSupervisor").handler(this::deleteSupervisor);
    router.operation("addCompanyToSupervisor").handler(this::addCompanyToSupervisor);
    router.operation("removeCompanyToSupervisor").handler(this::removeCompanyToSupervisor);

    // CRUD User
    router.operation("createUser").handler(this::createUser);
    router.operation("updateUser").handler(this::updateUser);
    router.operation("deleteUser").handler(this::deleteUser);
    router.operation("getAllUser").handler(this::getAllUser);
    router.operation("getUserById").handler(this::getUserById);
    router.operation("getAllDeviceByIdUser").handler(this::getAllDeviceByIdUser);
    router.operation("addDeviceToUser").handler(this::addDeviceToUser);
    router.operation("removeDeviceToUser").handler(this::removeDeviceToUser);
    router.operation("getDashboardByIdUser").handler(this::getDashboardByIdUser);

    // pass User forget
    router.operation("updatePassword").handler(this::updatePassword);
    router.operation("setPassword").handler(this::setPassword);

    // Session
    router.operation("statusSession").handler(this::statusSession);
    router.operation("login").handler(this::login);
    router.operation("logout").handler(this::logout);

    // CRUD Device
    router.operation("updateDevice").handler(this::updateDevice);
    router.operation("deleteDevice").handler(this::deleteDevice);
    router.operation("getAllDevice").handler(this::getAllDevice);
    router.operation("getDeviceById").handler(this::getDeviceById);
    router.operation("exportFileDevice").handler(this::exportFileDevice);
    router.operation("getDeviceByIdGraphic").handler(this::getDeviceByIdGraphic);

    // Notifications
    router.operation("createNotification").handler(this::createNotification);
    router.operation("getAllNotifications").handler(this::getAllNotifications);
    router.operation("deleteNotification").handler(this::deleteNotification);
    router.operation("updateNotification").handler(this::updateNotification);
    router.operation("getNotificationById").handler(this::getNotificationById);
    router.operation("getExportNotifications").handler(this::getExportNotifications);

    // Events
    router.operation("getAllEvents").handler(this::getAllEvents);
    router.operation("getEventById").handler(this::getEventById);
    router.operation("exportDataEvent").handler(this::exportDataEvent);

    // Incidence
    router.operation("getAllIncidence").handler(this::getAllIncidence);
    router.operation("deleteIncidence").handler(this::deleteIncidence);
    router.operation("getIncidenceById").handler(this::getIncidenceById);
    router.operation("exportDataIncidence").handler(this::exportDataIncidence);


  }


  // ====== ============= ENTITIES =======================


  private void createCompany(RoutingContext routingContext) {

    JsonObject company = routingContext.getBodyAsJson();

    String createCompany = " INSERT INTO app_chirpstack_user.company " +
      "(owner_id, name ) VALUES " +
      "( '" + company.getString("adminId") + "', " +
      "'" + company.getString("name") + "' " +
      " ) RETURNING id, created_at ";

    System.out.println("company: " + createCompany);

    pool
      .getConnection()
      .compose(conn ->
        conn
          .query(createCompany)
          .execute()
          .compose(result -> {
            System.out.println("count: " + result.rowCount());

            Row row = result.iterator().next();
            JsonObject insert = new JsonObject()
              .put("id", row.getUUID("id"))
              .put("createdAt", row.getOffsetDateTime("created_at").toInstant());

            return Future.succeededFuture(insert);
          })
          .recover(err -> {
            //errorSqlEx(err);
            return Future.failedFuture(errorSqlEx(err).toString()); // todo: mejorar el mensaje de error
          })

          .onSuccess(result -> {
            routingContext.response().putHeader("content-type", "application/json").end(result.encode());
            System.out.println(result.encodePrettily());
          })
          .onFailure(err -> {
            routingContext.response().end(err.getMessage());
            System.out.println(err.getMessage());
            conn.close();
          }));

  }

  private void getAllCompany(RoutingContext routingContext) {
    ValidateData validar = new ValidateData();

    String count = " SELECT COUNT(*) from app_chirpstack_user.company ";

    String sql = sqlFilterCompany(routingContext);


    System.out.println(sql);

    pool
      .getConnection()
      .flatMap(conn -> conn
        .begin()
        .flatMap(tx -> {
          return
            conn
              .query(count)
              .execute()
              .flatMap(size -> {
                int countApp = size.iterator().next().getInteger(0);
                return Future.succeededFuture(countApp);

              })


              .compose(res -> conn
                .query(sql)
                .execute()
                .flatMap(rows -> {

                  if (rows.rowCount() == 0) {
                    return Future.failedFuture("404");
                  }

                  JsonArray items = new JsonArray();
                  JsonObject result = new JsonObject();
                  for (Row row : rows) {

                    items.add(
                      //getCompany(null, null));

                      new JsonObject()

                        .put("companyId", row.getUUID("company_id"))
                        .put("supervisorId", row.getUUID("supervisor_id"))
                        .put("adminId", row.getUUID("admin_id"))
                        .put("name", row.getString("name"))
                        .put("totalApplication", 0) // TODO: ver como asignar el valor total de app // validar.getTotalApp(pool, row.getUUID("company_id").toString())

                      // validar.getListApp(pool, row.getUUID("company_id").toString()
                    );
                    result.put("size", res).put("items", items);

                  }
                  return Future.succeededFuture(result);
                })
                .eventually(v -> conn.close()));
        })

        .recover(err -> {
          //errorSqlEx(err);
          return Future.failedFuture(errorSqlEx(err).toString()); // todo: mejorar el mensaje de error
        })

        .onSuccess(result -> {
          routingContext.response().putHeader("content-type", "application/json").end(result.encode());
          System.out.println(result.encodePrettily());
        })
        .onFailure(err -> {
          routingContext.response().putHeader("content-type", "application/json").end(err.getMessage());
          System.out.println(err.getMessage());
        })

      );


  }

  private void updateCompany(RoutingContext routingContext) { // OK
    JsonObject company = routingContext.getBodyAsJson();

    String id = routingContext.request().getParam("companyId");

    String sql = " UPDATE app_chirpstack_user.company SET  owner_id = '" + company.getString("adminId") + "', " +
      " updated_at = default, name = '" + company.getString("name") + "' WHERE id = '" + id + "' ";

    System.out.println(sql);

    pool
      .getConnection()
      .flatMap(conn -> //{
        conn
          .begin()
          .compose(tx -> {
            return
              conn
                .query(sql)
                .execute()
                .compose(size -> {

                  if (size.rowCount() != 1) {
                    return Future.failedFuture("404");
                  }
                  tx.commit();
                  return Future.succeededFuture();
                })
                .eventually(v -> conn.close());
          })
          .recover(this::errorSqlEx)
          .onSuccess(app -> {
            routingContext.response().setStatusCode(204).end();
          })
          .onFailure(err -> {
            routingContext.response().end(err.getMessage());
            System.out.println(err.getMessage());

          })
      );
  }

  private void deleteCompany(RoutingContext routingContext) { // OK

    String id = routingContext.request().getParam("companyId");

    String sql = " DELETE FROM app_chirpstack_user.company WHERE id = '" + id + "'";


    System.out.println(sql);

    pool
      .getConnection()
      .flatMap(conn -> conn
        .begin()
        .compose(tx -> {
          return
            conn
              .query(sql)
              .execute()
              .compose(size -> {
                if (size.rowCount() != 1) {
                  return Future.failedFuture("404");
                }
                tx.commit();
                return Future.succeededFuture();

              })
              .eventually(v -> conn.close());
        })
        .recover(this::errorSqlEx)
        .onSuccess(app -> {
          routingContext.response().setStatusCode(204).end();
        })
        .onFailure(err -> {
          routingContext.response().end(err.getMessage());
          System.out.println(err.getMessage());
        })
      );

  }

  private void getCompanyById(RoutingContext routingContext) {
    String id = routingContext.request().getParam("companyId");

    String sql =
      "SELECT t1.id AS company_id, t1.owner_id AS admin_id, t1.name, t2.id AS supervisor_id " +
        "FROM app_chirpstack_user.company t1 INNER JOIN app_chirpstack_user.admin_company tX " +
        "ON t1.id = tX.company_id " +
        "INNER JOIN app_chirpstack_user.admin t2 ON tX.admin_id = t2.id  " +
        "WHERE t1.id = '" + id + "' ";

    System.out.println("SQL: " + sql);

    pool
      .getConnection()
      .compose(res ->
        res.query(sql)
          .execute()
          .compose(result -> {
            System.out.println("count: " + result.rowCount());
            JsonObject company = new JsonObject();
            if (result.rowCount() != 1) {
              return Future.failedFuture("404");
            }
            Row row = result.iterator().next();
            company

              .put("companyId", row.getUUID("company_id"))
              .put("supervisorId", row.getUUID("supervisor_id"))
              .put("adminId", row.getUUID("admin_id"))
              .put("name", row.getString("name"))
              .put("totalApplication", 0); // TODO: ver como asignar el valor total de app // validar.getTotalApp(pool, row.getUUID("company_id").toString())

            return Future.succeededFuture(company); // TODO: assignar el objecto correspondiente
          })
          .recover(err -> {
            errorSqlEx(err); // control exception
            return Future.failedFuture(err);
          })
          .onSuccess(result -> {
            routingContext
              .response()
              .setStatusCode(200)
              .putHeader("content-type", "application/json")
              .end(result.encode());
            System.out.println(result.encodePrettily());
          }))
      .onFailure(err -> { // Todo:  errores code de BD
        System.out.println(err.getMessage()); // TODO: error NULO QUE PUEDO HACER
        routingContext.response().end(err.getMessage());
      });

  }

  private void getAllAppByIdCompany(RoutingContext routingContext) {

    String id = routingContext.request().getParam("companyId");

    String sql = sqlFilterAppToCompany(routingContext);


    System.out.println("SQL: " + sql);

    String count = " SELECT COUNT(t1.id) FROM app_chirpstack_user.app t1 " +
      "INNER JOIN app_chirpstack_user.company_app tb on t1.id = tb.app_id " +
      "WHERE t1.id = '" + id + "'";

    pool
      .getConnection()
      .flatMap(conn -> conn
        .begin()
        .flatMap(tx -> {
          return
            conn
              .query(count)
              .execute()
              .flatMap(size -> {
                int countApp = size.iterator().next().getInteger(0);
                return Future.succeededFuture(countApp);

              })

              .compose(res -> conn
                .query(sql)
                .execute()
                .flatMap(rows -> {

                  if (rows.rowCount() == 0) {
                    return Future.failedFuture("404");
                  }

                  JsonArray items = new JsonArray();
                  JsonObject result = new JsonObject();
                  for (Row row : rows) {

                    items.add(
                      row.toJson()); // map interno por alias

                    result.put("size", res).put("items", items);

                  }
                  return Future.succeededFuture(result);
                })
                .eventually(v -> conn.close()));
        })

        .recover(err -> {
          //errorSqlEx(err);
          return Future.failedFuture(errorSqlEx(err).toString()); // todo: mejorar el mensaje de error
        })

        .onSuccess(result -> {
          routingContext.response().putHeader("content-type", "application/json").end(result.encode());
          System.out.println(result.encodePrettily());
        })
        .onFailure(err -> {
          routingContext.response().putHeader("content-type", "application/json").end(err.getMessage());
          System.out.println(err.getMessage());
        })

      );


  }

  private void addAppToCompany(RoutingContext routingContext) { // Todo : is OK

    String companyId = routingContext.request().getParam("companyId");
    String appId = routingContext.request().getParam("applicationId");

    String sql = "INSERT INTO app_chirpstack_user.company_app (app_id, company_id) VALUES ( '" + appId + "', '" + companyId + "' )";

    System.out.println("SQL: " + sql);

    pool
      .getConnection()
      .flatMap(conn -> //{
        conn
          .begin()
          .compose(tx -> {
            return
              conn
                .query(sql)
                .execute()
                .compose(size -> {

                  if (size.rowCount() != 1) {
                    return Future.failedFuture("404");
                  }
                  tx.commit();
                  return Future.succeededFuture();
                })
                .eventually(v -> conn.close());
          })
          .recover(this::errorSqlEx)
          .onSuccess(app -> {
            routingContext.response().setStatusCode(204).end();
          })
          .onFailure(err -> {
            routingContext.response().end(err.getMessage());
            System.out.println(err.getMessage());

          })
      );

  }

  private void removeAppToCompany(RoutingContext routingContext) {

    String companyId = routingContext.request().getParam("companyId");
    String appId = routingContext.request().getParam("applicationId");

    String sql = "DELETE FROM app_chirpstack_user.company_app WHERE  app_id = '" + appId + "' AND company_id = '" + companyId + "' ";

    System.out.println("SQL: " + sql);

    pool
      .getConnection()
      .flatMap(conn -> //{
        conn
          .begin()
          .compose(tx -> {
            return
              conn
                .query(sql)
                .execute()
                .compose(size -> {

                  if (size.rowCount() != 1) {
                    return Future.failedFuture("404");
                  }
                  tx.commit();
                  return Future.succeededFuture();
                })
                .eventually(v -> conn.close());
          })
          .recover(this::errorSqlEx)
          .onSuccess(app -> {
            routingContext.response().setStatusCode(204).end();
          })
          .onFailure(err -> {
            routingContext.response().end(err.getMessage());
            System.out.println(err.getMessage());

          })
      );
  }

  // -- App



  private void getAllApplication(RoutingContext routingContext) {

    // list param
    //String sql = sqlFilterApp(routingContext);

    String sql = "SELECT id, name from app_chirpstack_user.app";

    String count = " SELECT COUNT(*) from app_chirpstack_user.app ";
    System.out.println("QUERY: " + sql);

    pool
      .getConnection()
      .flatMap(conn -> conn
        .begin()
        .flatMap(tx -> {
          return
            conn
              .query(count)
              .execute()
              .flatMap(size -> {
                int countApp = size.iterator().next().getInteger(0);
                return Future.succeededFuture(countApp);

              })
              .compose(res -> conn
                .query(sql)
                .execute()
                .flatMap(rows -> {

                    JsonArray items = new JsonArray();
                    JsonObject result = new JsonObject();
                    for (Row row : rows) {
                      items.add(new JsonObject()
                        .put("id", row.getUUID("id"))
                        .put("name", row.getString("name")));

                      result.put("size", res).put("items", items);
                      System.out.println(items.encodePrettily());
                    }
                    return Future.succeededFuture(result);
                  }
                )
                .eventually(v -> conn.close()));
        }).onSuccess(app -> {
          routingContext
            .response()
            .putHeader("content-type", "application/json")
            .end(app.encode());
        })
        .onFailure(err -> {
          routingContext
            .response()
            .end(err.getMessage());
          System.out.println(err.getMessage());
        })
      );
  }

  private void getApplicationById(RoutingContext routingContext) { // TODO: rever los error de nulos

    String id = routingContext.request().getParam("applicationId");
    String sql = " SELECT id, name from app_chirpstack_user.app WHERE id = '" + id + "'";

    System.out.println("SQL: " + sql);

    pool
      .getConnection()
      .compose(res ->
        res.query(sql)
          .execute()
          .compose(result -> {
            System.out.println("count: " + result.rowCount());
            JsonObject app = new JsonObject();
            // if (result.rowCount() == 1) {
            Row row = result.iterator().next();
            app
              .put("id", row.getUUID("id"))
              .put("name", row.getString("name"));

            return Future.succeededFuture(app);
          })
          .recover(err -> {
            //errorSqlEx(err);
            return Future.failedFuture(errorSqlEx(err).toString()); // todo: mejorar el mensaje de error
          })

          .onSuccess(result -> {
            routingContext
              .response()
              .setStatusCode(200)
              .putHeader("content-type", "application/json")
              .end(result.encode());
            System.out.println(result.encodePrettily());
          }))
      .onFailure(err -> { // Todo:  errores code de BD
        System.out.println(err.getMessage()); // TODO: error NULO QUE PUEDO HACER
        routingContext.response().end(err.getMessage());
      });
  }


  private void getAllDeviceByIdApplication(RoutingContext routingContext) {

    String id = routingContext.request().getParam("applicationId");

    String count =

    "SELECT COUNT(*) "+
      "FROM app_chirpstack_user.device t1 "+
      "INNER JOIN app_chirpstack_user.app t2 ON t1.app_id = t2.id " +
      " WHERE t2.id = '" + id + "'";

    String  sql = sqlFilterApp(routingContext);

    // String sql = " SELECT * FROM app_chirpstack_user.app WHERE id = '" + id + "'";

    System.out.println("SQL: " + sql);
    System.out.println("count: " + count);
    //routingContext.response().end("ya esta.");
    pool
      .getConnection()
      .flatMap(conn -> conn
        .begin()
        .flatMap(tx -> {
          return
            conn
              .query(count)
              .execute()
              .flatMap(size -> {
                int countApp = size.iterator().next().getInteger(0);
                return Future.succeededFuture(countApp);

              })
              .compose(res -> conn
                .query(sql)
                .execute()
                .flatMap(rows -> {

                    JsonArray items = new JsonArray();
                    JsonObject result = new JsonObject();
                    for (Row row : rows) {
                      items.add(
                        row.toJson()); // map interno

                      result.put("size", res).put("items", items);
                      System.out.println(items.encodePrettily());
                    }
                    return Future.succeededFuture(result);
                  }
                )
                .eventually(v -> conn.close()));
        }).onSuccess(app -> {
          routingContext
            .response()
            .putHeader("content-type", "application/json")
            .end(app.encode());
        })
        .onFailure(err -> {
          routingContext
            .response()
            .end(err.getMessage());
          System.out.println(err.getMessage());
        })
      );

  }

  // CRUD Supervisor

  private void createSupervisor(RoutingContext routingContext) {
  }

  private void getAllCompanySupervisor(RoutingContext routingContext) {
  }

  private void getSupervisorById(RoutingContext routingContext) {
  }

  private void updateSupervisor(RoutingContext routingContext) {
  }

  private void deleteSupervisor(RoutingContext routingContext) {
  }

  private void addCompanyToSupervisor(RoutingContext routingContext) {
  }

  private void removeCompanyToSupervisor(RoutingContext routingContext) {
  }
  // CRUD User


  // User
  private void createUser(RoutingContext routingContext) {
  }

  private void updateUser(RoutingContext routingContext) {
  }

  private void deleteUser(RoutingContext routingContext) {
  }

  private void getAllUser(RoutingContext routingContext) {
  }

  private void getUserById(RoutingContext routingContext) {
  }

  private void getAllDeviceByIdUser(RoutingContext routingContext) {
  }

  private void addDeviceToUser(RoutingContext routingContext) {
  }

  private void removeDeviceToUser(RoutingContext routingContext) {

  }

  private void getDashboardByIdUser(RoutingContext routingContext) {
  }


  // password

  private void updatePassword(RoutingContext routingContext) {
  }

  private void setPassword(RoutingContext routingContext) {

  }


  // Session
  private void statusSession(RoutingContext routingContext) {
  }

  private void login(RoutingContext routingContext) {
  }

  private void logout(RoutingContext routingContext) {
  }

  // CRUD Device

  private void updateDevice(RoutingContext routingContext) {
  }

  private void deleteDevice(RoutingContext routingContext) {
  }

  private void getAllDevice(RoutingContext routingContext) {
  }

  private void getDeviceById(RoutingContext routingContext) {
  }

  private void exportFileDevice(RoutingContext routingContext) {
  }

  private void getDeviceByIdGraphic(RoutingContext routingContext) {

  }

  // Notifications
  private void createNotification(RoutingContext routingContext) {
  }

  private void getAllNotifications(RoutingContext routingContext) {
  }

  private void deleteNotification(RoutingContext routingContext) {
  }

  private void updateNotification(RoutingContext routingContext) {
  }

  private void getNotificationById(RoutingContext routingContext) {
  }

  private void getExportNotifications(RoutingContext routingContext) {
  }

  // Events
  private void getAllEvents(RoutingContext routingContext) {
  }

  private void getEventById(RoutingContext routingContext) {
  }

  private void exportDataEvent(RoutingContext routingContext) {
  }

  // Incidence
  private void getAllIncidence(RoutingContext routingContext) {
  }

  private void deleteIncidence(RoutingContext routingContext) {
  }

  private void getIncidenceById(RoutingContext routingContext) {
  }

  private void exportDataIncidence(RoutingContext routingContext) {
  }


  public Future<Void> clientDB(

  ) {

    PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(5432)
      .setHost("54.220.101.114")
      .setDatabase("chirpstack")
      .setUser("chirpstack_user")
      .setPassword("user3344");

// Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

// Create the client pool
    this.pool = PgPool.pool(vertx, connectOptions, poolOptions);
    //return this.pool;

    return Future.succeededFuture();
  }


  public static void sendError(
    final RoutingContext rc,
    final int code,
    final String cause) {

    final String message = HttpResponseStatus.valueOf(code).reasonPhrase();

    final JsonObject json = new JsonObject()
      .put("status", code)
      .put("title", message);

    if (cause != null && !cause.startsWith("ValidationException")) {
      json.put("cause", cause);
    }

    rc.response()
      .setStatusCode(code)
      .setStatusMessage(message)
      .putHeader("Content-Type", "application/json")
      .end(json.toBuffer());

  }

  private static String characterAt(String str) {
    if (str != null && str.length() > 0 && str.charAt(str.length() - 1) == 'x') {
      str = str.substring(0, str.length() - 1);
    }
    return str;
  }


  public Future<Object> errorSqlEx(Throwable error) {

    if (!error.getMessage().equals("404")) {
      PgException x = (PgException) error;

      if (x.getCode() != null) {
        return Future.failedFuture(sendErrorSql(x.getCode()).encode());
      }
    } else {
      return Future.failedFuture("not found!");
    }
    return Future.succeededFuture();
  }

  public JsonObject sendErrorSql(
    String code

  ) {
    String mensaje = null;
    if (code.equals("23505")) {
      mensaje = "Existing record!";
    } else {
      mensaje = "Internal error";
    }

    final JsonObject json = new JsonObject()
      .put(code, mensaje);

    return json;
  }

  public Future<Boolean> haveRegistre(PgPool pool, String id, String colum, String entity) {

    String find = "SELECT * FROM  " + entity + " WHERE " + colum + " = '" + id + "' ";
    System.out.println("queri METODO: " + find);

    pool
      .query(find)
      .execute()
      .compose(res -> {
        if (!res.iterator().hasNext()) {
          System.out.println("Estado: " + !res.iterator().hasNext());
          return Future.succeededFuture(true);

        }
        return Future.succeededFuture(false);
      });
    return Future.succeededFuture();
  }

}
