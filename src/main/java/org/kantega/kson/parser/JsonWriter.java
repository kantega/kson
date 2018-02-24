package org.kantega.kson.parser;

import fj.data.List;
import fj.data.Stream;
import fj.function.Effect1;
import fj.function.TryEffect1;
import org.kantega.kson.json.JsonValue;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;

public class JsonWriter {
    private static final int CONTROL_CHARACTERS_END = 0x001f;

    private static final char[] QUOT_CHARS         = {'\\', '"'};
    private static final char[] BS_CHARS           = {'\\', '\\'};
    private static final char[] LF_CHARS           = {'\\', 'n'};
    private static final char[] CR_CHARS           = {'\\', 'r'};
    private static final char[] TAB_CHARS          = {'\\', 't'};
    // In JavaScript, U+2028 and U+2029 characters count as line endings and must be encoded.
    // http://stackoverflow.com/questions/2965293/javascript-parse-error-on-u2028-unicode-character
    private static final char[] UNICODE_2028_CHARS = {'\\', 'u', '2', '0', '2', '8'};
    private static final char[] UNICODE_2029_CHARS = {'\\', 'u', '2', '0', '2', '9'};
    private static final char[] HEX_DIGITS         = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
      'a', 'b', 'c', 'd', 'e', 'f'};


    public static String write(JsonValue json) {
        return json
          .onNull(() -> "null")
          .orElse(json.onBool(Object::toString))
          .orElse(json.onNumber(BigDecimal::toString))
          .orElse(json.onString(s -> "\"" + writeJsonString(s) + "\""))
          .orElse(json.onArray(arr -> arr.isEmpty() ? "[]" : "[" + arr.tail().foldLeft((sum, val) -> sum + "," + write(val), write(arr.head())) + "]"))
          .orElse(json.onObject(obj -> mkString(obj.toList().map(pairs -> "\"" + pairs._1() + "\":" + write(pairs._2())), "{", ",", "}")))
          .orSome("");
    }

    public static String writePretty(JsonValue json) {
        return writePretty(json, 0);
    }

    private static String writePretty(JsonValue json, int indent) {
        return json
          .onNull(() -> "null")
          .orElse(json.onBool(Object::toString))
          .orElse(json.onNumber(BigDecimal::toString))
          .orElse(json.onString(s -> "\"" + writeJsonString(s) + "\""))
          .orElse(json.onArray(arr -> arr.isEmpty() ? "[]" : "[\n" + arr.tail().foldLeft((sum, val) -> sum + line(",") + indent(indent + 2) + writePretty(val, indent + 2), indent(indent + 2) + writePretty(arr.head(), indent + 2)) + "\n" + indent(indent) + "]"))
          .orElse(json.onObject(obj -> mkString(obj.toList().map(pairs -> indent(indent + 2) + "\"" + pairs._1() + "\":" + writePretty(pairs._2(), indent + 2)), line("{"), ",\n", "\n" + indent(indent) + "}")))
          .orSome("");
    }

    private static String mkString(List<String> vals, String pre, String delim, String post) {
        return vals.isEmpty()
          ? pre + post
          : pre + vals.tail().foldLeft((sum, v) -> sum + delim + v, vals.head()) + post;
    }

    private static String indent(int depth) {
        return Stream.asString(Stream.repeat(' ').take(depth));
    }

    private static String line(String l) {
        return l + "\n";
    }


    private static Effect1<Writer> write(TryEffect1<Writer, IOException> effect) {
        return writer -> {
            try {
                effect.f(writer);
            } catch (Exception e) {
                throw new RuntimeException("Could not write json to writer: " + e.getMessage(), e);
            }
        };
    }

    private static String writeJsonString(String string) {
        try {
            StringWriter writer = new StringWriter();
            int          length = string.length();
            int          start  = 0;
            for (int index = 0; index < length; index++) {
                char[] replacement = getReplacementChars(string.charAt(index));
                if (replacement != null) {
                    writer.write(string, start, index - start);
                    writer.write(replacement);
                    start = index + 1;
                }
            }
            writer.write(string, start, length - start);
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Could not write string" + e.getMessage(), e);
        }
    }


    private static char[] getReplacementChars(char ch) {
        if (ch > '\\') {
            if (ch < '\u2028' || ch > '\u2029') {
                // The lower range contains 'a' .. 'z'. Only 2 checks required.
                return null;
            }
            return ch == '\u2028' ? UNICODE_2028_CHARS : UNICODE_2029_CHARS;
        }
        if (ch == '\\') {
            return BS_CHARS;
        }
        if (ch > '"') {
            // This range contains '0' .. '9' and 'A' .. 'Z'. Need 3 checks to get here.
            return null;
        }
        if (ch == '"') {
            return QUOT_CHARS;
        }
        if (ch > CONTROL_CHARACTERS_END) {
            return null;
        }
        if (ch == '\n') {
            return LF_CHARS;
        }
        if (ch == '\r') {
            return CR_CHARS;
        }
        if (ch == '\t') {
            return TAB_CHARS;
        }
        return new char[]{'\\', 'u', '0', '0', HEX_DIGITS[ch >> 4 & 0x000f], HEX_DIGITS[ch & 0x000f]};
    }
}
