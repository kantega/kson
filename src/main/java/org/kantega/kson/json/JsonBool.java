package org.kantega.kson.json;

import fj.Equal;

public class JsonBool extends JsonValue {

  public static final Equal<JsonBool> eq =
      Equal.booleanEqual.contramap(eq -> eq.value);

  public final boolean value;

  public JsonBool(boolean value) {
    this.value = value;
  }

}
