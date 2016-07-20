# KSON - minimal JSON parsing, writing and converting

KSON is a safe and minimal library for parsing text into json, writing json, and converting json to and from your domain types.

By safe we mean no sideeffects (exceptions, mutations). All objects are immutable and threadsafe. (Also the fields of all 
objects this library produces are immutable and threadsafe,
we even encourage the use of object fields, they are all final) The API will not let you perform operations that will
leave your objects in an undefined or undesired state. If you follow the types, it works!

The Kson library depends on functionaljava and functionaljava-quickcheck. The parser is implemented by using the parser 
combinators from the functionaljava
library. Read more on [functionaljava.org](https://functionaljava.org/)

Beware though: This library is slow. Very slow. It uses over 100 times more time to parse a json document
than other parsers. Does it matter to you? Probably not, as most likely your database or network is the bottneck.
However, if you _do_ discover that indeed the json parsing is the bottlneck of your application, please
let us know an dwe will spend some time optimizing.
Or use one of the unsafe libraries out there. You probably know them already.


##Building, writing and parsing json

Lets begin by constructing some json values with the ```JsonValues``` class. By importing the static methods of the class, 
you get a nice and readable dsl for constructing 
json values:
```java
public class ParseExample {
  public static void main(String[] args) {

    final JsonObject json =
        jObj(
            field("name", jString("Ola Nordmann")),
            field("age", jNum(28)),
            field("favourites", jArray(jString("red"), jString("blue"), jString("purple")))
        );
  }
}
```

The example is pretty self-explanatory, but notice that you explicitly have to define the type of the json value, for example "jString". 
We have chosen not to provide any shortcuts, and no under the hood conversion. If you would like the convenience of constructing 
JsonString objects automatically, you can provide your own dsl. 
(In fact, that is why we use static methods and static imports for the dsl, it makes it very easy to extend the dsl with your own combinators)

In order to serialize the json object you use the ```JsonWriter.write(JsonValue)``` or ```JsonWriter.writePretty(JsonValue)```. 
You probably guessed that
writePretty outputs prettified json. (Its not actaully pretty, just indented. Writing really pretty json is impossible) 

Lets give it a go:
```java
public class ParseExample {
  public static void main(String[] args) {

    final JsonObject json =
        jObj(
            field("name", jString("Ola Nordmann")),
            field("age", jNum(28)),
            field("favourites", jArray(jString("red"), jString("blue"), jString("purple")))
        );
        
    final String jsonString =
        JsonWriter.writePretty(json);
        
    System.out.println(jsonString);
  }
}
```
Which outputs
```json
{
  "age":28,
  "favourites":[
    "red",
    "blue",
    "purple"
  ],
  "name":"Ola Nordmann"
}
```

That was easy.




Now lets parse the string again:
```java
public class ParseExample {
  public static void main(String[] args) {

    final JsonObject json =
        jObj(
            field("name", jString("Ola Nordmann")),
            field("age", jNum(28)),
            field("favourites", jArray(jString("red"), jString("blue"), jString("purple")))
        );
    
    final String jsonString =
        JsonWriter.writePretty(json);
    
    
    final Validation<String,JsonValue> parsedJsonV =
        JsonParser.parse(jsonString);
  }
}
```
Observe that the parser yields a ```Validation<String,JsonValue>```. Of you are unfamiliar with the validation type you 
can google it, there are plenty of articles 
 that introduce the concept. Basically a Validation holds either a failure value (A ```String``` explaining what went 
 wrong in this case) 
 or a success value, you have to use some of its convenience methods or fold it to find out.


(If you are used to functional programming, you probably want to skip to the conversion section now...)

Now we have a validation with either a failure message or a JsonValue object, but which one? To make matters slightly worse, 
we do not even know what type
of JsonValue we have. (Well - _we_ do - because we wrote the json string we parsed, but imagine you got that string from 
someone else, over the internet even)

The idiomatic java solution would be to throw an exception in the failure case, and leave that for later (= never), 
and then maybe cast to the expected kind of
JsonValue. But that is not safe, and this is a safe library. Plus: we are smarter than that!
 
When you can assume that the string you parse has a certain structure (We will cover the case where you cannot or don't 
want to use a priori 
knowledge of the structure later on), you can construct a program that performs some operation on that structure or performs 
some failurehandling if it does
 not match.

Lets make a program that prints the name and age contained in the jsonstructure. 
```java
public class ParseExample {
  public static void main(String[] args) {

    final JsonObject json =
        jObj(
            field("name", jString("Ola Nordmann")),
            field("age", jNum(28)),
            field("favourites", jArray(jString("red"), jString("blue"), jString("purple")))
        );

    final String jsonString =
        JsonWriter.writePretty(json);


    final Validation<String, JsonValue> parsedJsonV =
        JsonParser.parse(jsonString);

    final F<JsonValue, Validation<String,String>> getNameAndAge =
        obj ->
            getFieldAsText(obj,"name").bind(name->getFieldAsText(obj,"age").map(age->name+", "+age)).toValidation("Oh noh!");
    
    final String output = 
           parsedJsonV.validation(
               failmsg->failmsg,
               parsedJson->getNameAndAge.f(parsedJson).validation(failmsg2->failmsg2,nameAndAge->nameAndAge));
 
    System.out.println(output);
  }
}
```


Our name and age extraction consists of two interesting parts:
First we have a function (the object of type ```F```) that accepts a JsonValue and yields a Validation with a string 
explaining the failure as the fail value, 
and the name and age concatenated
as the success value. 
Then we bind the function to the first validation. If the it is a fail, we just get the message. If it is a success, 
we apply the function to get a new validation which we fold into
either a new failmessage or the string we are constructing.

Hm, that seemed more complicated that it could have been. But why?
It is a consequence of constructing a safe program. When we have a result, we know that we are in a consistent state. 
It seems annoying to handle all possible failures
in a trivial example like this, but most programs start out as trivial. When we are forced to make qualified choices of 
how the program is constructed 
(including hwo to handle failure states)
early on, we assure ourselves that the application can grow without peril.

When we try it, it prints

```
Oh noh!

```

Bummer!

You probably spotted the bug earlier. I expected the age to be a string. If we try to convert to the wrong type we get None, 
which we converted to a failure message.
Lets correct the bug and try again:

```java
public class ParseExample {
  public static void main(String[] args) {

    final JsonObject json =
        jObj(
            field("name", jString("Ola Nordmann")),
            field("age", jNum(28)),
            field("favourites", jArray(jString("red"), jString("blue"), jString("purple")))
        );

    final String jsonString =
        JsonWriter.writePretty(json);


    final Validation<String, JsonValue> parsedJsonV =
        JsonParser.parse(jsonString);

    final F<JsonValue, Validation<String,String>> getNameAndAge =
        obj ->
            getFieldAsText(obj,"name").bind(name->getFieldAsText(obj,"age").map(age->name+", "+age)).toValidation("'name' or 'age' is missing");
    
    final String output = 
           parsedJsonV.validation(
               failmsg->failmsg,
               parsedJson->getNameAndAge.f(parsedJson).validation(failmsg2->failmsg2,nameAndAge->nameAndAge));
 
    System.out.println(output);
  }
}
```
which prints
```
Ola Nordmann, 28

```

Phew! But kson was supposed to be safe, what happened? Well. It _is_ safe, we did not throw any exceptions or burn 
down any building or the likes. 
And with a small modification we can let the compiler find bugs like this for us. Lets try again.
```java
public class ParseExample {
  public static void main(String[] args) {

    final JsonObject json =
        jObj(
            field("name", jString("Ola Nordmann")),
            field("age", jNum(28)),
            field("favourites", jArray(jString("red"), jString("blue"), jString("purple")))
        );

    final String jsonString =
        JsonWriter.writePretty(json);

    final Validation<String, JsonValue> parsedJsonV =
        JsonParser.parse(jsonString);

    final F<JsonValue, Validation<String, P2<String,BigDecimal>>> getNameAndAge =
        obj ->
            getFieldAsText(obj, "name").bind(name -> getFieldAsNumber(obj, "age").map(age -> P.p(name, age))).toValidation("'name' or 'age' is missing");

    final String output =
        parsedJsonV.validation(
            failmsg -> failmsg,
            parsedJson -> getNameAndAge.f(parsedJson).validation(failmsg2 -> failmsg2, nameAndAge -> nameAndAge._1()+","+nameAndAge._2()));

    System.out.println(output);

  }
}
```
which also prints
```
Ola Nordmann, 28

```
Can you spot the difference? We have changed the extraction function to return a tuple2 of string and bigdecimal and defer 
the construction of the string to the latest
possible moment in our program (often called "the end of the universe"). Now the compiler will tell us if we mistakenly 
use the wrong conversion. Later we will use
 the compiler even more by using converters.


##More on using JsonValue, for when you know little or nothing about its structure
A JsonValue has a very limited set of operations you can perform on it. This is because you do not know exactly what type 
of value it is. 
It represents the different kind of values a json structure can contain (boolean, number, string, array,object and null) 
as subtypes of JsonValue.
In kson we have opted for the visitor pattern to extract the content of the a json tree. You use a ```JsonValue.Fold``` 
type to fold a JsonValue into 
the desired type by calling ```JsonValue.fold(JsonValue.Fold)```.  Most java based json libraries use the unsafe _is***_ 
idiom in conjunction with a jungle of ifs
and elses. We felt that folding was the most compact and safe way to deconstruct a json tree.

The downside with folding the datastructure directly is that json is a nested structure, and
we would like to easily get to the deepnodes without manually having to handle all possibilities
along the way. That is what _Lenses_ are for! A Lens is an object that knows how to extract a value
from an object, and to set a value in an object - in a immutable manner. Think of it as a setter
and getter pair, but not attached to the object it sets and gets from. This gives us the advantage 
of combining Lenses together as we wish, and create arbitrary combinations and nested goodness
without having the json object at hand. 
This sounds like mumbo jumbo for most java-developers, so lets look at an example:

```java
public class LensExample {

  public static void main(String[] args) {

    JsonLens<JsonValue, String> zipLens =
        JsonLenses.select("address").select("zip").asString();


    JsonValue userjson =
        jObj(
            field("name", jString("Ole Normann")),
            field("address", jObj(
                field("street", jString("Kongensgate 3")),
                field("zip", jString("1234")) //Observe the presence of this field
            ))
        );

    JsonValue invalidUserJson =
        jObj(
            field("name", jString("Kari Normann")),
            field("address", jObj(
                field("street", jString("Kongensgate 3"))
                //Behold the missing zipcode field
            ))
        );

    
    LensResult<String> zipCode = 
        zipLens.get(userjson);
    
    System.out.println(zipCode); //The zipLens yields the zipcode as expected

    LensResult<String> noZipCode =
        zipLens.get(invalidUserJson);

    System.out.println(noZipCode); //The zipLens yields no zipcode, also as expected
    
  }
}
```
Not too shabby, considering this is safe. But why bother with lenses? We could just as well write
``` userjson.getField("address").bind(f->f.getFieldAsText("zip")).getOrElse("No zipcode in object)```.
There are three advantages of using zippers over getters:
 * They are values, you can use them like any other value, pass them as arguments and so forth.
 * They compose, so you can mix them as you like
 * They can update inner values in an immutable fashion
 
(We leave as an excercise for the reader to prove that these points are indeed advantages. Please make a deeply nested
object and update a leaf value in a safe way)

Lets try a less trivial example with an update:
```java
public class LensExample {

  public static void main(String[] args) {

    JsonLens<JsonValue, String> zipLens =
        JsonLenses.select("address").select("zip").asString();


    JsonValue userjson =
        jObj(
            field("name", jString("Ole Normann")),
            field("address", jObj(
                field("street", jString("Kongensgate 3")),
                field("zip", jString("1234")) //Observe the presence of this field
            ))
        );

    JsonValue invalidUserJson =
        jObj(
            field("name", jString("Kari Normann")),
            field("address", jObj(
                field("street", jString("Kongensgate 3"))
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
the zipcode of the users in the list has been updated.

##Converting to your domainmodel using the typesafest (and simplest actually) way.
Nothing is simpler than the automatic conversion done by jackson you say? Then i am convinced that your 
application is either stringly typed or infected with annotion hell. Or you use stringly typed DTOs to provide
for the mapping. Adding a converter for you Money and Measurements librarires is also trivial i presume?
 
 Lets see how simple this can be with kson. We start by importing the dsl on the ```JsonCodecs``` class.



...


##Behind the scenes
###or how to build an excellent library by combining small parts
Building this library was actually an excercise in teaching functional progarmming. If it served its purpose
awaits to be seen.

Our main fascination for functional programming arose when we discovered that functionally written programs
have certain advantages over idiomatic java (commonly referred to as "objectoriented programming"):

 * The programs became smaller, meaning less code to write and maintain
 * The programs became safer, less exceptions, bugs and mistakes were made.
 * We found the functional paradigm was easier to reason about, and hence the architecture and design of our
were improved (i know, it is subjective. But try for yourself)
 
By writing this library we wanted to demostrate how easy it is to build a solid library (that beats all the jsonlibraries i know of
regarding all aspects except runtime speed) by using only a limited set of buildingblocks.

This library is built up by four distinct parts. Used by the others, and core to the library are the _data-objects_ 
(org.kantega.kson.json) that represent
a json structure. To convert to and from character streams / strings one can use the _parser_ part. To navigate in the json structure you
can employ the _lens_ part. And finally you can map json to your domain model by using the _codec_ part.
 
Here we explain how we built the codec. Althoug it is on the "highest level", we feel that it is a nice place to 
start learning.

In idiomatic java (henceforth referred to as Objectoriented programming, or OO, even though many Smalltalkers would 
protest heavily upon that
label) one usually thinks of a program of a graph of objects that collaborate. A network of objects send messages
or data to each other by calling each others functions. The object graph is often stored using some mapping that maps to 
some underlying store. When we act on the application, som object graph is restored, messages are submittet to the objects,
and the objects are stored again.

In the Functional paradigm however, we think of a program that converts input to output. If we have persist state, we
add that to the equotion: some input plus some state is converted to some output and a new state. Since this library does not
concern itself with state, we can forget about that all together: We can work completely stateless.


###Basics

Now, converting between our domain objects and json can be represented by two functions: An encoder which converts to json, 
and a decoder which converts from json. These two functions can be represented as functional interfaces.

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
Take note that the function does not change anything in the surroundings, it has no sideeffects. That means that
 calling the function with the same argument will always yield the same results.
We extend ```F``` for two reasons: It makes it clear we think of the constructs as functions, but it also 
gives us the benefits of using the functionaljava library, which has a lot of functionality around functions.

Now we have defined our encoder and decoder functions, but we have no implementations of them yet. Lets change that
by implemementing the simplest possible encoder: An encoder for strings:

```java
public class JsonEncoders {

  public static final JsonEncoder<String> stringEncoder =
      JsonValues::jString;

}
```

Wow, that was simple. Not everything looks complicated using FP. Lets complete the dsl with all the basic 
json types:

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
program safer too, since _null_ is netiher typesafe nor carries semantic meaning. You cannot look at _null_ 
and understand its meaning without looking at the context, for example the field or variable it is assigned to.

But how do we represent the case when we have no value for field? Turns out java added a type to represent that:
```Optional<T>```, which is intertpreted as "The object can contain zero or one element of type T, but you 
do not know". We use the type ```Option<A>``` from fj, but for consistence. They are pretty eqvivalent.

In json a missing value is represented by the keyword _null_. But when we encode, we know the expected type, so we
can use an ```Option<A>``` to hold the unknown state (_null_ or value), but how do we convert?
Lets examine the type of the function first:

We want to convert a JsonValue that can be either _null_ or some primitive value, into an ```Option<A>``` where 
```A``` is the type of the value. 

If we know the type is either _null_ or ```String``` we could write 
```JsonEncoder<Option<String>>```, but that only works for strings. We want to be able to make all types
optional:
```JsonEncoder<Option<A>>```.
Lets implement this:

```
  public static <A> JsonEncoder<Option<A>> optionEncoder(){
       return maybeValue -> ???;
  }  
```

So what do we actally return? It turns out we need to know how we encode ```A``` before we can encode ```Option<A>```, so we supply
that:

```
  public static <A> JsonEncoder<Option<A>> optionEncoder(JsonEncoder<A> aEncoder){
    return maybeValue -> maybeValue.option(JsonValues.jNull(), aEncoder::encode);
  }
```

maybeValue.option is the _fold_ of the Option type, meaning it handles both states, and transforms
the value completely to an object of another type without the possibility to go back. The first argument
is used in case the Option is empty (None), and the second argument is a function that transforms the 
contained value to an object of the target type.

That wasnt that hard, plus we learned about fold, and got a feel for how we can can transform an encoder for a type
into a transformer for optional values of that type.

###Arrays
Lets dig deeper  and go for a non-primitive json types, for example array:
We want to transform a List with As - ```List<A>``` to a json array. Its just as with option, of we can encode
A, we can encode a List og As.

```
  public static <A> JsonEncoder<List<A>> arrayEncoder(JsonEncoder<A> aEncoder) {
    return list -> JsonValues.jArray(list.map(aEncoder::encode));
  }
```

Now, in json, an array can interchange its the types of its element, meaning you can mix numbers, string objects etc.
In a typed language (like java), that sort of structure is called an HList (for Heterogenous List). You basically
have to provide a converter for all the elements which makes it a bit more complicated. Lets save that for later.







