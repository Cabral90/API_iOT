package connectionDB;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowStream;


import java.sql.Timestamp;
import java.time.LocalDateTime;

import static javax.swing.UIManager.getString;


//public class SQLRowStreamWrapper implements ReadStream<JsonObject> {
//
//  private final RowStream<Row> wrapper;
//  private Handler<JsonObject> bufferHandler;
//
//  private long length;
//  private long rows;
//
//  public SQLRowStreamWrapper(final RowStream<Row> rowStream) {
//    this.wrapper = rowStream;
//  }
//
//  @Override
//  public ReadStream<JsonObject> exceptionHandler(Handler<Throwable> handler) {
//    this.wrapper.exceptionHandler(handler);
//    return this;
//  }
//
//  @Override
//  public ReadStream<JsonObject> handler(Handler<JsonObject> handler) {
//    this.bufferHandler = handler;
////final JsonObject jsonObject = null;
//    if (this.bufferHandler != null) {
//      this.wrapper.handler(row -> {
//        // final JsonObject jsonObject = row.toJson();
//        // "humedad": 5.46, "voltaje": 0, "Bareria_V": 0, "temperatura": 22.9, "device_status": "off"
//
//        //String fila = row
//        //final Buffer buffer = row.getBuffer("received_at"); // toJson().toBuffer().appendString("\n");
//        String valor = String.valueOf(row.getBuffer("received_at"));
//        System.out.println(valor);
//
//        final JsonObject jsonObject = new JsonObject()
//          .put("received_at", row.getString("received_at"));
////          .put("received_at", row.getValue("received_at"))
////          .put("object", row.getValue("object"));
////          .put("pyload", new JsonObject()
////            .put("humedad", row.getValue(0))
////            .put("voltaje", row.getValue(1))
////            .put("temperatura", row.getValue(2))
////            .put("evice_status", row.getValue(3))
//
//        //    );
//
//        //this.length += buffer.length();
//        // this.rows++;
//       // System.out.println(jsonObject.encodePrettily());
//        //this.bufferHandler.handle(jsonObject);
//      });
//    } else {
//      this.wrapper.handler(null);
//    }
//    return this;
//  }
//
//  @Override
//  public ReadStream<JsonObject> pause() {
//    this.wrapper.pause();
//    return this;
//  }
//
//  @Override
//  public ReadStream<JsonObject> resume() {
//    this.wrapper.resume();
//    return this;
//  }
//
//  @Override
//  public ReadStream<JsonObject> fetch(long l) {
//    this.wrapper.fetch(l);
//    return this;
//  }
//
//  @Override
//  public ReadStream<JsonObject> endHandler(Handler<Void> handler) {
//    this.wrapper.endHandler(handler);
//    return this;
//  }


public class SQLRowStreamWrapper implements ReadStream<JsonObject> {

  private final RowStream<Row> wrapper;
  private Handler<JsonObject> handler;

  public SQLRowStreamWrapper(
    final RowStream<Row> rowRowStream
  ) {
    this.wrapper = rowRowStream;
  }


  @Override
  public ReadStream<JsonObject> exceptionHandler(
    Handler<Throwable> handler
  ) {
    this.wrapper.exceptionHandler(handler);
    return this;
  }

  @Override
  public ReadStream<JsonObject> handler(

    Handler<JsonObject> handler
  ) {
    System.out.println("handler...");
    this.handler = handler;
    if (handler != null) {

      this.wrapper.handler(row -> {
//        int result = row.size();
//        String Srest = String.valueOf(result);

        System.out.println("Datos :" + row.getString("schema"));

//        System.out.println(row.getString("nombre"));
//        JsonObject  jsonObject = new JsonObject().put("nombre",row.getString("nombre") );
        //System.out.println("activo: "+row.getBoolean("activo"));
       // object
        //System.out.println(row.getOffsetDateTime("received_at")); // este si
        // System.out.println(row.getString("object")); // eso si
      //String objec = row.getString("object").

        //System.out.println(row.getJ("object"));
       //final String buffer = row.getBuffer("object").toJson().toString();
//        JsonObject jsonObjectB = new JsonObject(buffer);
        //System.out.printf(jsonObjectB.encodePrettily());
//        System.out.println(buffer);

//
//       JsonObject jsonObject1 = new JsonObject().put("object", new JsonObject().put("humedad", row.getString("object").substring(0)));
//        JsonObject jsonObject = new JsonObject()
//          .put("object", new JsonObject().put("humedad", row.getJsonObject("object"))); // indexOf("voltaje")

//        System.out.println(jsonObject1.encodePrettily());
        //System.out.println(jsonObject.encodePrettily());


   //handler.handle(jsonObject);
      });

    } else {
      this.wrapper.handler(null);
    }

    return this;
  }


  @Override
  public ReadStream<JsonObject> pause() {
    this.wrapper.pause();
    return this;
  }

  @Override
  public ReadStream<JsonObject> resume() {
    this.wrapper.resume();
    return this;
  }

  @Override
  public ReadStream<JsonObject> fetch(long l) {
    this.wrapper.fetch(l);
    return this;
  }

  @Override
  public ReadStream<JsonObject> endHandler(
    Handler<Void> handler
  ) {
    this.wrapper.endHandler(handler);
    return this;
  }

}
