package API;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

public class Usuario {

  public static void main(String[] args) {
    ArrayJson();
  }

  private static void ArrayJson() {

    ArrayList<String> newColor = new ArrayList<>();
    ArrayList<String> colores = new ArrayList<>();
    colores.add("Verde");
    colores.add("Amrillo");
    colores.add("Azul");

    /*final JsonObject json = new JsonObject();
    for (String color : colores) {
      newColor.add(color);

      json
        .put("companyId", "COMPANY_ID")
        .put("title", "TITLE")
        .put("description", "DESCRIPTION")
        .put("schema", new JsonObject().getJsonObject(color))
        .put("size", "20");
      //System.out.println(json.encodePrettily());
    }
*/
   /* json
      //.put("items", new JsonArray().add(colores.toArray()).add(1, "20"));
      .put("items", new JsonArray().add(););
    json.put("size", new JsonObject().put("v", 20));

    System.out.println(json.encodePrettily());*/



    /*final JsonObject json = new JsonObject()
      .put("companyId", "COMPANY_ID")
      .put("title", "TITLE")
      .put("description", "DESCRIPTION")
      .put("schema", new JsonObject().getJsonObject(newColor.toString()))
      .put("size", "20");
    System.out.println(json.encodePrettily());*/

    /*final JsonObject json = new JsonObject()
      .put("companyId", "COMPANY_ID")
      .put("title", "TITLE")
      .put("description", "DESCRIPTION")
      .put("schema", "schema")
      .put("layout", "getLayout()");*/


    JsonObject object1 = new JsonObject()
      .put("Nombre", "Jose")
      .put("Apellido", "Cabral")
      .put("Edad", 20);

    JsonObject object2 = new JsonObject()
      .put("Nombre", "Mark")
      .put("Apellido", "Mendes")
      .put("Edad", 19);

/*    JsonArray jsonArray = new JsonArray()
      .add(object1)
      .add(object2)
      .add(new JsonObject()
        .put("size", 20));*/

    final JsonObject json = new JsonObject();

    json
      //.put("items", new JsonArray().add(colores.toArray()).add(1, "20"));
      .put("items", new JsonArray()
        .add(object1)
        .add(object2)
        .add(new JsonObject().put("size", 20))
      );
    //json.put("size", new JsonObject().put("v", 20));

    System.out.println(json.encodePrettily());
    // System.out.println(jsonArray.encodePrettily());

  }

  private static JsonObject getSchema(
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
}
