package org.kantega.kson.json;

import fj.Equal;
import fj.F;
import fj.data.Option;

public class JsonBool extends JsonValue {

  public static final Equal<JsonBool> eq =
      Equal.booleanEqual.contramap(eq -> eq.value);

  public final boolean value;

  public JsonBool(boolean value) {
    this.value = value;
  }

  public <T> Option<T> onBool(F<Boolean, T> f) {
    return Option.some(f.f(value));
  }

  public JsonBool update(F<Boolean,Boolean> f){
    return new JsonBool(f.f(value));
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("JsonBool{");
    sb.append(value);
    sb.append('}');
    return sb.toString();
  }
}
