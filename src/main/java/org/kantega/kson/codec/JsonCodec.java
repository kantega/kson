package org.kantega.kson.codec;

import fj.F;
import org.kantega.kson.JsonResult;
import org.kantega.kson.json.JsonValue;

public class JsonCodec<A> {

  private final JsonEncoder<A> encoder;
  private final JsonDecoder<A> decoder;

  public JsonCodec(JsonEncoder<A> encoder, JsonDecoder<A> decoder) {
    this.encoder = encoder;
    this.decoder = decoder;
  }

  public JsonValue encode(A t){
    return encoder.encode(t);
  }

  public JsonResult<A> decode(JsonValue val){
    return decoder.decode(val);
  }

  public <B> JsonCodec<B> xmap(F<B,A> f, F<A,B> g){
    return new JsonCodec<>(encoder.contramap(f),decoder.map(g));
  }

}
