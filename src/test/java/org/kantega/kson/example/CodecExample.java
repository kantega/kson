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

  static class UserModel {
    final EncodeExample.User       leader;
    final List<EncodeExample.User> users;

    UserModel(EncodeExample.User leader, List<EncodeExample.User> users) {
      this.leader = leader;
      this.users = users;
    }
  }

  static class TopLevel {
    final EncodeExample.UserModel model;

    TopLevel(EncodeExample.UserModel model) {
      this.model = model;
    }
  }


  //**** Equal instances for verifying that out roundtrip is correct ****

  final static Equal<EncodeExample.Address> addrEq =
      p2Equal(stringEqual, stringEqual).contramap(address -> p(address.street, address.zip));

  final static Equal<EncodeExample.User> userEq =
      p2Equal(stringEqual, addrEq).contramap(user -> p(user.name, user.address));

  final static Equal<EncodeExample.UserModel> umEq =
      p2Equal(userEq, Equal.listEqual(userEq)).contramap(um -> p(um.leader, um.users));

  final static Equal<EncodeExample.TopLevel> tlEq =
      umEq.contramap(tl -> tl.model);

  //**** Codecs ****

  final static JsonCodec<EncodeExample.Address> addressCodec =
      objectCodec(
          field("street", stringCodec),
          field("zip", stringCodec),
          addr -> p(addr.street, addr.zip),
          EncodeExample.Address::new
      );

  final static JsonCodec<EncodeExample.User> userCodec =
      objectCodec(
          field("name", stringCodec),
          field("address", addressCodec),
          user -> p(user.name, user.address),
          EncodeExample.User::new
      );

  final static JsonCodec<EncodeExample.UserModel> userModelCodec =
      objectCodec(
          field("leader", userCodec),
          field("users", arrayCodec(userCodec)),
          um -> p(um.leader, um.users),
          EncodeExample.UserModel::new
      );

  final static JsonCodec<EncodeExample.TopLevel> topLevelJsonCodec =
      objectCodec(
          field("model", userModelCodec),
          tl -> tl.model,
          EncodeExample.TopLevel::new
      );


  //**** Running the example ****

  public static void main(String[] args) {

    //Create an instance of the domain model

    EncodeExample.TopLevel tl =
        new EncodeExample.TopLevel(
            new EncodeExample.UserModel(
                new EncodeExample.User("Ola Normann", new EncodeExample.Address("abcstreet", "1234")),
                List.list(
                    new EncodeExample.User("Kari Normann", new EncodeExample.Address("defstreet", "4321")),
                    new EncodeExample.User("Jens Normann", new EncodeExample.Address("ghbstreet", "4444")))
            )
        );

    //Convert

    JsonValue json =
        topLevelJsonCodec.encode(tl);

    JsonResult<EncodeExample.TopLevel> readResult =
        topLevelJsonCodec.decode(json);

    String output =
        readResult.fold(err -> "Oh no:" + err, read -> "tl equals readTl  = " + tlEq.eq(tl, read));

    System.out.print(output);

  }
}