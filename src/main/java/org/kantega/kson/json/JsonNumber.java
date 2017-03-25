package org.kantega.kson.json;

import fj.Equal;
import fj.F;

import java.math.BigDecimal;

public class JsonNumber extends JsonValue {

  public final static Equal<JsonNumber> eq =
      Equal.stringEqual.contramap(num -> num.value);

  public final String value;

  public JsonNumber(String value) {
    this.value = value;
  }

  public JsonNumber update(F<BigDecimal,BigDecimal> f){
    return new JsonNumber(f.f(new BigDecimal(value)).toString());
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("JsonNumber{");
    sb.append(value);
    sb.append('}');
    return sb.toString();
  }
}
