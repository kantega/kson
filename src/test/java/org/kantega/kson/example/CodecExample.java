package org.kantega.kson.example;

import fj.Equal;
import fj.data.List;
import org.kantega.kson.JsonResult;
import org.kantega.kson.codec.JsonCodec;
import org.kantega.kson.json.JsonValue;

import static fj.Equal.p2Equal;
import static fj.Equal.stringEqual;
import static fj.P.p;
import static org.kantega.kson.codec.JsonCodecs.*;

public class CodecExample {

    //**** Domain model ****

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


    //**** Equal instances for verifying that out roundtrip is correct ****

    final static Equal<Address> addrEq =
      p2Equal(stringEqual, stringEqual).contramap(address -> p(address.street, address.zip));

    final static Equal<User> userEq =
      p2Equal(stringEqual.<Name>contramap(name -> name.value), addrEq).contramap(user -> p(user.name, user.address));

    final static Equal<Department> departmentEqual =
      p2Equal(userEq, Equal.listEqual(userEq)).contramap(um -> p(um.boss, um.users));


    //**** Codecs ****

    final static JsonCodec<Name> nameCodec =
      stringCodec.xmap(name -> name.value, Name::new);

    final static JsonCodec<Address> addressCodec =
      objectCodec(
        field("street", stringCodec),
        field("zip", stringCodec),
        addr -> p(addr.street, addr.zip),
        Address::new
      );

    final static JsonCodec<User> userCodec =
      objectCodec(
        field("name", nameCodec),
        field("address", addressCodec),
        user -> p(user.name, user.address),
        User::new
      );

    final static JsonCodec<Department> departmentJsonCodec =
      objectCodec(
        field("boss", userCodec),
        field("users", arrayCodec(userCodec)),
        um -> p(um.boss, um.users),
        Department::new
      );


    //**** Running the example ****

    public static void main(String[] args) {

        //Create an instance of the domain model

        Department tl =
          new Department(
            new User(new Name("Ola Normann"), new Address("abcstreet", "1234")),
            List.list(
              new User(new Name("Kari Normann"), new Address("defstreet", "4321")),
              new User(new Name("Jens Normann"), new Address("ghbstreet", "4444")))

          );

        //Convert

        JsonValue json =
          departmentJsonCodec.encode(tl);

        JsonResult<Department> readResult =
          departmentJsonCodec.decode(json);

        String output =
          readResult.fold(err -> "Oh no:" + err, read -> "tl equals readTl  = " + departmentEqual.eq(tl, read));

        System.out.print(output);

    }
}