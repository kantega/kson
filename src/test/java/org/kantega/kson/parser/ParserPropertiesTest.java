package org.kantega.kson.parser;

import fj.Equal;
import fj.P;
import fj.P2;
import fj.data.List;
import fj.data.Validation;
import fj.test.Arbitrary;
import fj.test.Gen;
import fj.test.Property;
import fj.test.reflect.Name;
import fj.test.runner.PropertyTestRunner;
import org.junit.runner.RunWith;
import org.kantega.kson.json.JsonValue;
import org.kantega.kson.json.JsonValues;

import java.util.function.Supplier;

import static fj.data.Enumerator.charEnumerator;
import static fj.data.Stream.range;
import static fj.test.Gen.elements;

@RunWith(PropertyTestRunner.class)
public class ParserPropertiesTest {

  final Gen<String> nonEmptyAlphanum =
      Arbitrary.arbNonEmptyList(elements(range(charEnumerator, 'a', 'z').append(
          range(charEnumerator, 'A', 'Z')).append(
          range(charEnumerator, '0', '9')).toArray().array(Character[].class))).map(list -> List.asString(list.toList()));

  final Gen<JsonValue> jBoolGen =
      Arbitrary.arbBoolean.map(JsonValues::jBool);

  final Gen<JsonValue> jStringGen =
      nonEmptyAlphanum.map(JsonValues::jString);

  final Gen<JsonValue> jNumberGen =
      Arbitrary.arbBigDecimal.map(JsonValues::jNum);

  final Gen<P2<String, JsonValue>> fieldGen(int depth) {
    return nonEmptyAlphanum.bind(valueGen(depth), name -> value -> P.<String, JsonValue>p(name, value));
  }

  final Gen<List<P2<String, JsonValue>>> arbUniqueFields(int depth) {
    return Arbitrary.arbNonEmptyList(fieldGen(depth)).map(nel -> nel.toList().nub(Equal.stringEqual.contramap(P2::_1)));
  }

  Gen<JsonValue> jObjGen(int depth) {
    return arbUniqueFields(depth).map(JsonValues::jObj);
  }

  Gen<JsonValue> jArrayGen(int depth) {
    return Arbitrary.arbList(valueGen(depth)).map(JsonValues::jArray);
  }

  Gen<JsonValue> valueGen(int depth) {
    return lazy(() -> depth > 1 ?
        Gen.oneOf(List.list(jBoolGen, jStringGen, jNumberGen, jObjGen(Math.min(depth - 1, 5)), jArrayGen(Math.min(depth - 1, 5)))) :
        Gen.oneOf(List.list(jBoolGen, jStringGen, jNumberGen)));
  }

  Gen<JsonValue> lazy(Supplier<Gen<JsonValue>> l) {
    return Gen.gen(i -> r ->
        l.get().gen(Math.min(7, i), r));
  }

  Gen<JsonValue> jsonGen() {
    return Gen.sized(this::valueGen);
  }

  @Name("Any JsonValue constructed with the generators must be serialized and deserialized into the same JsonValue")
  Property p1 =
      Property.property(jsonGen(), json -> {
        String                        jsonString = JsonWriter.writePretty(json);
        Validation<String, JsonValue> readVal    = JsonParser.parse(jsonString);
        return Property.prop(readVal.isSuccess() && JsonValue.eq().eq(json, readVal.success()));
      });

}
