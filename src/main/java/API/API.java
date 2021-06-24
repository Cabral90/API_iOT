package API;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.validation.BadRequestException;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.ext.web.validation.builder.Bodies;
import io.vertx.ext.web.validation.builder.Parameters;
import io.vertx.json.schema.Schema;
import io.vertx.json.schema.SchemaParser;
import io.vertx.json.schema.SchemaRouter;
import io.vertx.json.schema.SchemaRouterOptions;
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.*;


import javax.print.attribute.standard.JobStateReasons;
import java.util.UUID;

import static API.CreateOject.Task;
import static API.ValidateData.*;
import static io.vertx.ext.web.validation.builder.Bodies.formUrlEncoded;
import static io.vertx.ext.web.validation.builder.Bodies.json;
import static io.vertx.ext.web.validation.builder.Parameters.param;
import static io.vertx.json.schema.common.dsl.Schemas.*;


public class API extends AbstractVerticle {

  private HttpServerResponse response;

  private PgPool pool;
  private Router router;

  public static void main(String[] args) {

    Vertx.vertx().deployVerticle(new API());
  }

  /**
   *
   */
  @Override

  public void start() { // Promise<Void> startPromise


    System.out.println(" Init.....");
    clientDB()
      .compose(this::setUpInitialData);

    System.out.println(" Init.....");

  }


  public Future<Void> setUpInitialData(Void vd) {
    System.out.println(" Init2 Router.....");

    RouterBuilder.create(vertx, "src/main/resources/openAPI3_v1_090621.yaml")
      .onSuccess(routerBuilder -> {
        System.out.println(" Init3 call function .....");

        // call all endpoints
        allFunction(routerBuilder);

        //Router router = Router.router(vertx)
        this.router = Router.router(vertx)
          .errorHandler(400, rc -> sendError(rc, 400, rc.failure().getMessage()));

        router.mountSubRouter("/v1", routerBuilder.createRouter());

        vertx
          .createHttpServer()
          .requestHandler(router)
          .listen(8080);

        System.out.println("pasamos");
      })
      .onFailure(Throwable::printStackTrace);
    return Future.succeededFuture();

  }


  private void allFunction(RouterBuilder router) {

    // CRUD Company
    router.operation("createCompany").handler(this::createCompany);
    router.operation("updateCompany").handler(this::updateCompany);
    router.operation("deleteCompany").handler(this::deleteCompany);
    router.operation("getAllCompany").handler(this::getAllCompany);
    router.operation("getCompanyById").handler(this::getCompanyById);


    // CRUD User
    router.operation("createUser").handler(this::createUser);
    router.operation("updateUser").handler(this::updateUser);
    router.operation("deleteUser").handler(this::deleteUser);
    router.operation("getAllUser").handler(this::getAllUser);
    router.operation("getUserById").handler(this::getUserById);

    // pass User
    router.operation("recoverPassword").handler(this::recoverPassword);
    //router.operation("updatePassword").handler(this::updatePassword);


    // Session
    router.operation("statusSession").handler(this::statusSession);
    router.operation("login").handler(this::login);
    router.operation("logout").handler(this::logout);

    // CRUD Device
    router.operation("createDevice").handler(this::createDevice);
    router.operation("updateDevice").handler(this::updateDevice);
    router.operation("deleteDevice").handler(this::deleteDevice);
    router.operation("getAllDevice").handler(this::getAllDevice);
    router.operation("getDeviceById").handler(this::getDeviceById);
    router.operation("getDeviceByName").handler(this::getDeviceByName);
    router.operation("getAllDeviceByIdCompany").handler(this::getAllDeviceByIdCompany);
    router.operation("getAllDeviceByIdUser").handler(this::getAllDeviceByIdUser);

    // Alert
    router.operation("getAllAlert").handler(this::getAllAlert);
    router.operation("getAlertByName").handler(this::getAlertByName);
    router.operation("getAlertByIdUser").handler(this::getAlertByIdUser);

    // operations
    /*router.operation("getAlertByIdUser").handler(this::"getAlertByIdUser");
    router.operation("getAlertByIdUser").handler(this::"getAlertByIdUser");
    router.operation("getAlertByIdUser").handler(this::"getAlertByIdUser");
    router.operation("getAlertByIdUser").handler(this::"getAlertByIdUser");*/

    // Task
    router.operation("createTask").handler(this::createTask);
    router.operation("updateTask").handler(this::updateTask);
    router.operation("deleteTask").handler(this::deleteTask);
    router.operation("getAllTask").handler(this::getAllTask);
    router.operation("getTaskById").handler(this::getTaskById);
    router.operation("getTaskByIdUser").handler(this::getTaskByIdUser);

  }


  // ========================  CRUD EMPRESA ===========================

  private void createCompany(RoutingContext routingContext) {


    // The object body request.
    JsonObject company = routingContext.getBodyAsJson();

    // variables
    String companyId = company.getString("companyId");


    System.out.println("Imprimimos el contenido enviado");
    System.out.println(company.encodePrettily());

    // sql
    String sqlCompany = "INSERT INTO app_chirpstack_user.company "
      + " (company_id, company_name, NIF, address, code_postal, phone, email, web)"
      + " VALUES ($1, $2, $3, $4, $5, $6, $7, $8 ) ";
    String findCompany = "SELECT * FROM app_chirpstack_user.company WHERE company_id =  '" + companyId + "' ";

    System.out.println("Query find company: " + findCompany);

    pool.preparedQuery(findCompany) // verified if exist a similar company
      .execute(find -> {
        if (find.succeeded()) {
          int size = find.result().rowCount();

          if (size == 0) {
            System.out.println("create a company");

            pool.preparedQuery(sqlCompany)
              .execute(Tuple.of(
                company.getString("companyId"),
                company.getString("companyName"),
                company.getString("nif"),
                company.getString("address"),
                company.getString("codePostal"),
                company.getString("phone"),
                company.getString("email"),
                company.getString("web")
              ), ar -> {
                if (ar.succeeded()) {
                  RowSet<Row> rows = ar.result();
                  System.out.println(rows.rowCount());

                  // if ok, return ID of this company
                  pool.query(findCompany)
                    .execute(ar2 -> {
                      if (ar2.succeeded()) {

                        // create a jsonObject to returns
                        JsonObject thisCompany = new JsonObject();
                        RowSet<Row> result = ar2.result();
                        Row row = result.iterator().next();
                        thisCompany.put("company", row.toJson());
                        //System.out.println("Got " + result.size() + " rows ");
                        System.out.println(row.toJson().encodePrettily());

                        routingContext.response().putHeader("content-type", "application/json").end(thisCompany.encode());
                      } else {
                        ar2.cause().printStackTrace();
                      }
                    });
                } else {
                  ar.cause().printStackTrace();
                }
              });

          } else {
            System.out.println("Company exist: " + size);
            System.out.println("Not create a company bat have in system similar company");

          }
          routingContext.response().end();
        } else {
          System.out.println("Not create a company bat have in system similar company");
          routingContext.response().end();
          this.router.errorHandler(400, rc -> sendError(rc, 400, rc.failure().getMessage()));
          find.cause().printStackTrace();
        }
      });


  }

  private void updateCompany(RoutingContext routingContext) {
    response = routingContext.response();

    // The object body request.
    JsonObject company = routingContext.getBodyAsJson();
    // parameter from query request
    String companyId = routingContext.request().getParam("companyId"); // only value

    System.out.println(company.encodePrettily());

    String findCompany = "SELECT * FROM app_chirpstack_user.company WHERE company_id = '" + companyId + "' ";
    System.out.println("myQuery: " + findCompany);

    String updateCompany = "UPDATE app_chirpstack_user.company SET "
      + " company_name = $1, NIF = $2, address = $3, code_postal = $4, phone = $5, web = $6"
      + " WHERE company_id = '" + companyId + "' ";

    System.out.println("Query: " + updateCompany);

    pool.preparedQuery(findCompany) // find company
      .execute(ar -> {
        if (ar.succeeded()) {
          int size = ar.result().rowCount();
          if (size == 1) {
            // have company
            System.out.println("actualizando ....");
            pool.preparedQuery(updateCompany) // update company
              .execute(Tuple.of(
                company.getString("companyName"),
                company.getString("nif"),
                company.getString("address"),
                company.getString("codePostal"),
                company.getString("phone"),
                company.getString("web")
              ), ar1 -> {
                if (ar1.succeeded()) {
                  RowSet<Row> rows = ar1.result();
                  System.out.println(rows.rowCount());

                  // if ok, return ID of this company
                  pool.preparedQuery(findCompany) // return company
                    .execute(ar2 -> {
                      if (ar2.succeeded()) {
                        // create a jsonObject to returns
                        JsonObject thisCompany = new JsonObject();
                        RowSet<Row> result = ar2.result();
                        Row row = result.iterator().next();
                        thisCompany.put("company", row.toJson());
                        //System.out.println("Got " + result.size() + " rows ");
                        System.out.println(row.toJson().encodePrettily());

                        routingContext.response().putHeader("content-type", "application/json").end(thisCompany.encode());
                      } else {
                        System.out.println("arg 2 error");
                        ar2.cause().printStackTrace();
                      }
                    });
                } else {
                  System.out.println("arg 1 error");
                  ar1.cause().printStackTrace();
                }
              });
          } else {
            // no faund company
            System.out.println("No found Company");
            sendError2(404, response, "Company not found");
            this.router
              .errorHandler(404, rc -> sendError(rc, 400, rc.failure().getMessage()));
            ar.cause().printStackTrace();
          }

        } else {
          this.router.errorHandler(404, rc -> sendError(rc, 400, rc.failure().getMessage()));
          //sendError(400, response, "Invalid ID supplied");
          ar.cause().printStackTrace();
        }
      });
  }


  private void deleteCompany(RoutingContext routingContext) {
    String companyId = routingContext.request().getParam("companyId"); // only value

    String findCompany = "SELECT * FROM app_chirpstack_user.company WHERE company_id = '" + companyId + "' ";

    String deleteCompany = "DELETE FROM app_chirpstack_user.company WHERE company_id = '" + companyId + "' ";
    System.out.println("qury delete: " + deleteCompany);

    pool.preparedQuery(findCompany)
      .execute(find -> {
        if (find.succeeded()) {
          int size = find.result().rowCount();
          if (size == 1) {

            pool.preparedQuery(deleteCompany)
              .execute(ar -> {
                if (ar.succeeded()) {
                  routingContext.response().setStatusCode(200).end();
                } else {
                  routingContext.response().setStatusCode(400).end();
                  ar.cause().printStackTrace();
                }
              });

          } else {
            System.out.println("Company Not found");
          }
        }
      });


  }

  public void getAllCompany(RoutingContext routingContext) {

    response = routingContext.response();

    System.out.println("COMPANY");
    //JsonObject company = new JsonObject();

    JsonArray companies = new JsonArray();
    //JsonObject myObc = new JsonObject();

    pool.query("SELECT * FROM app_chirpstack_user.company")
      .execute(ar -> {
        if (ar.failed()) {
          // routingContext.fail(500);

          //sendError(404, response, "Companies not found");
        } else {
          for (Row row : ar.result()) {
            companies.add(new JsonObject().put("items", row.toJson()));
          }
          companies.add(new JsonObject().put("size", ar.result().rowCount()));
          routingContext.response().putHeader("content-type", "application/json").end(companies.encode());
          System.out.println(companies.encodePrettily());
        }

      });

  }

  private void getCompanyById(RoutingContext routingContext) {

    HttpServerResponse serverResponse = routingContext.request().response();

    response = routingContext.response();
    String companyId = routingContext.request().getParam("companyId"); // only value

    String findCompany = "SELECT * FROM app_chirpstack_user.company WHERE company_id = '" + companyId + "' ";
    System.out.println("query delete: " + findCompany);
    pool.preparedQuery(findCompany)
      .execute(ar -> {
        if (ar.succeeded()) {
          JsonObject thisCompany = new JsonObject();
          RowSet<Row> result = ar.result();
          Row row = result.iterator().next();
          thisCompany.put("company", row.toJson());

          System.out.println(row.toJson().encodePrettily());

          routingContext.response().putHeader("content-type", "application/json").end(thisCompany.encode());
        } else {
          // sendError(404, response, "Company not found");
          //serverResponse.putHeader()
          //serverResponse.setStatusCode(404).end();
          ar.cause().printStackTrace();
        }
      });
  }


  // =============== CRUD USER ==========

  private void createUser(RoutingContext routingContext) { // TODO will by i create one a function validate if exist another user a similar id and email.


    // =====================================

    JsonObject user = routingContext.getBodyAsJson();

    // boolean action =  validateCreateUserData(pool, user, routingContext);

    validateCreateUserData(pool, user, routingContext);


    //System.out.println("ACTION: "+ action);

    /*String id = user.getString("userId");
    String email = user.getString("email");

    System.out.println("Imprimimos el contenido enviado");
    System.out.println(user.encodePrettily());

    String findUserById = " SELECT * FROM app_chirpstack_user.user WHERE id =  '" + id + "' ";
    String findUserByEmail = "SELECT * FROM app_chirpstack_user.user WHERE email =  '" + email + "'";
    String creteCompany = "INSERT INTO app_chirpstack_user.user "
      + " (id, role_id, company_id, name, nicknames, email, password)"
      + " VALUES ($1, $2, $3, $4, $5, $6, $7 ) ";

    pool.preparedQuery(findUserById)
      .execute(find -> {
        if (find.succeeded()) {
          int thisRow = find.result().rowCount();
          if (thisRow == 0) {
            System.out.println("Not yet user this id");
            pool.preparedQuery(findUserByEmail)
              .execute(find1 -> {
                int thisRow1 = find1.result().rowCount();
                if (thisRow1 == 0) {

                  pool.preparedQuery(creteCompany)
                    .execute(Tuple.of(
                      user.getString("userId"),
                      user.getString("roleId"),
                      user.getString("companyId"),
                      user.getString("name"),
                      user.getString("nicknames"),
                      user.getString("email"),
                      user.getString("password")
                    ), ar -> {
                      if (ar.succeeded()) {
                        RowSet<Row> rows = ar.result();
                        System.out.println(rows.rowCount());

                        // if ok, return ID of this company
                        pool.query(findUserById)
                          .execute(ar2 -> {
                            if (ar2.succeeded()) {

                              // create a jsonObject to returns
                              JsonObject thisUser = new JsonObject();
                              RowSet<Row> result = ar2.result();
                              Row row = result.iterator().next();
                              thisUser.put("user", row.toJson());
                              //System.out.println("Got " + result.size() + " rows ");
                              System.out.println(row.toJson().encodePrettily());

                              routingContext.response().putHeader("content-type", "application/json").end(thisUser.encode());
                            } else {
                              ar2.cause().printStackTrace();
                            }
                          });
                      } else {
                        System.out.println("error de json");
                        ar.cause().printStackTrace();
                      }
                    });

                  System.out.println("Not yet user this email");

                } else {

                  System.out.println("No create bat have one user of this email");
                }
              });


          } else {
            System.out.println("No create bat have a similar data");
          }
        } else {
          System.out.println("Errorl de sql");
          find.cause().printStackTrace();
        }
      });*/


  }

  private void updateUser(RoutingContext routingContext) {
    response = routingContext.response();

    //response = routingContext.response();
    System.out.println("Update data for User ....  ");

    // The object body request.
    JsonObject user = routingContext.getBodyAsJson();
    JsonObject userUpdate = new JsonObject();
    // parameter from query request
    String userId = routingContext.request().getParam("userId"); // only value

    System.out.println(user.encodePrettily());

    String findUser = "SELECT * FROM app_chirpstack_user.user WHERE id = '" + userId + "' ";
    System.out.println("myQuery: " + findUser);

    String updateUser = "UPDATE app_chirpstack_user.user SET "
      + " role_id = $1,  company_id = $2, name = $3, nicknames = $4 "
      + " WHERE id = '" + userId + "' ";

    System.out.println("Query: " + updateUser);

    pool.preparedQuery(findUser) // find company
      .execute(ar -> {
        if (ar.succeeded()) {
          int size = ar.result().rowCount();
          if (size == 1) {
            // have company
            System.out.println("actualizando ....");
            pool.preparedQuery(updateUser) // update company
              .execute(Tuple.of(
                user.getString("roleId"),
                user.getString("companyId"),
                user.getString("name"),
                user.getString("nicknames")

              ), ar1 -> {
                if (ar1.succeeded()) {
                  RowSet<Row> rows = ar1.result();
                  System.out.println(rows.rowCount());

                  pool.preparedQuery(findUser) // return User
                    .execute(ar2 -> {
                      if (ar2.succeeded()) {
                        // create a jsonObject to returns
                        JsonObject thisUser = new JsonObject();
                        RowSet<Row> result = ar2.result();
                        Row row = result.iterator().next();
                        //thisUser.put("user", row.toJson());
                        thisUser.put("roleId", row.getUUID("role_id"));
                        thisUser.put("companyId", row.getUUID("company_id"));
                        thisUser.put("name", row.getString("name"));
                        thisUser.put("nicknames", row.getString("nicknames"));
                        //System.out.println("Got " + result.size() + " rows ");
                        System.out.println("User update");
                        System.out.println(row.toJson().encodePrettily());

                        routingContext.response().putHeader("content-type", "application/json").end(thisUser.encode());
                      } else {
                        System.out.println("arg 2 error");
                        ar2.cause().printStackTrace();
                      }
                    });
                } else {
                  System.out.println("arg 1 error");
                  ar1.cause().printStackTrace();
                }
              });
          } else {
            // no faund company
            System.out.println("No found User");
            //sendError2(404, response, "User not found");
            this.router.errorHandler(404, rc -> sendError(rc, 404, rc.failure().getMessage()));

            ar.cause().printStackTrace();
          }

        } else {
          //sendError(400, response, "Invalid ID supplied");
          this.router.errorHandler(400, rc -> sendError(rc, 400, rc.failure().getMessage()));
          ar.cause().printStackTrace();
        }
      });

  }

  private void deleteUser(RoutingContext routingContext) {

    String userId = routingContext.request().getParam("userId"); // only value

    String findUser = "SELECT * FROM app_chirpstack_user.user WHERE id = '" + userId + "' ";
    System.out.println("myQuery: " + findUser);

    String deleteUser = "DELETE FROM app_chirpstack_user.user WHERE id = '" + userId + "' ";
    System.out.println("qury delete: " + deleteUser);

    pool.preparedQuery(findUser)
      .execute(find -> {
        if (find.succeeded()) {
          int size = find.result().rowCount();
          if (size == 1) {

            pool.preparedQuery(deleteUser)
              .execute(ar -> {
                if (ar.succeeded()) {
                  routingContext.response().setStatusCode(200).end();
                } else {
                  routingContext.response().setStatusCode(400).end();
                  ar.cause().printStackTrace();
                }
              });

          } else {
            System.out.println("User Not found");
          }
        }
      });


  }

  private void getUserById(RoutingContext routingContext) {
    System.out.println("usuario por ID.... ");


    response = routingContext.response();
    String userId = routingContext.request().getParam("userId"); // only value

    String findUser = "SELECT * FROM app_chirpstack_user.user WHERE id = '" + userId + "' ";
    System.out.println("query select: " + findUser);
    pool.preparedQuery(findUser)
      .execute(ar -> {
        if (ar.succeeded()) {
          JsonObject thisUser = new JsonObject();
          RowSet<Row> result = ar.result();
          System.out.println("result: " + result.toString());
          int res = result.rowCount();
          //System.out.println("res: " + res);
          if (res == 1) {
            Row row = result.iterator().next();
            thisUser.put("user", row.toJson());

            System.out.println(row.toJson().encodePrettily());

            routingContext.response().putHeader("content-type", "application/json").end(thisUser.encode());
          } else {
            System.out.println("User not found");
            //sendError2(404, response, "User not found");
            this.router.errorHandler(404, rc -> sendError(rc, 404, rc.failure().getMessage()));

          }


        } else {
          //sendError2(404, response, "User not found");
          this.router.errorHandler(400, rc -> sendError(rc, 400, rc.failure().getMessage()));
          System.out.println("Error Query");
          ar.cause().printStackTrace();
        }
      });
  }

  private void getAllUser(RoutingContext routingContext) {

    response = routingContext.response();

    System.out.println("USER");
    //JsonObject company = new JsonObject();

    JsonArray users = new JsonArray();
    //JsonObject myObc = new JsonObject();

    pool.query("SELECT * FROM app_chirpstack_user.user")
      .execute(ar -> {
        if (ar.failed()) {
          // routingContext.fail(500);

          sendError2(404, response, "Users not found");
        } else {
          for (Row row : ar.result()) {
            //users.add(new JsonObject().put("items", row.toJson()));
            users.add(new JsonObject()
              .put("items", new JsonObject()
                .put("userId", row.getUUID("id"))
                .put("roleId", row.getUUID("role_id"))
                .put("companyId", row.getUUID("company_id"))
                .put("name", row.getString("name"))
                .put("nicknames", row.getString("nicknames"))
                .put("email", row.getString("email"))
                .put("password", row.getString("password"))

              ));
          }
          users.add(new JsonObject().put("size", ar.result().rowCount()));
          routingContext.response().putHeader("content-type", "application/json").end(users.encode());
          System.out.println(users.encodePrettily());
        }

      });


  }

  private void recoverPassword(RoutingContext routingContext) {

    response = routingContext.response();

    //response = routingContext.response();
    System.out.println("Update data for User ....  ");

    // The object body request.
    JsonObject user = routingContext.getBodyAsJson();

    // parameter from query request
    String userId = routingContext.request().getParam("userId"); // only value

    System.out.println("IDUSER: " + userId);

    System.out.println(user.encodePrettily());

    String findUser = "SELECT * FROM app_chirpstack_user.user WHERE id = '" + userId + "' ";
    System.out.println("myQuery: " + findUser);

    String updatePassword = "UPDATE app_chirpstack_user.user SET "
      + " password = $1  WHERE id = '" + userId + "' ";

    System.out.println("Query: " + updatePassword);

    pool.preparedQuery(findUser) // find company
      .execute(ar -> {
        if (ar.succeeded()) {
          int size = ar.result().rowCount();
          if (size == 1) {
            // have company
            System.out.println("actualizando ....");
            pool.preparedQuery(updatePassword) // update company
              .execute(Tuple.of(
                encriptyPass(user.getString("newPassword"))

              ), ar1 -> {
                if (ar1.succeeded()) {

                  sendError2(200, response, "Operation ok");
                  // routingContext.response().putHeader("content-type", "application/json").end(thisUser.encode());

                } else {
                  System.out.println("Error query");
                  ar1.cause().printStackTrace();
                }
              });
          } else {
            // no faund company
            System.out.println("No found User");
            // sendError2(404, response, "User not found");
            this.router.errorHandler(404, rc -> sendError(rc, 404, rc.failure().getMessage()));

            ar.cause().printStackTrace();
          }

        } else {
          //sendError(400, response, "Invalid ID supplied");
          this.router.errorHandler(400, rc -> sendError(rc, 400, rc.failure().getMessage()));
          ar.cause().printStackTrace();
        }
      });


  }

  private void updatePassword(RoutingContext routingContext) {
  }

  // ====================== SESSION ===========================

  private void login(RoutingContext routingContext) {


    //response = routingContext.response();
    System.out.println("Estamos en el login");


    // parameter from query request
    String email = routingContext.request().getParam("email");
    String password = routingContext.request().getParam("password");

    System.out.println("Email: " + email);
    System.out.println("Password: " + password);


    //if (email != null && password != null) {
    String login = "SELECT * FROM app_chirpstack_user.user WHERE email = '" + email + "' AND password = '" + password + "' ";
    pool.preparedQuery(login)
      .execute(ar -> {
        if (ar.succeeded()) {
          int size = ar.result().rowCount();
          if (size == 1) {

            // comprabar si el email de usuario existe en una session

            JsonObject thisUser = new JsonObject();
            RowSet<Row> result = ar.result();
            Row row = result.iterator().next();
            //thisUser.put("user", row.toJson());
            // Genera uuid
            UUID sessionId = UUID.randomUUID();


            String startSession = "INSERT INTO app_chirpstack_user.session_up " +
              "(id, user_id, company_id, role_id, email) " +
              "VALUES " +
              " ( '" + sessionId + "'::uuid, " +
              " '" + row.getUUID("id") + "'::uuid, " +
              " '" + row.getUUID("company_id") + "'::uuid, " +
              " '" + row.getUUID("role_id") + "'::uuid, " +
              " '" + row.getString("email") + "' )";

            System.out.println("INSERT: " + startSession);

            pool.preparedQuery(startSession)
              .execute(sesion -> {
                if (sesion.succeeded()) {
                  System.out.println(row.toJson().encodePrettily());
                  JsonObject sessionID = new JsonObject().put("sessionId", sessionId);
                  JsonObject User = new JsonObject();
                  User.put("sessionId", sessionId)
                    .put("userId", row.getUUID("id"))
                    .put("companyId", row.getUUID("company_id"))
                    .put("roleType", getRole(row.getUUID("role_id")))
                    .put("name", row.getString("name"))
                    .put("nicknames", row.getString("nicknames"))
                    .put("email", row.getString("email"))
                    .put("status", true);


                  System.out.println(User.encodePrettily());


                  // routingContext.response().putHeader("content-type", "application/json").end(sessionID.encode());
                  routingContext.response().putHeader("content-type", "application/json").end(User.encode());
                } else {
                  System.out.println("Not start session");
                }
              });

          } else {
            System.out.println("error Query");
          }
        }
      });


  }

  private void statusSession(RoutingContext routingContext) {

    String sessionId = routingContext.request().getParam("sessionId");

    String statusSession = "SELECT * FROM app_chirpstack_user.session_up WHERE id =  '" + sessionId + "' ";

    System.out.println("Query session :" + statusSession);
    JsonObject session = new JsonObject();
    pool
      .preparedQuery(statusSession)
      .execute(ar -> {
        if (ar.succeeded()) {
          int size = ar.result().rowCount();
          if (size == 1) {
            session.put("status", true).put("code", 200);
            System.out.println(session.encodePrettily());
            routingContext.response().putHeader("content-type", "application/json").end(session.encode());

          } else {
            session.put("status", false).put("code", 404);
            System.out.println(session.encodePrettily());
            routingContext.response().putHeader("content-type", "application/json").end(session.encode());
          }
        }
      });

  }

  private void logout(RoutingContext routingContext) {
    String sessionId = routingContext.request().getParam("sessionId");

    String statusSession = "SELECT * FROM app_chirpstack_user.session_up WHERE id =  '" + sessionId + "' ";
    String logout = "DELETE FROM app_chirpstack_user.session_up WHERE id =  '" + sessionId + "' ";
    System.out.println("query 1" + statusSession);
    System.out.println("query 2" + logout);

    pool
      .preparedQuery(statusSession)
      .execute(session -> {
        if (session.succeeded()) {
          int size = session.result().rowCount();
          if (size == 1) {
            pool
              .preparedQuery(logout)
              .execute(ar -> {
                routingContext.response().setStatusCode(200).end();
                System.out.println("Hemos borrado la session.");
              });
          } else {
            System.out.println("ho nay session");
            routingContext.response().setStatusCode(404).end();
          }
        } else {
          System.out.println("Error");
        }
      });

  }

  // ========================== CRUD DEVICE =============================

  private void createDevice(RoutingContext routingContext) {

  }

  private void updateDevice(RoutingContext routingContext) {

  }

  private void deleteDevice(RoutingContext routingContext) {

  }

  private void getAllDevice(RoutingContext routingContext) {

  }


  private void getDeviceById(RoutingContext routingContext) {
  }

  private void getDeviceByName(RoutingContext routingContext) {
  }

  private void getAllDeviceByIdCompany(RoutingContext routingContext) {
  }

  private void getAllDeviceByIdUser(RoutingContext routingContext) {
  }


  // ================== CRUD TASK  =============
  private void createTask(RoutingContext routingContext) {
    // The object body request.
    JsonObject task = routingContext.getBodyAsJson();
    UUID taskId = UUID.randomUUID();

    String createTask = "INSERT INTO app_chirpstack_user.task " +
      "(id, user_id, device_name, task_name, if_temperature, if_humidity, email)" +
      " VALUES ( '" + taskId + "', '" + task.getString("userId") + "', " +
      " '" + task.getString("deviceName") + "',  '" + task.getString("taskName") + "', " +
      " " + task.getFloat("ifTemperature") + ", " + task.getFloat("ifHumility") + ", " +
      " '" + task.getString("email") + "' ) ";

    String findTask = "SELECT * FROM app_chirpstack_user.task WHERE id = '" + taskId + "' ";

    System.out.println("create task : " + createTask);

    pool.query(createTask)
      .execute(ar -> {
        if (ar.succeeded()) {
          //Task();
          JsonObject thisTask = new JsonObject();

          pool.preparedQuery(findTask)
            .execute(ar1 -> {
              if (ar1.succeeded()) {
                int size = ar1.result().rowCount();
                if (size == 1) {
                  RowSet<Row> result = ar1.result();
                  Row row = result.iterator().next();
                  //Task(row.toJson());
                  System.out.println(Task(row.toJson()).encodePrettily());
                  routingContext.response().putHeader("content-type", "application/json").end(Task(row.toJson()).encode());
                } else {
                  System.out.println("Not found");
                }
              }
            });

        }
      });
  }

  private void updateTask(RoutingContext routingContext) {
    JsonObject task = routingContext.getBodyAsJson();
    String taskId = routingContext.request().getParam("taskId");

    String findTask = "SELECT * FROM app_chirpstack_user.task WHERE id = '" + taskId + "' ";

    String updateTask = "UPDATE app_chirpstack_user.task " +
      " SET task_name = '" + task.getString("taskName") + "'," +
      " if_temperature = " + task.getFloat("ifTemperature") + ", if_humidity = " + task.getFloat("ifHumility") + " " +
      " WHERE id = '" + taskId + "' ";

    System.out.println("update task: " + updateTask);
    pool
      .preparedQuery(findTask)
      .execute(ar -> {
        if (ar.succeeded()) {
          int size = ar.result().rowCount();
          if (size == 1) {
            pool
              .preparedQuery(updateTask)
              .execute(ar1 -> {
                if (ar.succeeded()) {
                  System.out.println("update ok");
                  routingContext.response().setStatusCode(200).end("successful operation");
                } else {
                  System.out.println("Error de query");
                  routingContext.response().setStatusCode(400).end("Error query");
                }
              });
          } else {
            routingContext.response().setStatusCode(400).end("Error query");
          }
        } else {
          System.out.println("Not found");
          routingContext.response().setStatusCode(404).end("Not found");
        }
      });
  }

  private void deleteTask(RoutingContext routingContext) {
    String taskId = routingContext.request().getParam("taskId");

    String findTask = "SELECT * FROM app_chirpstack_user.task WHERE id = '" + taskId + "' ";
    String deleteTask = "DELETE FROM app_chirpstack_user.task WHERE id = '" + taskId + "'";

    pool
      .preparedQuery(findTask)
      .execute(ar -> {
        if (ar.succeeded()) {
          int size = ar.result().rowCount();
          if (size == 1) {
            pool
              .preparedQuery(deleteTask)
              .execute(ar1 -> {
                if (ar1.succeeded()) {
                  System.out.println("delete ok");
                  routingContext.response().setStatusCode(200).end(" successful operation");
                } else {
                  System.out.println("Error query");
                  routingContext.response().setStatusCode(400).end("Invalid ID");
                }
              });
          } else {
            System.out.println("error query find");
            routingContext.response().setStatusCode(400).end("Not found task");
          }
        } else {
          System.out.println("Not found");
          routingContext.response().setStatusCode(404).end("Not found task");
        }
      });
  }

  private void getAllTask(RoutingContext routingContext) {

    String selectAllTask = " SELECT * FROM app_chirpstack_user.task ";
    JsonArray allTasks = new JsonArray();
    pool
      .query(selectAllTask)
      .execute(ar -> {
        if (ar.succeeded()) {
          int size = ar.result().rowCount();
          if (size >= 1) {
            RowSet<Row> result = ar.result();
            // Row row = result.iterator().next();
            for (Row row : ar.result()) {
              allTasks.add(new JsonObject().put("items", Task(row.toJson())));
            }
            allTasks.add(new JsonObject().put("size", ar.result().rowCount()));
            routingContext.response().putHeader("content-type", "application/json").end(allTasks.encode());
          } else {
            routingContext.response().setStatusCode(404).end("Not found task");
          }
        } else {
          routingContext.response().setStatusCode(400).end("Bad Request");
        }
      });

  }

  private void getTaskById(RoutingContext routingContext) {

    String taskId = routingContext.request().getParam("taskId");
    String findTask = "SELECT * FROM app_chirpstack_user.task WHERE id = '" + taskId + "' ";

    pool
      .preparedQuery(findTask)
      .execute(ar -> {
        if (ar.succeeded()) {
          int size = ar.result().rowCount();
          if (size == 1) {
            RowSet<Row> result = ar.result();
            Row row = result.iterator().next();

            System.out.println(Task(row.toJson()).encodePrettily());
            //Task(row.toJson())
            routingContext.response().putHeader("content-type", "application/json").end(Task(row.toJson()).encode());
          } else {
            routingContext.response().setStatusCode(404).end("Not found task");
          }
        } else {
          routingContext.response().setStatusCode(400).end("Bad Request");
        }
      });


  }

  private void getTaskByIdUser(RoutingContext routingContext) {

    System.out.println("all task user ....-> ");
    String userId = routingContext.request().getParam("userId");
    String findTaskUser = "SELECT * FROM app_chirpstack_user.task WHERE user_id = '" + userId + "' ";

    System.out.println("query: "+findTaskUser);

    JsonArray allTasks = new JsonArray();
    pool
      .query(findTaskUser)
      .execute(ar -> {
        if (ar.succeeded()) {
          int size = ar.result().rowCount();
          if (size >= 1) {
            RowSet<Row> result = ar.result();
            // Row row = result.iterator().next();
            for (Row row : ar.result()) {
              allTasks.add(new JsonObject().put("items", Task(row.toJson())));
            }
            allTasks.add(new JsonObject().put("size", ar.result().rowCount()));
            routingContext.response().putHeader("content-type", "application/json").end(allTasks.encode());
          } else {
            routingContext.response().setStatusCode(404).end("Not found task");
          }
        } else {
          routingContext.response().setStatusCode(400).end("Bad RequestDDD");
        }
      });
  }


  // ================== ALERT LIST ====================================

  private void getAllAlert(RoutingContext routingContext) {


  }

  private void getAlertByName(RoutingContext routingContext) {
  }

  private void getAlertByIdUser(RoutingContext routingContext) {
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


  // ========================== EXTRAS ================================


  // ===================== DATOS DE CONEXION BASE DE DATO ===============================


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

// =============== CONTROLADOR DE ERRORES  ================


  private static HttpServerResponse sendError2(
    int statusCode, HttpServerResponse response,
    String message
  ) {
    HttpServerResponse response1 = null;
    //HttpServerResponse serverResponse = routingContext.request().response();

    response1 = (HttpServerResponse) response.setStatusCode(statusCode).end(message);
    return response1;

  }


}


