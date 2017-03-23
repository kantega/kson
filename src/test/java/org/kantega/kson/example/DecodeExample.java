package org.kantega.kson.example;

import org.kantega.kson.JsonResult;
import org.kantega.kson.codec.JsonDecoder;
import org.kantega.kson.codec.JsonDecoders;
import org.kantega.kson.json.JsonValue;
import org.kantega.kson.parser.JsonParser;

public class DecodeExample {

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
      "          \"zip\":\"4321\"\n" +
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

    public static class Address {
        final String street;
        final String zip;

        public Address(String street, String zip) {
            this.street = street;
            this.zip = zip;
        }
    }

    static class User {
        final String                name;
        final EncodeExample.Address address;

        User(String name, EncodeExample.Address address) {
            this.name = name;
            this.address = address;
        }
    }

    public static void main(String[] args) {

        final JsonDecoder<Address> adressDecoder =
          JsonDecoders.obj(
            JsonDecoders.field("street", JsonDecoders.stringDecoder),
            JsonDecoders.field("zip", JsonDecoders.stringDecoder),
            Address::new
          );


        JsonResult<JsonValue> json =
          JsonParser.parse(jsonString);

        Address address =
          json.field("model").field("leader").field("address").decode(adressDecoder).orThrow(RuntimeException::new);

        System.out.println(address);
    }

}
