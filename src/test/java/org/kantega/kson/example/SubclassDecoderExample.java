package org.kantega.kson.example;

import fj.P;
import fj.data.List;
import org.kantega.kson.JsonResult;
import org.kantega.kson.codec.JsonDecoder;
import org.kantega.kson.json.JsonValue;
import org.kantega.kson.parser.JsonParser;

import java.math.BigDecimal;

import static org.kantega.kson.codec.JsonDecoders.*;

public class SubclassDecoderExample {

    static final JsonDecoder<Common> aDecoder =
        obj(field("name", stringDecoder))
            .map(A::new);

    static final JsonDecoder<Common> bDecoder =
        obj(field("value", bigDecimalDecoder))
            .map(B::new);

    static final JsonDecoder<Common> commonDecoder =
        subclassObjDecoder("type", P.p("a",aDecoder),P.p("b",bDecoder));

    static final JsonDecoder<List<Common>> commonListDecoder =
        arrayDecoder(commonDecoder);

    static final String json = "[" +
        "{\"type\":\"a\",\"name\":\"ONE\"}," +
        "{\"type\":\"a\",\"name\":\"TWO\"}," +
        "{\"type\":\"b\",\"value\":1.2}]";

    static final String failjson = "[" +
        "{\"type\":\"a\",\"name\":\"ONE\"}," +
        "{\"type\":\"a\",\"name\":\"TWO\"}," +
        "{\"type\":\"c\",\"value\":1.2}]";

    public static void main(String[] args) {
        List<Common> list = JsonParser.parse(json).decode(commonListDecoder).orThrow();
        System.out.println(list);

        List<Common> faillist = JsonParser.parse(failjson).decode(commonListDecoder).orThrow();
        System.out.println(faillist);
    }


    interface Common {
    }

    static class A implements Common {

        public final String name;

        A(String name) {
            this.name = name;
        }
    }

    static class B implements Common {
        public final BigDecimal value;

        B(BigDecimal value) {
            this.value = value;
        }
    }
}
