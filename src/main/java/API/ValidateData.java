package API;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgException;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import javax.sound.midi.Soundbank;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.SQLOutput;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static API.createObject.getCompany;


public class ValidateData {
  private static final String SUPER_ADMIN = "23f0c751-d678-4c08-aa6d-0cee5cd5bae0";
  private static final String ADMIN = "506958f5-7c1f-4320-85af-2a5e0533706e";
  private static final String USER = "aaf86fd3-5ce8-4c60-aae7-36ba4be420da";

  // permission
  //private static final String INSERT_ID =""
  //private static final String INSERT_ID =""

  public static void main(String[] args) {
/*    System.out.println(encriptyPass("hola"));

    System.out.println("Pass1 1: "+
    ByteToHex( encriptyPass("hola")));

    System.out.println(" Pass2: "+encriptyPass2("hola"));*/

    System.out.println(setRolTypeUUID("superAdmin"));

  }

  public static void validateCreateUserData
    (
      PgPool pool, JsonObject user, RoutingContext routingContext
    ) { // Future<Boolean>

    //JsonObject user = routingContext.getBodyAsJson();
    String id = user.getString("userId");
    String email = user.getString("email");

    boolean isValid = false;

    AtomicBoolean action = new AtomicBoolean(false);

    System.out.println("Imprimimos el contenido enviado");
    System.out.println(user.encodePrettily());

    String findUserById = " SELECT * FROM app_chirpstack_user.user WHERE id =  '" + id + "' ";
    String findUserByEmail = "SELECT * FROM app_chirpstack_user.user WHERE email =  '" + email + "'";
    String creteCompany = "INSERT INTO app_chirpstack_user.user "
      + " (id, role_id, company_id, name, nicknames, email, password)"
      + " VALUES ($1, $2, $3, $4, $5, $6, $7 ) ";

    JsonObject company = new JsonObject();

    /*pool.getConnection()
      // Transaction must use a connection
      .onSuccess(conn -> {
        // Begin the transaction
        conn.begin()
          .compose(tx -> conn
            // Various statements
            .query(findUserById)
            .execute()
            .compose(res2 -> conn
              .query(findUserByEmail)
              .execute()
            .compose(res3 -> conn
            .query(creteCompany)
              .execute()))
            // Commit the transaction
            .onSuccess(rows -> company.put("Company", rows.iterator().next()))
            //.compose(res4 -> tx.commit())

          )
          // Return the connection to the pool

          //.onSuccess(v -> System.out.println("Transaction succeeded"))
          .onComplete( v -> System.out.println(company.encodePrettily()))
          .eventually(v -> conn.close())
          .onFailure(err -> System.out.println("Transaction failed: " + err.getCause()));

      });*/

   /* pool
      .getConnection()
      .onSuccess( conn -> {
        conn.begin()
          .compose( tx -> conn
      .query(findUserById)
      .execute()
      .compose( ar2 -> conn
        .query(""))
        )
      .onSuccess()
      .onFailure(Throwable::getMessage);
      });*/


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

                  // action.set(true);


                  pool.preparedQuery(creteCompany)
                    .execute(Tuple.of(
                      user.getString("userId"),
                      user.getString("roleId"),
                      user.getString("companyId"),
                      user.getString("name"),
                      user.getString("nicknames"),
                      user.getString("email"),
                      encriptyPass(user.getString("password"))
                      // user.getString(encriptyPass("password"))
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
      });

    // return action.get();

    // return Future.succeededFuture(isValid);

  }


  public static String encriptyPass(String pass) {
    String pwd = null;

    try {
      byte[] data = pass.getBytes();
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hash1 = md.digest(data);

      BigInteger bin = new BigInteger(1, hash1);
      pwd = String.format("%0" + (data.length << 1) + "X", bin);

    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return pwd;
  }

  public static String getRole(UUID role, PgPool pool) {

    String value = String.valueOf(role);

    String sqlRol = " SELECT * FROM app_chirpstack_user.role WHERE id = '" + role + "' ";
    String ROOLL = null;
    System.out.println("query: " + sqlRol);

    //return
    AtomicReference<String> roll3 = new AtomicReference<>();
    String v = null;
    pool
      .query(sqlRol)
      .execute(ar -> {
        if (ar.succeeded()) {
          RowSet<Row> result = ar.result();
          Row row = result.iterator().next();
/*          roll3.set(row.getString("type_role"));
          System.out.println(" MI_ROLE " + roll3);*/
          // returnData(row.getString("type_role"));
          //v = roll3.toString();

          // return Future.succeededFuture(row.getString("type_role"));

          //return type_role1;

        }
      });
    return "";

    //return Future.succeededFuture("");
    //AtomicReference<String> roll3 = new AtomicReference<>();
    //AtomicReference<String> roll3 = new AtomicReference<>();
      /*pool
      .getConnection()
      .compose(conn -> conn
        .query(sqlRol)
        .execute()
        .compose(rows -> {

          String rol = null;

          RowIterator<Row> rowIterator = rows.iterator();
          rol = rowIterator.next().getString("type_role");

          return Future.succeededFuture(rol);
        })
        .onSuccess(rol -> {



          return Future.succeededFuture(rol);

        ));*/

    /*String Roll = roll3.toString(º);

    System.out.println("role metodo");
    System.out.println(role);

    String typeRole = null;

    if (role.equals(value)) {
      typeRole = "superAdmin";
    }
    if (ADMIN.equals(value)) {
      typeRole = "admin";
    }
    if (USER.equals(value)) {
      typeRole = "user";
    }*/
    // return ROOLL;
  }

  public static String returnData(String roll) {
    System.out.println("new metodo: ROLE " + roll);
    return roll;
  }


  public static UUID setRolTypeUUID(String role) { //
    //String role = "superAdmin";
    UUID roleId = UUID.fromString("aaf86fd3-5ce8-4c60-aae7-36ba4be420da");

    if (role.equals("super Admin")) {
      roleId = UUID.fromString(SUPER_ADMIN);

    }

    if (role.equals("Admin")) {
      roleId = UUID.fromString(ADMIN);

    }

    if (role.equals("User")) {
      roleId = UUID.fromString(USER);
    }
    return roleId;
  }

  public static String setRolTypeTXT(String role) { //
    String roleText = null;

    if (role.equals(SUPER_ADMIN)) {
      roleText = "super Admin";

    }

    if (role.equals(ADMIN)) {
      roleText = "admin";

    }

    if (role.equals(USER)) {
      roleText = "user";
    }
    return roleText;
  }

  public static String getPermission(PgPool pool, UUID roleUserId) {
    String permission = null;
    String sql = " SELECT * FROM app_chirpstack_user.role_permission WHERE role_id = '4bb62696-cd6d-4b93-88c9-4b1b6b12ee45'";
    String typePermiso = " SELECT * FROM app_chirpstack_user.permission ";

    JsonArray permissoionR = new JsonArray();
    JsonArray jsonObject = new JsonArray();
    pool
      .getConnection()
      .compose(conn -> conn
        .query(sql)
        .execute()
        .compose(rows -> {
          conn
            .query(typePermiso)
            .execute()
            .compose(row2 -> {
              String permiso = null;

              for (Row rowPer : row2) {
//                if(row.getString("id").equals("")){
//                  System.out.println("");
                //permissoionR.put()
                permiso = rowPer.getString("id");
                permissoionR.add(rowPer.toJson());
              }
              // }
              return Future.succeededFuture(permissoionR);
            });
          //JsonArray jsonObject = new JsonArray();
          for (Row row : rows) {
            jsonObject.add(row.toJson());

            if (row.getString("permission_id").equals("")) {

            }

          }
          return Future.succeededFuture(jsonObject);
        })
        .onSuccess(object -> {
            System.out.println("print object to permission");

            //ystem.out.println(" es permiso:: "+ object.getString(1));
            //object.getString(1)
            System.out.println(object.encodePrettily());
          }
        ));

    return permission;

  }

  public Future<Boolean> hasPermission(PgPool pool, String roleUserId) { // TODO : (userId, permissionId) return boolean
    String permission = null;

    String sqlPermission = " SELECT  t1.id AS permission_id, t1.type_permission AS type_permission, t3.id AS role_id " +
      "FROM app_chirpstack_user.permission t1 " +
      "INNER JOIN app_chirpstack_user.role_permission t2 ON t1.id = t2.permission_id " +
      "INNER JOIN app_chirpstack_user.role t3 ON t2.role_id = t3.id " +
      "WHERE t3.id = '" + roleUserId + "'";


    return
      pool
        .getConnection()
        .flatMap(conn -> conn
            .begin()
            .flatMap(tx -> {
              return
                conn
                  .preparedQuery(sqlPermission)
                  .execute()
                  .flatMap(rows -> {
//                  System.out.println("Is ok ");
                    System.out.println("*********** AL PRIVILEGES ***********");
                    for (Row row : rows) {
                      System.out.println(row.toJson().encodePrettily());
                      System.out.println("TABLA PREVI: " + row.getString("type_permission"));
                      if (row.getString("type_permission").equals("INSERT")) { // GRANT PRIVILEGE
                        System.out.println(" es verdadero: " + true);
                        return Future.succeededFuture(true);
                      } else {
                        System.out.println(" es verdadero: " + false);
                        return Future.succeededFuture(false);
                      }
                    }
                    return Future.succeededFuture();
                  })

                  .onFailure(err -> {
                    System.out.println(err.getMessage());
                  })

                  .eventually(v -> conn.close());
            }).onSuccess(v -> {
              System.out.println(" Imprimimos el de el estado de permiso: ");
              System.out.println("estado: " + v);
            })
            .onFailure(err -> {
              System.out.println(err.getMessage());
            })
        );


 /*   pool
      .getConnection()
      .flatMap(conn -> conn
        .begin()
        .flatMap(tx -> {
          return
            conn
              .query(sqlPermission)
              .execute()
              .map(ps -> ps.iterator().next().getString("permission_type"))
            //.map()

            ;
        })
      );
    return Future.succeededFuture();*/

  }


  public Future<JsonObject> getDetailsRole(PgPool pool, String userId) { // TODO : el metodo esta bien pero no (future not resolved)

    String sql = " SELECT * FROM  app_chirpstack_user.role WHERE id = '" + userId + "' ";

    System.out.println(" ESTE METODO.....");
    System.out.println("METO QUERY: " + sql);

    return
      pool
        .getConnection()
        .flatMap(conn -> conn
          .begin()
          .flatMap(tx -> {
            return
              conn
                .preparedQuery(sql)
                .execute()
                .compose(ar -> {
                  JsonObject Role = new JsonObject();
                  System.out.println("Is ok ");
                  ar.iterator().next().toJson();
                  Role
                    .put("roleId", ar.iterator().next().getUUID("id"))
                    .put("roleName", ar.iterator().next().getString("type_role"))
                    .put("description", ar.iterator().next().getString("description"));
                  System.out.println("IMPRIMIMOS");
                  System.out.println(Role.encodePrettily());

                  return Future.succeededFuture(Role);
                })

                .onFailure(err -> {
                  System.out.println(err.getMessage());
                })

                .eventually(v -> conn.close());
          }).onSuccess(v -> {
            System.out.println(" Imprimimos el de talle de role del usuario: ");
            System.out.println(v.encodePrettily());
          })
          .onFailure(err -> {
            System.out.println(err.getMessage());
          })
        );

  }


  public Future<Integer> getCountDeviceUser(PgPool pool, UUID userId) { // TODO : el metodo esta bien pero no (future not resolved)

    String sql = " SELECT COUNT(*) FROM app_chirpstack_user.device WHERE user_id =  '" + userId + "' ";

    System.out.println("new sql: " + sql);

    return
      pool
        .getConnection()
        .compose(conn -> conn
          .query(sql)
          .execute()
          .map(rows ->

            rows.iterator().next().getInteger(0))

          .eventually(v -> conn.close()))
        .onSuccess(v -> {
          System.out.println("****************************************************");
          System.out.println(" operation count device ok");
          System.out.println("total device: for userID: " + userId + " is: " + v);
          System.out.println("****************************************************");
        }).onFailure(err -> {
        System.out.println(err.getMessage());
      });


  }


  public Future<JsonArray> getListApp(PgPool pool, String id) {
    System.out.println("call funtion ID app");
    JsonArray app = new JsonArray();

    String findApp = "SELECT app_id from app_chirpstack_user.company_app where company_id = '" + id + "'";

    //String sql = sqlFilterCompany(routingContext);

    System.out.println(findApp);

    //System.out.println(findApp);

    pool
      .getConnection()
      .compose(conn ->
        conn
          .query(findApp)
          .execute()
          .compose(result -> {
            System.out.println("count: " + result.rowCount());


            Row row = result.iterator().next();
            app.add(row.getUUID("app_id"));

            System.out.println("LIST: " + row.getUUID("app_id"));

            return Future.succeededFuture(app);
          })
          .recover(err -> {
            PgException x = (PgException) err;
            if (x.getCode() != null) {
              //return Future.failedFuture(sendErrorSql(x.getCode()).encode());
            }
            return Future.failedFuture(err);
          })
          .onSuccess(result -> {
            //routingContext.response().putHeader("content-type", "application/json").end(result.encode());
            System.out.println("SE HA COMPLETADO");
            System.out.println(result.encodePrettily());
          })
          .onFailure(err -> {
            //routingContext.response().end(err.getMessage());
            System.out.println(err.getMessage());
            conn.close();
          }));

    System.out.println("El APP: " + app.encodePrettily());
    //return app;

    return Future.succeededFuture(app);
  }


  public Future<Integer> getTotalApp(PgPool pool, String id) {
    System.out.println("call funtion ID app");
    //JsonArray app = new JsonArray();

    String findApp = "SELECT COUNT(app_id) from app_chirpstack_user.company_app where company_id = '" + id + "'";

    //String sql = sqlFilterCompany(routingContext);

    System.out.println(findApp);

    //System.out.println(findApp);

    pool
      .getConnection()
      .compose(conn ->
        conn
          .query(findApp)
          .execute()
          .compose(result -> {
            System.out.println("count: " + result.rowCount());

            int countApp = result.iterator().next().getInteger(0);
            return Future.succeededFuture(countApp);

          })
          .recover(err -> {
            PgException x = (PgException) err;
            if (x.getCode() != null) {
              //return Future.failedFuture(sendErrorSql(x.getCode()).encode());
            }
            return Future.failedFuture(err);
          })
          .onSuccess(result -> {
            //routingContext.response().putHeader("content-type", "application/json").end(result.encode());
            System.out.println("SE HA COMPLETADO");
            System.out.println(result);
          })
          .onFailure(err -> {
            //routingContext.response().end(err.getMessage());
            System.out.println(err.getMessage());
            conn.close();
          }));

    //System.out.println("El APP: "+app);
    //return app;

    return Future.succeededFuture();
  }


  // =======================  12/07/2021 =============================

  public static String sqlFilterApp(RoutingContext routingContext) {

    final StringBuilder filter = new StringBuilder();
   /* filter.append(" SELECT t1.id as userId,  t3.id as app_id, t3.name  " +
      "FROM app_chirpstack_user.user t1 " +
      " INNER JOIN app_chirpstack_user.company_app t2 ON t1.id = t2.user_id " +
      " INNER JOIN app_chirpstack_user.app t3 ON t2.app_id = t3.id  ");*/

    filter.append(

     "SELECT t1.id, t2.id AS applicationId, t1.name, t1.serial "+
      "FROM app_chirpstack_user.device t1 "+
      "INNER JOIN app_chirpstack_user.app t2 ON t1.app_id = t2.id ");

    MultiMap map = routingContext.request().params();
    Optional.ofNullable(map.get("applicationId")).ifPresent(v -> filter.append(" WHERE t2.id =" + "  '" + v + "' "));
/*    Optional.ofNullable(map.get("dateFrom")).ifPresent(v-> filter.append(" AND received_at BETWEEN " +  "  '"+v+"' "));
    Optional.ofNullable(map.get("dateTo")).ifPresent(v-> filter.append(" AND " +  "  '"+v+"' "));*/

    Optional.ofNullable(map.get("name")).ifPresent(v -> filter.append(" AND  t1.name = " + "  '" + v + "' "));
    Optional.ofNullable(map.get("sortBy")).ifPresent(v -> filter.append(" ORDER BY t1.name " + v));
    Optional.ofNullable(map.get("limit")).ifPresent(v -> filter.append(" LIMIT " + v));


    return filter.toString();
  }


  public static String sqlFilterCompany(RoutingContext routingContext) {

    final StringBuilder filter = new StringBuilder();

    filter.append(
      "SELECT t1.id AS company_id, t1.owner_id AS admin_id, t1.name, t2.id AS supervisor_id " +
        "FROM app_chirpstack_user.company t1 INNER JOIN app_chirpstack_user.admin_company tX " +
        "ON t1.id = tX.company_id " +
        "INNER JOIN app_chirpstack_user.admin t2 ON tX.admin_id = t2.id ");

    MultiMap map = routingContext.request().params();

    Optional.ofNullable(map.get("name")).ifPresent(vd -> filter.append(" WHERE t1.name LIKE " + "  '%" + vd + "%' "));
    Optional.ofNullable(map.get("sortBy")).ifPresent(vd -> filter.append(" ORDER BY t1.name " + vd));
    Optional.ofNullable(map.get("limit")).ifPresent(vd -> filter.append(" LIMIT " + vd));

    return filter.toString();
  }

  public static String sqlFilterAppToCompany(RoutingContext routingContext) {

/*    SELECT t1.id,   t2.created_at as createdAt, t2.updated_at as updatedAt, t1.name  FROM app_chirpstack_user.app t1
    INNER JOIN app_chirpstack_user.company_app t2 on t1.id = t2.app_id
    WHERE t1.id = '5b890946-e0ee-4f1b-8937-4995007ede31' and t2.created_at between '2021-07-21' and '2021-07-22'*/


    MultiMap map = routingContext.request().params();
    String id = routingContext.request().getParam("companyId");
    final StringBuilder filter = new StringBuilder();

    filter.append("SELECT t1.id,   t2.created_at as createdAt, t2.updated_at as updatedAt, " +
      "t1.name  FROM app_chirpstack_user.app t1 " +
      "INNER JOIN app_chirpstack_user.company_app t2 on t1.id = t2.app_id " +
      "WHERE t1.id = '" + id + "'");

    Optional.ofNullable(map.get("dateTo")).ifPresent(v -> filter.append(" AND t2.created_at BETWEEN '" + v +"' "));
    Optional.ofNullable(map.get("dateFrom")).ifPresent(v -> filter.append(" AND '" + v +"' "));

    Optional.ofNullable(map.get("sortBy")).ifPresent(v -> filter.append(" ORDER BY t1.name " + v));
    Optional.ofNullable(map.get("limit")).ifPresent(v -> filter.append(" LIMIT " + v));


    return filter.toString();
  }
}
