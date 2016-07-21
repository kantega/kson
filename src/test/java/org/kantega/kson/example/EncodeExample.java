package org.kantega.kson.example;

import fj.data.List;
import org.kantega.kson.codec.JsonEncoder;
import org.kantega.kson.json.JsonValue;
import org.kantega.kson.parser.JsonWriter;

import static fj.P.p;
import static org.kantega.kson.codec.JsonEncoders.*;

public class EncodeExample {


  public static class Address {
    final String street;
    final String zip;

    public Address(String street, String zip) {
      this.street = street;
      this.zip = zip;
    }
  }

  static class User {
    final String  name;
    final Address address;

    User(String name, Address address) {
      this.name = name;
      this.address = address;
    }
  }

  static class UserModel {
    final User       leader;
    final List<User> users;

    UserModel(User leader, List<User> users) {
      this.leader = leader;
      this.users = users;
    }
  }

  static class TopLevel {
    final UserModel model;

    TopLevel(UserModel model) {
      this.model = model;
    }
  }


  public static void main(String[] args) {

    JsonEncoder<Address> addressJsonEncoder =
        obj(
            field("street", stringEncoder),
            field("zip", stringEncoder),
            addr -> p(addr.street, addr.zip)
        );

    JsonEncoder<User> userEncoder =
        obj(
            field("name", stringEncoder),
            field("address", addressJsonEncoder),
            user -> p(user.name, user.address)
        );

    JsonEncoder<UserModel> userModelJsonEncoder =
        obj(
            field("leader", userEncoder),
            field("users", arrayEncoder(userEncoder)),
            um -> p(um.leader, um.users)
        );

    JsonEncoder<TopLevel> topLevelJsonEncoder =
        obj(
            field("model", userModelJsonEncoder),
            tl -> tl.model
        );


    TopLevel tl =
        new TopLevel(
            new UserModel(
                new User("Ola Normann", new Address("abcstreet", "1234")),
                List.list(
                    new User("Kari Normann", new Address("defstreet", "4321")),
                    new User("Jens Normann", new Address("ghbstreet", "4444")))
            )
        );

    JsonValue json =
        topLevelJsonEncoder.encode(tl);

    System.out.print(JsonWriter.writePretty(json));
  }
}
