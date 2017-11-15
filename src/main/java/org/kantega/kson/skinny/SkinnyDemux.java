package org.kantega.kson.skinny;

import fj.Ord;
import fj.P2;
import fj.data.*;
import org.kantega.kson.JsonResult;
import org.kantega.kson.codec.JsonDecoder;
import org.kantega.kson.json.JsonValue;


/**
 * Demuxing messages based on name of the field that contains the payload
 * For example
 * {"msg":["value1","value2"]}
 * Would loog for the decoder registered under "msg"
 *
 * @param <A>
 */
public class SkinnyDemux<A> implements JsonDecoder<A> {

    private final TreeMap<String, JsonDecoder<A>> decoders;

    private SkinnyDemux(TreeMap<String, JsonDecoder<A>> decoders) {
        this.decoders = decoders;
    }

    @SafeVarargs
    public static <A> SkinnyDemux<A> demuxer(P2<String, JsonDecoder<? extends A>>... messageDecoders) {
        TreeMap<String, JsonDecoder<A>> decoders =
          List.arrayList(messageDecoders).foldLeft((map, pair) -> map.set(pair._1(), (JsonDecoder<A>) pair._2()), TreeMap.empty(Ord.stringOrd));
        return new SkinnyDemux<>(decoders);
    }


    public JsonResult<A> decode(JsonValue value) {
        return
          value.asObject().bind(
            obj ->
              obj.pairs.min().map(pair -> {
                  String                 name         = pair._1();
                  Option<JsonDecoder<A>> maybeDecoder = decoders.get(name);
                  return maybeDecoder.map(decoder -> decoder.<A>decode(pair._2()))
                    .orSome(JsonResult.fail("No decoder registered with name " + name + " in demuxer"));
              }).orSome(JsonResult.fail("Found an empty object when trying to demux a message"))
          );
    }


}
