# KSON &mdash; minimal JSON parsing, writing and converting

KSON is a safe and minimal library for parsing text into JSON, writing JSON, and converting JSON to and from your domain types.

By safe we mean no side effects (exceptions, mutations), typesafe everywhere and compile time errors where possible. 
All objects are immutable and thread safe. (Also the fields of all 
objects this library produces are immutable and thread safe,
we even encourage the use of object fields, they are all final) The API will not let you perform operations that will
leave your objects in an undefined or undesired state. If you follow the types, it works!

The Kson library depends on functionaljava and functionaljava-quickcheck. The parser is implemented by using the parser 
from [minimal-json](https://github.com/ralfstx/minimal-json).


## Building, writing and parsing JSON

Let's begin by constructing some JSON values with the ```JsonValues``` class. By importing the static methods of the class, 
you get a nice and readable DSL for constructing 
JSON values:
```java
public class WriteAndParseJsonExample {


    public static void main(String[] args) {

        //Constructing
        JsonValue userjson =
          jObj(
            field("name", "Ole Normann"),
            field("address", jObj(
              field("street", "Kongensgate 3"),
              field("zip", "1234") //Observe the presence of this field
            ))
          );

        JsonValue userWithoutZipcode =
          jObj(
            field("name", "Kari Normann"),
            field("address", jObj(
              field("street", "Kongensgate 3")
              //Behold the missing zipcode field
            ))
          );

        //Values are reusable and can be nested.
        JsonValue team =
          jObj(
            field("team",
              jObj(
                field("members", jArray(userjson, userWithoutZipcode)),
                field("name", "A-team")
              )));


        //Write to a string
        String jsonAsString =
          JsonWriter.writePretty(team);

        //Parse a string
        JsonResult<JsonValue> json =
          JsonParser.parse(jsonAsString);


        //Find the zipcode of the first user
        String zip1 =
          json.field("team").field("members").index(0).field("address").fieldAsString("zip", "unknown");
        //Zip 1 is "1234"

        //If a field is not present, or a conversion fails, the navigations aborts, and the default value is used.
        String zip2 =
          json.field("team").field("members").index(1).field("address").fieldAsString("zip", "unknown");
        //zip2 is "unknown" since the field is not present.


        //You can also keep the JsonResult directly
        JsonResult<JsonValue> failedResult =
          json.field("temas"); //Contains a failure

        JsonResult<JsonValue> teamsResult =
          json.field("team"); //Contains "A-team"


    }
}
```

The example is pretty self-explanatory. 
We have chosen not to provide any shortcuts, and no under-the-hood conversion. If you would like the convenience of constructing 
JsonString objects automatically, you can provide your own DSL. 
(In fact, that is why we use static methods and static imports for the DSL, it makes it very easy to extend the DSL with your own combinators)

In order to serialize the JSON object you use the ```JsonWriter.write(JsonValue)``` or ```JsonWriter.writePretty(JsonValue)```. 
You probably guessed that
writePretty outputs prettified JSON. (It's not actually pretty, just indented. Writing really pretty JSON is impossible) 



Observe that the parser yields a `JsonResult<JsonValue>`. A `JsonResult` is actually just a wrapper around ```Validation<String, JsonValue>```. Of you are unfamiliar with the validation type you 
can google it, there are plenty of articles 
 that introduce the concept. Basically a Validation holds either a failure value (A ```String``` explaining what went 
 wrong in this case) 
 or a success value, you have to use some of its convenience methods or fold it to find out.


The JsonResult type can be used for navigating down and extracting values from the contained json. If it doesnt 





## Converting to your domain model using the safest (and simplest actually) way.

You say that nothing is simpler than the automatic conversion done by jackson? Then I am convinced that your 
application is either _stringly typed_ or infected with annotation hell. Or you use stringly typed DTOs to provide
for the mapping to and from your domain model. Adding a converter for you Money and Measurements libraries is also trivial I presume?
 
Let's see how simple this can be with Kson. We start by looking at decoding from json:
```java
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
        final Address address;

        User(String name, Address address) {
            this.name = name;
            this.address = address;
        }
    }

    public static void main(String[] args) {

        final JsonDecoder<Address> adressDecoder =
          obj(
            field("street", stringDecoder),
            field("zip", stringDecoder.ensure(z -> z.length() < 5)), //You can add constraints right here in the converter
            Address::new
          );


        JsonResult<JsonValue> json =
          JsonParser.parse(jsonString);

        Address address =
          json.field("model").field("leader").field("address").decode(adressDecoder).orThrow(RuntimeException::new);

        System.out.println(address);

        JsonResult<Address> userAddress =
          json.field("model").field("users").index(0).field("address").decode(adressDecoder);

        System.out.println(userAddress);
    }

}
```
You simply write decoders using the basic buildingBlocks in `JsonDecoders`. The can be nested and mixed as you like. If it compiles it works.
The pattern is simple, follow the same structure as the json object, and define the fields and types of the fields as arguments to the obj (or array) functions.
The last argument is the constructor that constructs you domain object. If you want to translate a json value to a domain value, you use the contramap
method on the `JsonDecoder`. For example if you would like to decode a field into a date you could
```
JsonDecoder<LocalDate> localDateDecoder = stringDecoder.contramap(str->LocalDate.parse(str))
```
Which you can reuse as you like. Its normal to define all your decoders in one place in your program as a library of decoders for you common datatypes.

If you find yourself encoding and decoding to and from you domain objects, you can define codecs instead. They follow the exact same pattern, but you have
to provide a deconstructor for your domain type. A deconstructor yields all the fields of your objects as a tuple.
Then we write our codecs using the DSL defined in `JsonCodecs`. We use the _objectCodec_ method in conjunction with the _field_ method to define our
conversion. It is pretty self explanatory. 

The `Equal` instances are used for comparison. They are a type-safe version of `equal()`, and quicker to write too.

Note that the _objectCodec_ functions always takes in functions as the last two arguments. The first function is a _deconstructor_, it tells the converter how to extract
all the fields from your domain object. The last one is a _constructor_. When you map one to one, you can just pass inn a reference to you objects constructor directly
like in the example.

Let's make codecs for our domain model, and save them as fields in out class.

```java
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
```

This outputs `tl equals readTl  = true`.

Note that the codecs are values. They can be passed safely around in your program, as they are immutable. You can store 
them as static instances in your classes for reuse, and combine them how you like. (Because they are immutable)
Did I mention everything is immutable? Nothing unexpected will happen here. Ever. We promise.

## Lenses, for when you know little about the JSON structure, or you want to manipulate it directly
A Lens is an object that knows how to extract a value
from an object, and to set a value in an object - in a immutable manner. Think of it as a setter
and getter pair, but not attached to the object it sets and gets from. This gives us the advantage 
of combining Lenses together as we wish, and create arbitrary combinations and nested goodness
without having the JSON object at hand. 
This sounds like mumbo jumbo for most Java-developers, so let's look at an example:

```java
public class LensExample {

  public static void main(String[] args) {

    JsonLens<JsonValue, String> zipLens =
        JsonLenses.select("address").select("zip").asString();


    
    JsonValue userjson =
            jObj(
                field("name", "Ole Normann"),
                field("address", jObj(
                    field("street", "Kongensgate 3"),
                    field("zip", "1234") //Observe the presence of this field
                ))
            );
    
        JsonValue invalidUserJson =
            jObj(
                field("name", "Kari Normann"),
                field("address", jObj(
                    field("street", "Kongensgate 3")
                    //Behold the missing zipcode field
                ))
            );
    
        JsonValue userModel =
            jObj(
                field("model",
                    jObj(
                        field("users", jArray(userjson, invalidUserJson)),
                        field("leader", userjson)
                    )));
    
    
        JsonValueLens modelLens =
            JsonLenses.field("model");
    
        JsonValueLens leaderLens =
            JsonLenses.field("leader");
    
        JsonValueLens usersLens =
            JsonLenses.field("users");
    
        //Replace leader
        JsonValue updatedLeaderModel =
            modelLens.then(leaderLens).setF(userModel, invalidUserJson);
    
    
        //Update zipcode of last user in array
        JsonValue modelWithZips =
            modelLens.then(usersLens).asArray().modF(updatedLeaderModel, list -> list.map(zipLens.setF("1234")));
    
        System.out.println(JsonWriter.writePretty(modelWithZips));
    
  }
}
```
Not too shabby. But why bother with lenses? We could just as well write
` userjson.field("address").field("zip").orElse("No zipcode in object)`.
There are three advantages of using lenses over getters/setters:
 * They are values, you can use them like any other value, pass them as arguments and so forth.
 * They compose, so you can mix them as you like
 * They can update inner values in an immutable fashion
 
(We leave as an exercise for the reader to prove that these points are indeed advantages. Please make a deeply nested
object and update a leaf value in a safe way)

Let's try a less trivial example with an update:
```java
public class LensExample {

  public static void main(String[] args) {

    JsonLens<JsonValue, String> zipLens =
        JsonLenses.select("address").select("zip").asString();


   JsonValue userjson =
               jObj(
                   field("name", "Ole Normann"),
                   field("address", jObj(
                       field("street", "Kongensgate 3"),
                       field("zip", "1234") //Observe the presence of this field
                   ))
               );
       
           JsonValue invalidUserJson =
               jObj(
                   field("name", "Kari Normann"),
                   field("address", jObj(
                       field("street", "Kongensgate 3")
                       //Behold the missing zipcode field
                   ))
               );
       
           JsonValue userModel =
               jObj(
                   field("model",
                       jObj(
                           field("users", jArray(userjson, invalidUserJson)),
                           field("leader", userjson)
                       )));


    JsonValueLens modelLens =
        JsonLenses.select("model");

    JsonValueLens leaderLens =
        JsonLenses.select("leader");

    JsonValueLens usersLens =
        JsonLenses.select("users");

    //Replace leader
    JsonValue updatedLeaderModel =
        modelLens.then(leaderLens).setF(userModel, invalidUserJson);


    //Update zipcode of last user in array
    JsonValue modelWithZips =
        modelLens.then(usersLens).asArray().modF(updatedLeaderModel, list -> list.map(zipLens.setF("1234")));

    System.out.println(JsonWriter.writePretty(modelWithZips));
  }
}
```

The example prints:
```
{
  "model":{
    "leader":{
      "address":{
        "street":"Kongensgate 3"
      },
      "name":"Kari Normann"
    },
    "users":[
      {
        "address":{
          "street":"Kongensgate 3",
          "zip":"1234"
        },
        "name":"Ole Normann"
      },
      {
        "address":{
          "street":"Kongensgate 3",
          "zip":"1234"
        },
        "name":"Kari Normann"
      }
    ]
  }
}
```
Which is what we could expect: The "leader" field has been replaced and
the zip code of the users in the list has been updated.

##Behind the scenes
####or how to build an excellent library by combining many little parts
Building this library was actually an exercise in teaching functional programming. If it served its purpose
awaits to be seen.

Our main fascination for functional programming arose when we discovered that functionally written programs
have certain advantages over idiomatic Java (commonly referred to as "object oriented programming"):

 * The programs became smaller - less code to write and maintain
 * The programs became safer - fever exceptions, bugs and mistakes.
 * We found the functional paradigm was easier to reason about, and hence the architecture and design of our
programs were improved (I know, it is subjective. But try for yourself)
 
By writing this library we wanted to demonstrate how easy it is to build a solid library &mdash; which beats all the JSON libraries I know of
regarding all aspects except runtime speed - by using only a limited set of building blocks.
Now, the term " easy" is of course a bit misleading. It's easy for us, but if you are not used
to functions, then it might take some time to get used to. But if you take your time
a new world will reveal itself before your eyes. I absolutely, 100 percent, positively guarantee it! <sup>1</sup> 

<sup>1 Not a guarantee</sup>

This library is built up by four distinct parts. Used by the others, and core to the library are the _data-objects_ 
(`org.kantega.kson.json`) that represent
a JSON structure. To convert to and from character streams / strings one can use the _parser_ part. To navigate in the JSON structure you
can employ the _lens_ part. And finally you can map JSON to your domain model by using the _codec_ part.
 
Here we explain how we built the codec. Although it is on the "highest level", we feel that it is a nice place to 
start learning.

In idiomatic Java (henceforth referred to as _object oriented programming_, or OO, even though many Smalltalkers would 
protest heavily upon that
label) one usually thinks of a program of a graph of objects that collaborate. A network of objects send messages
or data to each other by calling each others functions. The object graph is often stored using some mapping that maps to 
some underlying store. When we act on the application, some object graph is restored, messages are submitted to the objects,
and the objects are stored again.

In the Functional paradigm however, we think of a program that converts input to output. If we have persistent state, we
add that to the equation: some input plus some state is converted to some output and a new state. Since this library does not
concern itself with state, we can forget about that all together: We can work completely stateless.


###Basics
To build a codec we need to main parts: something that encodes our model, and something
that decodes it. So let's start by splitting our problem into two functions: An encoder which converts to JSON, 
and a decoder which converts from JSON. These two functions can be represented as functional interfaces.

The decoder can look like this
```java
public interface JsonDecoder<A> extends F<JsonValue,JsonResult<A>> {

  default JsonResult<A> f(JsonValue v){
    return decode(v);
  }

  JsonResult<A> decode(JsonValue v);
  
}
```
 
And the encoder can look like this:

```java
public interface JsonEncoder<A> extends F<A, JsonValue> {

  @Override
  default JsonValue f(A a) {
    return encode(a);
  }

  JsonValue encode(A t);

}
```

Note that they both extend the ```F``` type. ```F<A,B>``` is a class from the library _functionaljava_ 
(from now on _fj_), and represents
a function from _A_ to _B_. This means that the functional method _f()_ on ```F``` accepts any object of type _A_, and returns (yields) an object of
 type _B_. (Just like in math f(x) = x*2 is a function that accepts all real numbers, and returns a real number)
Take note that the function does not change anything in the surroundings, it has no side effects. That means that
 calling the function with the same argument will always yield the same results.
We extend ```F``` for two reasons: It makes it clear we think of the constructs as functions, but it also 
gives us the benefits of using the functionaljava library, which has a lot of functionality around functions.

Now we have defined our encoder and decoder functions, but we have no implementations of them yet. Let's change that
by implementing the simplest possible encoder, an encoder for strings:

```java
public class JsonEncoders {

  public static final JsonEncoder<String> stringEncoder =
      JsonValues::jString;

}
```

Wow, that was simple. Not everything looks complicated using FP. Let's complete the DSL with all the basic 
JSON types:

```java
public class JsonEncoders {

  public static final JsonEncoder<String> stringEncoder =
      JsonValues::jString;

  public static final JsonEncoder<BigDecimal> bigDecimalEncoder =
      JsonValues::jNum;

  public static final JsonEncoder<Boolean> boolEncoder =
      JsonValues::jBool;
  
}
```

### Optional values (often called _null_)
But what about _null_? In FP, we don't use _null_, since all functions must return something. That makes your
program safer too, since _null_ is neither type-safe nor does it carry semantic meaning. You cannot look at _null_ 
and understand its meaning without looking at the context, for example the field or variable it is assigned to.

But how do we represent the case when we have no value for field? Turns out Java added a type to represent that:
`Optional<T>`, which is interpreted as "The object can contain zero or one element of type T, but you 
do not know". We use the type `Option<A>` from fj, but for consistence. They are pretty equivalent.

In JSON a missing value is represented by the keyword _null_. But when we encode, we know the expected type, so we
can use an ```Option<A>``` to hold the unknown state (_null_ or value), but how do we convert?
Let's examine the type of the function first:

We want to convert a `JsonValue` that can be either _null_ or some primitive value, into an `Option<A>` where 
```A``` is the type of the value. 

If we know the type is either _null_ or `String` we could write 
`JsonEncoder<Option<String>>`, but that only works for strings. We want to be able to make all types
optional: `JsonEncoder<Option<A>>`.
Let's implement this:

```
  public static <A> JsonEncoder<Option<A>> optionEncoder(){
       return maybeValue -> ???;
  }  
```

So what do we actually return? It turns out we need to know how we encode `A` before we can encode `Option<A>`, so we supply
that:

```
  public static <A> JsonEncoder<Option<A>> optionEncoder(JsonEncoder<A> aEncoder){
    return maybeValue -> maybeValue.option(JsonValues.jNull(), aEncoder::encode);
  }
```

_maybeValue.option(onEmpty,onDefined)_ is the _fold_ of the Option type. The first argument
is used in case the Option is empty (None), and the second argument is a function that transforms the 
contained value to an object of the target type.

That wasn't that hard, plus we learned about fold, and got a feel for how we can can transform an encoder for a type
into a transformer for optional values of that type.

###Arrays
Let's dig deeper  and go for a non-primitive JSON types, for example array:
We want to transform a `List` with `A` - `List<A>` to a JSON array. It's just as with option, if we can encode
`A`, we can encode a `List` of `A`.

```
  public static <A> JsonEncoder<List<A>> arrayEncoder(JsonEncoder<A> aEncoder) {
    return list -> JsonValues.jArray(list.map(aEncoder::encode));
  }
```

Now, in JSON, an array can interchange its the types of its element, meaning you can mix numbers, string objects etc.
In a typed language (like Java), that sort of structure is called an HList (for Heterogenous List). You basically
have to provide a converter for all the elements which makes it a bit more complicated. Let's save that for later.

###Objects
The crux of any conversion is of course nested objects. Since JSON is a tree, we only care about directed
acyclic object-graphs. (If there is a cycle, you will get a stack overflow or an infinite loop)

Again, as with arrays, it seems intuitive that if we know how to encode the contents of an object, we can encode
the object itself. But since each field in the object can have its own type, we have to supply the
encoder with encoders for all the fields, plus the field names. Since the fieldname-fieldvalue pair has no
JSON representation, we use the fj tuple type (`P2`) to represent the pair: `P2<String,JsonValue>`.
An object basically consists of a list of pairs, so we provide a list of fieldnames and converters to the 
object converter.

First take:
```
  public static <X> JsonEncoder<X> objectEncoder(P2<String,JsonEncoder<?>>... fieldEncoders){
    return x->???;
  }
```
It's obvious that we are missing a couple of key points here. First &mdash; we need a way do extract the values from
our object, and second &mdash; we need to know the types of the fields. Let's split that into two problems, and solve the
two cases separately, and the merge them together (also a nice trait of FP)

An object of a type can be represented as a tuple of types of the fields of the object. 
A User
```
static class User{
    final String       name;
    final int          age;
    final List<String> favourites;

    User(String name, int age, List<String> favourites) {
      this.name = name;
      this.age = age;
      this.favourites = favourites;
    }
  }

```

can be represented as a tuple 3 like this `P3<String,Integer,List<String>`, it contains the same information
except the semantic names of the fields.

Let's write an encoder for a `P3`
```
  public static <A, B, C> JsonEncoder<P3<A, B, C>> p3Encoder(
      P2<String, JsonEncoder<A>> a,
      P2<String, JsonEncoder<B>> b,
      P2<String, JsonEncoder<C>> c) {
    return p3 ->
        JsonValues.jObj(
            P.p(a._1(), a._2().encode(p3._1())),
            P.p(b._1(), b._2().encode(p3._2())),
            P.p(c._1(), c._2().encode(p3._3()))
        );
  }
```
That looks like a lot of work. Too many underscores numbers and similar looking statements, this is too error prone
to be safe. Let's fix that by creating a type `FieldEncoder<A>` that puts fields-value pairs into an object.
```
public interface FieldEncoder<A> {

    JsonObject apply(JsonObject obj, A a);

  }
```
And then let's make a constructor (=static factory function) for values of that type.

```
  public static <A> FieldEncoder<A> field(String name, JsonEncoder<A> a) {
      return (obj, va) -> obj.withField(name, a.encode(va));
  }
```

and change the `P3` encoder
```
  public static <A, B, C> JsonEncoder<P3<A, B, C>> obj(
        FieldEncoder<A> a,
        FieldEncoder<B> b,
        FieldEncoder<C> c) {
      return t ->
          and(a, and(b, c)).apply(JsonObject.empty, p(t._1(), p(t._2(), t._3())));
    }
    
    //We added the and function to make it easier for us to add two FieldEncoders.
    static <A, B> FieldEncoder<P2<A, B>> and(FieldEncoder<A> fa, FieldEncoder<B> fb) {
        return (obj, t) -> fb.apply(fa.apply(obj, t._1()), t._2());
      }
```


A little nicer. Observe that only typed information changes between the calls to the FieldEncoder. The compiler will
tell us if we miss our mark. But we need to be able to convert objects with fieldcount ≠ 3, so we will
have to manually craft conversions from P2 up to P8 (the highest tuple in fj).

The function of arity 8 looks like this:
```
 public static <A, B, C, D, E, FF,G,H> JsonEncoder<P8<A, B, C, D, E, FF,G,H>> obj(
      FieldEncoder<A> a,
      FieldEncoder<B> b,
      FieldEncoder<C> c,
      FieldEncoder<D> d,
      FieldEncoder<E> e,
      FieldEncoder<FF> f,
      FieldEncoder<G> g,
      FieldEncoder<H> h) {
    return t ->
        and(a, and(b, and(c, and(d, and(e, and(f,and(g,h)))))))
            .apply(JsonObject.empty, p(t._1(), p(t._2(), p(t._3(), p(t._4(), p(t._5(), p(t._6(),p(t._7(),t._8()))))))));
  }
```

That is a mouthful of type information, but we only have to write it once, and it is safe. Maybe we will come back later 
and try to find a more elegant way to encode JSON objects, but probably not since it works and is already written.

But that tupletizing really draws a lot of attention when reading the code (kinda the same effect as a lava lamp). We factor 
that part out into a utility function _expand_ that converts a tuple into pairs (a,b,c,d,e,f,g) -> (a,(b,(c,(e,(f,g))))) for us.
```
public static <A, B, C, D, E, FF, G, H> JsonEncoder<P8<A, B, C, D, E, FF, G, H>> obj(
      FieldEncoder<A> a,
      FieldEncoder<B> b,
      FieldEncoder<C> c,
      FieldEncoder<D> d,
      FieldEncoder<E> e,
      FieldEncoder<FF> f,
      FieldEncoder<G> g,
      FieldEncoder<H> h) {
    return t ->
        and(a, and(b, and(c, and(d, and(e, and(f, and(g, h)))))))
            .apply(JsonObject.empty, expand(t));
  }
```
Not too shabby.




Let's see what this will look like when we use our encoder in in an example.
We want to create an encoder for our `User` objects. The observant reader however will have noticed by now that the field _age_
in the User class is of type _int_, but we don't have an encoder for ints. We address that first by _contramap_ ing the 
bigDecimalDecoder
```
public static final JsonEncoder<Integer> integerEncoder =
      bigDecimalEncoder.contramap(BigDecimal::valueOf);
```
_Contramap_ is the opposite of map. We change the "inner type" of the decoder to Integer by supplying it with a 
function from Integer to BigDecimal. When you use the encoder, you give it an Integer, and it applies the function you give
it in the contramap before it passes it down to the BigDecimal encoder. You pass it an Integer, but it is written to JSON as
a BigDecimal.


```java
public class EncodeExample {

  public static class User {
    public final String       name;
    public final int          age;
    public final List<String> favourites;

    User(String name, int age, List<String> favourites) {
      this.name = name;
      this.age = age;
      this.favourites = favourites;
    }
  }


  public static void main(String[] args) {

    JsonEncoder<User> userEncoder =
        obj(
            field("name", stringEncoder),
            field("age",integerEncoder),
            field("favourites",arrayEncoder(stringEncoder))
        ).contramap(user-> p(user.name,user.age,user.favourites));

  }
}
```
You can see that we also use _contramap_ to convert from our `User` to a tuple3 (`P3`). It looks ugly though, and we don't
want to write _contramap_ all over our code, so let's embed that into our DSL. 

Our highest arity converter for objects now looks like this
```
 public static <A, B, C, D, E, FF, G, H,X> JsonEncoder<X> obj(
      FieldEncoder<A> a,
      FieldEncoder<B> b,
      FieldEncoder<C> c,
      FieldEncoder<D> d,
      FieldEncoder<E> e,
      FieldEncoder<FF> ff,
      FieldEncoder<G> g,
      FieldEncoder<H> h,
      F<X,P8<A,B,C,D,E,FF,G,H>> f) {
    return obj(a,b,c,d,e,ff,g,h).contramap(f);
  }
```

its just calls the mapper for the tupled value and contramaps with f.

Now we can tidy up our example a bit
```
JsonEncoder<User> userEncoder =
        obj(
            field("name", stringEncoder),
            field("age", integerEncoder),
            field("favourites", arrayEncoder(stringEncoder)),
            user-> p(user.name,user.age,user.favourites)
        );
```
That looks really neat.


Let's use the model from the Lens example, and implement that using domain objects
```java
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
```

It prints 
```
{
  "model":{
    "leader":{
      "address":{
        "street":"abcstreet",
        "zip":"1234"
      },
      "name":"Ola Normann"
    },
    "users":[
      {
        "address":{
          "street":"defstreet",
          "zip":"4321"
        },
        "name":"Kari Normann"
      },
      {
        "address":{
          "street":"ghbstreet",
          "zip":"4444"
        },
        "name":"Jens Normann"
      }
    ]
  }
}
```

It works!

By using the basic encoders for numbers,booleans,null,arrays and objects we now can 
map any domain object of any complexity to JSON using contramap. But we are only halfway there,
we need to decode to. Unfortunately that is a little bit harder, since we need to 
handle the case when the JSON tree does not have the shape we need to build our domain model. But with 
the knowledge you have gained you will be able to figure out how to proceed.
Remember: Think of how you transform your data, and the type system will guide you
along the way. (Hint: start with the function that decodes to data, it would probably
look something like `JsonValue -> JsonResult<A>`. Look in the codec package for guidance)

This concludes the behind the scenes part. We hope you learned something by reading it, or at least give us feedback
about typos, errors and how to improve.
Sincerely atle.prange@kantega.no and edvard.karlsen@kantega.no





