package org.kantega.kson.codec;

import fj.F;
import org.kantega.kson.JsonResult;
import org.kantega.kson.json.JsonValue;

public interface JsonDecoder<A> extends F<JsonValue,JsonResult<A>> {

  default JsonResult<A> f(JsonValue v){
    return decode(v);
  }

  JsonResult<A> decode(JsonValue v);

  default <B> JsonDecoder<B> map(F<A,B> f){
    return v->this.decode(v).map(f);
  }

}
