package API;

import connectionDB.SQLRowStreamWrapper;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;


public class ValidateData {
  private static final String SUPERADMIN = "23f0c751-d678-4c08-aa6d-0cee5cd5bae0";
  private static final String ADMIN = "506958f5-7c1f-4320-85af-2a5e0533706e";
  private static final String USER = "aaf86fd3-5ce8-4c60-aae7-36ba4be420da";

  public static void main(String[] args) {
/*    System.out.println(encriptyPass("hola"));

    System.out.println("Pass1 1: "+
    ByteToHex( encriptyPass("hola")));

    System.out.println(" Pass2: "+encriptyPass2("hola"));*/

    System.out.println(setRolType("superAdmin"));

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

  public static String getRole(UUID role) {

    String value = String.valueOf(role);

    String typeRole = null;

    if (SUPERADMIN.equals(value)) {
      typeRole = "superAdmin";
    }
    if (ADMIN.equals(value)) {
      typeRole = "admin";
    }
    if (USER.equals(value)) {
      typeRole = "user";
    }
    return typeRole;
  }


  public static UUID setRolType(String role) { //
    //String role = "superAdmin";
    UUID roleId = UUID.fromString("aaf86fd3-5ce8-4c60-aae7-36ba4be420da");

    if (role.equals("superAdmin")) {
      roleId = UUID.fromString(SUPERADMIN);

    }

    if (role.equals("admin")) {
      roleId = UUID.fromString(ADMIN);

    }

    if (role.equals("user")) {
      roleId = UUID.fromString(USER);
    }
    return roleId;
  }

}
