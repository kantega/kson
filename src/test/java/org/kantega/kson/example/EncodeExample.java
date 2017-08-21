package org.kantega.kson.example;

import fj.data.List;
import org.kantega.kson.codec.JsonEncoder;
import org.kantega.kson.json.JsonValue;
import org.kantega.kson.parser.JsonWriter;

import static fj.P.p;
import static org.kantega.kson.codec.JsonEncoders.*;

public class EncodeExample {


    static class Address {
        final String street;
        final String zip;

        public Address(String street, String zip) {
            this.street = street;
            this.zip = zip;
        }
    }

    static class Name {
        final String value;

        Name(String value) {
            this.value = value;
        }
    }

    static class User {
        final Name    name;
        final Address address;

        User(Name name, Address address) {
            this.name = name;
            this.address = address;
        }
    }

    static class Department {
        final User       boss;
        final List<User> users;

        Department(User boss, List<User> users) {
            this.boss = boss;
            this.users = users;
        }
    }

    static final JsonEncoder<Name> nameJsonEncoder =
      stringEncoder.contramap(name -> name.value);


    static final JsonEncoder<Address> addressJsonEncoder =
      obj(
        field("street", stringEncoder),
        field("zip", stringEncoder),
        addr -> p(addr.street, addr.zip)
      );

    static final JsonEncoder<User> userEncoder =
      obj(
        field("name", nameJsonEncoder),
        field("address", addressJsonEncoder),
        user -> p(user.name, user.address)
      );

    static final JsonEncoder<Department> departmentJsonEncoder =
      obj(
        field("leader", userEncoder),
        field("users", arrayEncoder(userEncoder)),
        um -> p(um.boss, um.users)
      );

    public static void main(String[] args) {


        Department tl =
          new Department(
            new User(new Name("Ola Normann"), new Address("abcstreet", "1234")),
            List.list(
              new User(new Name("Kari Normann"), new Address("defstreet", "4321")),
              new User(new Name("Jens Normann"), new Address("ghbstreet", "4444")))
          );

        JsonValue json =
          departmentJsonEncoder.encode(tl);

        System.out.print(JsonWriter.writePretty(json));
    }
}
