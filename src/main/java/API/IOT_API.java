package API;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgException;
import io.vertx.pgclient.PgPool;
import io.vertx.pgclient.data.Path;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;


import java.io.File;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.*;

import static API.MapObject.session;
import static API.QueryDB.*;


public class IOT_API extends AbstractVerticle {
  private PgPool pool;
  private Router router;
  private static String NOT_FOUND = "404";
  private static String OK = "200";
  private static final UUID getUUID = UUID.randomUUID();

  final private static String API_FILE = "openApi_v14_260721_.yaml";

  Path url = null;

  public static void main(String[] args) {

    Vertx.vertx().deployVerticle(new IOT_API());
  }

  /**
   *
   */
  @Override

  public void start() {
    System.out.println(" Start Verticle: Ok");
    clientDB()
      .compose(this::setUpInitialData);

  }


  /**
   * @param vd
   * @return
   */
  public Future<Void> setUpInitialData(Void vd) {

   /* URL url = getClass().getResource(API_FILE);
    //assert url != null;
    File file = new File(url.getPath());*/

    RouterBuilder.create(vertx, "src/main/resources/openapi.yaml")//"openapi.yaml")
      .onSuccess(routerBuilder -> {
        System.out.println(" Call  all endpoint: Ok");

        JWTAuthOptions jwt = new JWTAuthOptions()
          .addPubSecKey(new PubSecKeyOptions()
            .setAlgorithm("HS256")
            .setBuffer("superKey"));

        JWTAuth provider = JWTAuth.create(vertx, jwt);
        JWTAuthHandler handler = JWTAuthHandler
          .create(provider);

        routerBuilder
          .securityHandler("bearerAuth", handler);
        allFunction(routerBuilder);

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
          .listen(8088);

        System.out.println("Listening .... ");
      })
      .onFailure(Throwable::printStackTrace);
    return Future.succeededFuture();

  }


  private void allFunction(RouterBuilder router) {

    // Company

    router.operation("createCompany").handler(this::createCompany);
    router.operation("getAllCompany").handler(this::getAllCompany);
    router.operation("updateCompany").handler(this::updateCompany);
    router.operation("deleteCompany").handler(this::deleteCompany);
    router.operation("getCompanyById").handler(this::getCompanyById);
    router.operation("getAllAppByIdCompany").handler(this::getAllAppByIdCompany);
    router.operation("getAllDeviceByIdCompany").handler(this::getAllDeviceByIdCompany);
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
    router.operation("getCompanyHaveSupervisorById").handler(this::getCompanyHaveSupervisorById);
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
    router.operation("updateDeviceToUser").handler(this::updateDeviceToUser);

    // pass User forget
    router.operation("updatePassword").handler(this::updatePassword);
    router.operation("setPassword").handler(this::setPassword);

    // dashboard
    router.operation("getDashboardById").handler(this::getDashboardById);


    // Session
    router.operation("statusSession").handler(this::statusSession);
    router.operation("login").handler(this::login);
    router.operation("updateSession").handler(this::updateSession);
    router.operation("logout").handler(this::logout);

    // CRUD Device
    router.operation("getAllDevice").handler(this::getAllDevice);
    router.operation("getDeviceById").handler(this::getDeviceById);
    router.operation("exportFileDevice").handler(this::exportFileDevice);
    router.operation("getDeviceByIdGraphic").handler(this::getDeviceByIdGraphic); //
    router.operation("getExportDeviceByIdGraphic").handler(this::getExportDeviceByIdGraphic);

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
      "( '" + UUID.randomUUID() + "', " +
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

    String sql = sqlFilterCompany(routingContext);
    System.out.println(sql);

    System.out.println("......");


   /* this
      .haveSession(routingContext)
      .compose(session -> {
        System.out.println("session DD: " + session);
        if (!session.equals(NOT_FOUND)) {
          countRows("app_chirpstack_user.company")
            .compose(size -> {
              System.out.println("Error: " + NOT_FOUND + " size: " + size);
              return pool
                .query(sql)
                .execute()
                .compose(rows -> {
                  JsonArray items = new JsonArray();
                  JsonObject result = new JsonObject();
                  for (Row row : rows) {
                    items.add(new JsonObject()

                      .put("companyId", row.getUUID("company_id"))
                      .put("supervisorId", row.getUUID("supervisor_id"))
                      .put("adminId", row.getUUID("admin_id"))
                      .put("name", row.getString("name"))
                      .put("totalApplication", row.getInteger("total_app")));
                  }
                  result.put("size", size).put("items", items);
                  System.out.println("Res: " + size);
                  return Future.succeededFuture(result);
                })
                .recover(err -> {
                  return Future.failedFuture(err.getMessage());
                })
                .onSuccess(company -> {
                  routingContext.response().putHeader("content-type", "application/json").end(company.encode());
                  System.out.println(company.encodePrettily());
                })
                .onFailure(err -> {
                  System.out.println(err.getMessage());
                });

            });
        }
        return Future.succeededFuture();
      }).onFailure(err -> {
      routingContext.response().putHeader("content-type", "application/json").end("Not have conetion.");
    });*/



    pool
      .getConnection()
      .flatMap(conn -> conn
        .begin()
        .flatMap(tx -> {
          return
            conn
              .query("SELECT COUNT(*) from app_chirpstack_user.company")
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
                    return Future.failedFuture(NOT_FOUND);
                  }

                  JsonArray items = new JsonArray();
                  JsonObject result = new JsonObject();
                  for (Row row : rows) {

                    items.add(
                      new JsonObject()
                        .put("companyId", row.getUUID("company_id"))
                        .put("supervisorId", row.getUUID("supervisor_id"))
                        .put("adminId", row.getUUID("admin_id"))
                        .put("name", row.getString("name"))
                        .put("totalApplication", row.getInteger("total_app")));
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


  private void updateCompany(RoutingContext routingContext) {

    String sql = sqlUpdateCompany(routingContext);

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
                    return Future.failedFuture(NOT_FOUND);
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

  private void deleteCompany(RoutingContext routingContext) {

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
                  return Future.failedFuture(NOT_FOUND);
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

    String sql = sqlGetCompanyById(routingContext);
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
              return Future.failedFuture(NOT_FOUND);
            }
            Row row = result.iterator().next();
            company
              .put("companyId", row.getUUID("company_id"))
              .put("supervisorId", row.getUUID("supervisor_id"))
              .put("adminId", row.getUUID("admin_id"))
              .put("name", row.getString("name"))
              .put("totalApplication", row.getInteger("total_app"));

            return Future.succeededFuture(company);
          })
          .recover(err -> {
            errorSqlEx(err);
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
      .onFailure(err -> {
        System.out.println(err.getMessage());
        routingContext.response().end(err.getMessage());
      });

  }

  private void getAllAppByIdCompany(RoutingContext routingContext) {

    String sql = sqlFilterAppToCompany(routingContext);
    String count = sqlCountAppByIdCompany(routingContext);

    System.out.println("SQL: " + sql);

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
                    return Future.failedFuture(NOT_FOUND);
                  }

                  JsonArray items = new JsonArray();
                  JsonObject result = new JsonObject();
                  for (Row row : rows) {

                    items.add(
                      // row.toJson()); // map interno por alias
                      new JsonObject()
                        .put("id", row.getUUID("id"))
                        .put("createdAt", row.getOffsetDateTime("created_at").toInstant())
                        .put("updatedAt", row.getOffsetDateTime("updated_at").toInstant())
                        .put("name", row.getString("name")));

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

  private void getAllDeviceByIdCompany(RoutingContext routingContext) {
    String sql = sqlFilterDeviceCompany(routingContext);
    String count = getSqlCountDeviceCompany(routingContext);

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
                    return Future.failedFuture(NOT_FOUND);
                  }

                  JsonArray items = new JsonArray();
                  JsonObject result = new JsonObject();
                  for (Row row : rows) {
                    System.out.println("OBJEC: " + row.toJson().encodePrettily());
                    items.add(
                      new JsonObject()
                        .put("id", row.getUUID("id"))
                        .put("applicationId", row.getUUID("app_id"))
                        .put("applicationName", row.getString("app_name"))
                        .put("name", row.getString("device_name"))
                        .put("serial", row.getString("serial"))
                    );

                    result.put("size", res)
                      .put("items", items);

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


  private void addAppToCompany(RoutingContext routingContext) {
    String sql = sqlAddAppToCompany(routingContext);
    System.out.println("SQL: " + sql);

    pool
      .getConnection()
      .flatMap(conn ->
        conn
          .begin()
          .compose(tx -> {
            return
              conn
                .query(sql)
                .execute()
                .compose(size -> {

                  if (size.rowCount() != 1) {
                    return Future.failedFuture(NOT_FOUND);
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

    String sql = sqlRemoveAppToCompany(routingContext);

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
                    return Future.failedFuture(NOT_FOUND);
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

    String sql = "SELECT id, name from app_chirpstack_user.app";
    String count = " SELECT COUNT(*) from app_chirpstack_user.app ";

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
            return Future.failedFuture(errorSqlEx(err).toString());
          })

          .onSuccess(result -> {
            routingContext
              .response()
              .setStatusCode(200)
              .putHeader("content-type", "application/json")
              .end(result.encode());
            System.out.println(result.encodePrettily());
          }))
      .onFailure(err -> {
        System.out.println(err.getMessage());
        routingContext.response().end(err.getMessage());
      });
  }


  private void getAllDeviceByIdApplication(RoutingContext routingContext) {

    String count = sqlGetAllDeviceByIdApp(routingContext);
    String sql = sqlFilterApp(routingContext);

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

  private void getCompanyHaveSupervisorById(RoutingContext routingContext) {

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

  private void updateDeviceToUser(RoutingContext routingContext) {
  }


  // password

  private void updatePassword(RoutingContext routingContext) {

  }

  private void setPassword(RoutingContext routingContext) {

  }


  // Session
  private void statusSession(RoutingContext routingContext) {


    String id = routingContext.user().principal().getString("sessionId");

    String sql = " SELECT token FROM app_chirpstack_user.session_up2 " +
      "WHERE id =  '" + id + "'";

    pool
      .getConnection()
      .compose(conn -> conn
        .query(sql)
        .execute()
        .compose(result -> {
          if (result.rowCount() != 1)
            return Future.failedFuture(NOT_FOUND);
          Row row = result.iterator().next();
          JsonObject session = new JsonObject()
            .put("token", row.getString("token"))
            .put("status", true);

          return Future.succeededFuture(session);
        })
        .recover(err -> {
          return Future.failedFuture(err.getMessage());
        })
        .onSuccess(session -> {
          routingContext
            .response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(session.encode());
        })
        .onFailure(err -> {
          routingContext
            .response()
            .putHeader("content-type", "application/json")
            .end(err.getMessage());
        })
      );

  }

  /**
   * Probabelmente este metodo hay que mejorlo mucho
   * aun que por ahora hace lo que tiene que hacer:
   *
   * @param routingContext
   */
  private void login(RoutingContext routingContext) {

    final String sessionId = getUUID.toString();

    String email = routingContext.request().getParam("email");
    String password = routingContext.request().getParam("password");

    String login = sqlLogin(routingContext);


    pool
      .getConnection()
      .compose(conn -> conn
        .query(login)
        .execute()
        .compose(result -> {
          if (result.rowCount() != 1)
            return Future.failedFuture(NOT_FOUND);
          Row row = result.iterator().next();
          System.out.println(row.toJson().encodePrettily());

          String role = getRole(row.getUUID("id").toString(),
            row.getUUID("role_id").toString());

          JsonObject session = new JsonObject();
          if (email.equals(row.getString("email")) &&
            password.equals(row.getString("password"))) {

            System.out.println("login OK ...");

            JWTAuthOptions jwt = new JWTAuthOptions()
              .addPubSecKey(new PubSecKeyOptions()
                .setAlgorithm("HS256")
                .setBuffer("superKey"));
            jwt.getJWTOptions().setSubject(sessionId);


            JWTAuth auth = JWTAuth.create(vertx, jwt);
            String token =
              auth.generateToken(new JsonObject()
                .put("sessionId", sessionId)
                .put("userId", row.getUUID("id").toString())
                .put("lastSeen", Optional
                  .ofNullable(row.getOffsetDateTime("last_seen"))
                  .map(OffsetDateTime::toInstant)
                  .orElse(null))
                .put("name", row.getString("name"))
                .put("surname", row.getString("surname"))
                .put("companyId", row.getUUID("company_id").toString())
                .put("role", role)
                .put("email", row.getString("email")), jwt.getJWTOptions());

            session
              .put("sessionId", sessionId)
              .put("token", token)
              .put("user", new JsonObject()
                .put("id", row.getUUID("id").toString())
                .put("createdAt", row.getOffsetDateTime("created_at").toInstant())

                .put("lastSeen", Optional
                  .ofNullable(row.getOffsetDateTime("last_seen"))
                  .map(OffsetDateTime::toInstant)
                  .orElse(null))
                .put("role", role)
                .put("name", row.getString("name"))
                .put("companyId", row.getUUID("company_id").toString())
                .put("surname", row.getString("surname"))
                .put("email", row.getString("email")));

            return Future.succeededFuture(session);
          }

          return Future.succeededFuture();
        })
        .compose(session -> {
          System.out.println("Token: " + session.encodePrettily());

          String saveSession = sqlInsertSessionData(session, sessionId);
          String updateLastSeen = sqlUpdateLastSession(session);
          conn.
            query(saveSession)
            .execute()
            .compose(res4 ->
              conn.
                query(updateLastSeen)
                .execute());

          return Future.succeededFuture(session.getString("token"));

        })
        .recover(err -> {
          System.out.println(err.getMessage());
          return Future.failedFuture("error!");
        })
        .onSuccess(res3 -> {

          routingContext
            .response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("token", res3).encode());
        }))

      .onFailure(err -> {
        routingContext
          .response()
          .putHeader("content-type", "application/json")
          .end(err.getMessage()); // TODO: rever erroes
        System.out.println(err.getMessage());
      });

  }


  private void logout(RoutingContext routingContext) {

    String id = routingContext.user().principal().getString("sessionId");

    String sql = "DELETE FROM app_chirpstack_user.session_up2 WHERE  id = '" + id + "' ";

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
                    return Future.failedFuture(NOT_FOUND);
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

  private void updateSession(RoutingContext routingContext) {

    JsonObject updateSession = session(routingContext);

    JWTAuthOptions jwt = new JWTAuthOptions()
      .addPubSecKey(new PubSecKeyOptions()
        .setAlgorithm("HS256")
        .setBuffer("superKey"));
    jwt.getJWTOptions().setSubject(updateSession.getString("sessionId"));

    JWTAuth auth = JWTAuth.create(vertx, jwt);
    String token =
      auth.generateToken(updateSession, jwt.getJWTOptions());

    String sql = " UPDATE app_chirpstack_user.session_up2  SET token = '" + token +
      "' WHERE id = '" + updateSession.getString("sessionId") + "' RETURNING token";

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
                    return Future.failedFuture(NOT_FOUND);
                  }
                  tx.commit();
                  return Future.succeededFuture(size.iterator().next().toJson());
                })
                .eventually(v -> conn.close());
          })
          .recover(err -> {
            return Future.failedFuture(err.getMessage());
          })
          .onSuccess(app -> {
            routingContext.response().putHeader("content-type", "application/json").end(app.encode());

          })
          .onFailure(err -> {
            routingContext.response().end(err.getMessage());
            System.out.println(err.getMessage());

          })
      );


  }


  // dashboard

  private void getDashboardById(RoutingContext routingContext) {

  }


  // CRUD Device


  private void getAllDevice(RoutingContext routingContext) {

    String sql = sqlFilterDevice(routingContext);

    String count = " SELECT COUNT(*) from app_chirpstack_user.device ";

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

                  if (rows.rowCount() == 0)
                    return Future.failedFuture(NOT_FOUND);

                  JsonArray items = new JsonArray();
                  JsonObject result = new JsonObject();
                  for (Row row : rows) {
                    items.add(
                       new JsonObject()
                        .put("id", row.getUUID("id"))
                        .put("applicationId", row.getUUID("app_id"))
                        .put("applicationName", row.getString("name"))
                        .put("serial", row.getString("serial"))
                    );
                    result.put("size", res).put("items", items);
                  }
                  return Future.succeededFuture(result);
                })
                .eventually(v -> conn.close()));
        })

        .recover(err -> {
          return Future.failedFuture(errorSqlEx(err).toString());
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

  private void getDeviceById(RoutingContext routingContext) {

    String sql = sqlGetDeviceById(routingContext);

    System.out.println(sql);

    pool
      .getConnection()
      .flatMap(conn -> conn
        .begin()
        .flatMap(tx -> {
          return
            conn
              .query(sql)
              .execute()
              .flatMap(rows -> {

                if (rows.rowCount() != 1) {
                  return Future.failedFuture(NOT_FOUND);
                }

                Row row = rows.iterator().next();
                JsonObject result = new JsonObject()
                  .put("id", row.getUUID("id"))
                  .put("applicationId", row.getUUID("app_id"))
                  .put("applicationName", row.getString("app_name"))
                  .put("serial", row.getString("serial"));

                System.out.println("ID: " + result.encodePrettily());
                return Future.succeededFuture(result);
              })
              .eventually(v -> conn.close());
        })

        .recover(err -> {
          System.out.println(err.getMessage());
          return Future.failedFuture(err.getMessage());
        })

        .onSuccess(device -> {
          routingContext.response().putHeader("content-type", "application/json").end(device.encode());
          System.out.println(device.encodePrettily());
        })
        .onFailure(err -> {
          routingContext.response().putHeader("content-type", "application/json").end(err.getMessage());
          System.out.println(err.getMessage());
        })

      );
  }

  private void exportFileDevice(RoutingContext routingContext) {
  }

  private void getDeviceByIdGraphic(RoutingContext routingContext) {

  }

  private void getExportDeviceByIdGraphic(RoutingContext routingContext) {

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
      .setHost("192.168.0.104")
        .setDatabase("chirpstack")
      .setUser("chirpstack_user")
      .setPassword("user3344");

    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    this.pool = PgPool.pool(vertx, connectOptions, poolOptions);


    return Future.succeededFuture();
  }

  /**
   * Funcion para los tipos de error que ocurren
   * con los fallos del servidor, parametros
   * y conexion con la BBDD
   * @param rc
   * @param code
   * @param cause
   */

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

  /**
   * Esta funcion no es segura habria que mejorarlo tambien.
   * A veces que falla cuando el tipo de error no de SQL
   * @param error
   * @return
   */
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

  /**
   * metodo para validar errores de SQL
   * no es efectivo. habria que mejorarlo.
   * @param code
   * @return
   */
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

  /**
   * Esta desarrollada pero no implementada
   * El probela esta cuando el resultado de la consulta es nulo o es 0
   * no envia el Futuro considera que la operacion ha ido bien entonces sique la sequiencia de llamada.
   * Ejemplo veras el el metodo (getAllCompany).
   * @param routingContext
   * @return
   */

  public Future<Void> haveSession(RoutingContext routingContext) {

    String id = routingContext.user().principal().getString("sessionId");
    String session = "SELECT COUNT(id) FROM app_chirpstack_user.session_up2  WHERE id = '" + id + "' ";

    return
      pool
        .getConnection()
        .compose(conn -> //{
          conn
            .begin()
            .compose(tx -> {
              return
                conn
                  .query(session)
                  .execute()
                  .compose(size -> {
                    // System.out.println("Testy: " + size.rowCount());
                    if (size.rowCount() != 1) {
                      return Future.failedFuture(new Throwable().getMessage()); // ERROR
                    }
                    return tx.commit();

                  })
                  .eventually(vm -> conn.close());
            })
            .recover(err -> {
              return Future.failedFuture(err.getMessage());
            })
            .onSuccess(rows -> System.out.println("Operation Ok "))
            .onFailure(err -> System.out.println(err.getMessage()))
        );

  }

  /**
   * El mismo problema con los futuros. desarrollado pero no implementado
   *
   * @param tableName
   * @return
   */
  public Future<Integer> countRows(String tableName) {

    String count = " SELECT COUNT(*) from  " + tableName + " ";

    return pool
      .getConnection()
      .compose(conn -> //{
        conn
          .begin()
          .compose(tx -> {
            return
              conn
                .query(count)
                .execute()
                .compose(size -> {
                  // System.out.println("Testy: " + size.rowCount());
                  if (size.rowCount() != 1) {
                    return Future.failedFuture(NOT_FOUND);
                  }
                  return tx.commit()
                    .map(size.rowCount());
                })
                .eventually(vm -> conn.close());
          })
          .recover(err -> {
            return Future.failedFuture(err.getMessage());
          })
          .onSuccess(rows -> System.out.println("Total Rows: " + rows))
          .onFailure(err -> System.out.println(err.getMessage()))
      );
  }


}
