package org.kantega.kson.json;

import fj.Equal;
import fj.F;
import fj.data.List;

public class JsonBool extends JsonValue {

  public static final Equal<JsonBool> eq =
      Equal.booleanEqual.contramap(eq -> eq.value);

  public final boolean value;

  public JsonBool(boolean value) {
    this.value = value;
  }

  public JsonBool update(F<Boolean,Boolean> f){
    return new JsonBool(f.f(value));
  }
}
