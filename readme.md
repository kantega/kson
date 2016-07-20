# KSON - minimal JSON parsing, writing and converting

KSON is a safe and minimal library for parsing text into json, writing json, and converting json to and from your domain types.

By safe we mean no sideeffects (exceptions, mutations). All objects are immutable and threadsafe. (Also the fields of all objects this library produces are immutable and threadsafe,
we even encourage the use of object fields, they are all final) The API will not let you perform operations that will
leave your objects in an undefined or undesired state. If you follow the types, it works!

The Kson library depends on functionaljava and functionaljava-quickcheck. The parser is implemented by using the parser combinators from the functionaljava
library. Read more on [functionaljava.org](https://functionaljava.org/)

Beware though: This library is slow. Very slow. It uses over 100 times more time to parse a json document
than other parsers. Does it matter to you? Probably not, as most likely your database or network is the bottneck.
However, if you _do_ discover that indeed the json parsing is the bottlneck of your application, please
let us know an dwe will spend some time optimizing.
Or use one of the unsafe libraries out there. You probably know them already.


##Building, writing and parsing json

Lets begin by constructing some json values with the ```JsonValues``` class. By importing the static methods of the class, you get a nice and readable dsl for constructing 
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
We have chosen not to provide any shortcuts, and no under the hood conversion. If you would like the convenience of constructing JsonString objects automatically, you can provide your own dsl. 
(In fact, that is why we use static methods and static imports for the dsl, it makes it very easy to extend the dsl with your own combinators)

In order to serialize the json object you use the ```JsonWriter.write(JsonValue)``` or ```JsonWriter.writePretty(JsonValue)```. You probably guessed that
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
Observe that the parser yields a ```Validation<String,JsonValue>```. Of you are unfamiliar with the validation type you can google it, there are plenty of articles 
 that introduce the concept. Basically a Validation holds either a failure value (A ```String``` explaining what went wrong in this case) 
 or a success value, you have to use some of its convenience methods or fold it to find out.


(If you are used to functional programming, you probably want to skip to the conversion section now...)

Now we have a validation with either a failure message or a JsonValue object, but which one? To make matters slightly worse, we do not even know what type
of JsonValue we have. (Well - _we_ do - because we wrote the json string we parsed, but imagine you got that string from someone else, over the internet even)

The idiomatic java solution would be to throw an exception in the failure case, and leave that for later (= never), and then maybe cast to the expected kind of
JsonValue. But that is not safe, and this is a safe library. Plus: we are smarter than that!
 
When you can assume that the string you parse has a certain structure (We will cover the case where you cannot or don't want to use a priori 
knowledge of the structure later on), you can construct a program that performs some operation on that structure or performs some failurehandling if it does
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
            getFieldAsText(obj,"name").bind(name->getFieldAsText(obj,"age").map(age->name+", "+age)).toValidation("'name' or 'age' is missing");
    
    final String output = 
           parsedJsonV.validation(
               failmsg->failmsg,
               parsedJson->getNameAndAge.f(parsedJson).validation(failmsg2->failmsg2,nameAndAge->nameAndAge));
 
    System.out.println(output);
  }
}
```


Our name and age extraction consists of two interesting parts:
First we have a function (the object of type ```F```) that accepts a JsonValue and yields a Validation with a string explaining the failure as the fail value, and the name and age concatenated
as the success value. 
Then we bind the function to the first validation. If the it is a fail, we just get the message. If it is a success, we apply the function to get a new validation which we fold into
either a new failmessage or the string we are constructing.

Hm, that seemed more complicated that it could have been. But why?
It is a consequence of constructing a safe program. When we have a result, we know that we are in a consistent state. It seems annoying to handle all possible failures
in a trivial example like this, but most programs start out as trivial. When we are forced to make qualified choices of how the program is constructed (including hwo to handle failure states)
early on, we assure ourselves that the application can grow without peril.

When we try it, it prints

```
'name' or 'age' is missing

```

Bummer!

You probably spotted the bug earlier. I expected the age to be a string. If we try to convert to the wrong type we get None, which we converted to a failure message.
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

But it was supposed to be safe, what happened? Well. It _is_ safe, but with a small modification we can let the compiler find bugs like this for us. Lets try again.
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
Can you spot the difference? We have changed the extraction function to return a tuple2 of string and bigdecimal and defer the construction of the string to the latest
possible moment in our program (often called "the end of the universe"). Now the compiler will tell us if we mistakenly use the wrong conversion. Later we will use
 the compiler even more by using converters.


##More on using JsonValue, for when you know little or nothing about its structure
A JsonValue has a very limited set of operations you can perform on it. This is because you do not know exactly what type of value it is. 
It represents the different kind of values a json structure can contain (boolean, number, string, array,object and null) as subtypes of JsonValue.
In kson we have opted for the visitor pattern to extract the content of the a json tree. You use a ```JsonValue.Fold``` type to fold a JsonValue into 
the desired type by calling ```JsonValue.fold(JsonValue.Fold)```.  Most java based json libraries use the unsafe _is***_ idiom in conjunction with a jungle of ifs
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
Which is what we could expect: The "leader" field has been replaced by the user wihtout a zipcode, and
the zipcode of the users in the list has been updated.

##Converting to and from JsonValue, the typesafest way to convert to and from json.


...


