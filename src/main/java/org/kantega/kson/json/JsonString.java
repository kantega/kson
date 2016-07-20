package org.kantega.kson.json;

import fj.F;

public class JsonString extends JsonValue{

  public final String value;

  public JsonString(String value) {
    this.value = value;
  }

  public JsonString update(F<String,String> f){
    return new JsonString(f.f(value));
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("JsonString{");
    sb.append("value='").append(value).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
