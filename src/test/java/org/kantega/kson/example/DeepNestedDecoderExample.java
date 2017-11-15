package org.kantega.kson.example;

import fj.data.List;
import org.kantega.kson.JsonResult;
import org.kantega.kson.codec.JsonDecoder;
import org.kantega.kson.parser.JsonParser;

import static org.kantega.kson.codec.JsonDecoders.*;

public class DeepNestedDecoderExample {

  final static String jsonString = "{\n" +
    "  \"model\":{\n" +
    "    \"leader\":{\n" +
    "      \"address\":{\n" +
    "        \"street\":\"abcstreet\",\n" +
    "        \"zip\":\"1234\"\n" +
    "      },\n" +
    "      \"name\":\"Ola Normann\"\n" +
    "    },\n" +
    "    \"users\":[\n" +
    "      {\n" +
    "        \"address\":{\n" +
    "          \"street\":\"defstreet\",\n" +
    "          \"zip\":\"43210\"\n" +
    "        },\n" +
    "        \"name\":\"Kari Normann\"\n" +
    "      },\n" +
    "      {\n" +
    "        \"address\":{\n" +
    "          \"street\":\"ghbstreet\",\n" +
    "          \"zip\":\"4444\"\n" +
    "        },\n" +
    "        \"name\":\"Jens Normann\"\n" +
    "      }\n" +
    "    ]\n" +
    "  }\n" +
    "}";

  public static class DomainObject {
    final String       leadername;
    final List<String> zips;


    public DomainObject(String leadername, List<String> zips) {
      this.leadername = leadername;
      this.zips = zips;
    }
  }


  public static void main(String[] args) {

    final JsonDecoder<DomainObject> doDecoder =
      obj(
        field("model",
          obj(
            field("leader", obj(field("name", stringDecoder))),
            field("users", arrayDecoder(obj(field("address", obj(field("zip", stringDecoder))))))))
      ).map(pair -> new DomainObject(pair._1(), pair._2()));


    JsonResult<DomainObject> json =
      JsonParser.parse(jsonString).decode(doDecoder);

    System.out.println(json);


  }
}
