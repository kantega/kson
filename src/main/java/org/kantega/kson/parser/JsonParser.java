
package org.kantega.kson.parser;

import fj.Ord;
import fj.data.List;
import fj.data.TreeMap;
import org.kantega.kson.JsonResult;
import org.kantega.kson.json.JsonArray;
import org.kantega.kson.json.JsonObject;
import org.kantega.kson.json.JsonValue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;

import static org.kantega.kson.json.JsonValues.*;

/**
 * Parser, based on the fast https://github.com/ralfstx/minimal-json
 */
public class JsonParser {

    private static final int MIN_BUFFER_SIZE     = 10;
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private final Reader        reader;
    private final char[]        buffer;
    private       int           bufferOffset;
    private       int           index;
    private       int           fill;
    private       int           line;
    private       int           lineOffset;
    private       int           current;
    private       StringBuilder captureBuffer;
    private       int           captureStart;

  /*
   * |                      bufferOffset
   *                        v
   * [a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t]        < input
   *                       [l|m|n|o|p|q|r|s|t|?|?]    < buffer
   *                          ^               ^
   *                       |  index           fill
   */

    private JsonParser(String string) {
        this(new StringReader(string),
          Math.max(MIN_BUFFER_SIZE, Math.min(DEFAULT_BUFFER_SIZE, string.length())));
    }

    private JsonParser(Reader reader) {
        this(reader, DEFAULT_BUFFER_SIZE);
    }

    private JsonParser(Reader reader, int buffersize) {
        this.reader = reader;
        buffer = new char[buffersize];
        line = 1;
        captureStart = -1;
    }

    private JsonValue parse() throws IOException {
        read();
        skipWhiteSpace();
        JsonValue result = readValue();
        skipWhiteSpace();
        if (!isEndOfText()) {
            throw error("Unexpected character");
        }
        return result;
    }

    public static JsonResult<JsonValue> parse(String string) {
        try {
            return JsonResult.success(new JsonParser(string).parse());
        } catch (IOException ioe) {
            return JsonResult.fail("IOException while parsing: " + ioe.getMessage());
        } catch (ParseFailure f) {
            return JsonResult.fail("Failed to parse resource: " + f.getMessage() + ": line " + f.line + ", " + f.offset + ", i" + f.i);
        }
    }

    public static JsonResult<JsonValue> parse(Reader string) {
        try {
            return JsonResult.success(new JsonParser(string).parse());
        } catch (IOException ioe) {
            return JsonResult.fail("IOException while parsing: " + ioe.getMessage());
        } catch (ParseFailure f) {
            return JsonResult.fail("Failed to parse resource: " + f.getMessage() + ": line " + f.line + ", " + f.offset + ", i" + f.i);
        }
    }

    private JsonValue readValue() throws IOException {
        switch (current) {
            case 'n':
                return readNull();
            case 't':
                return readTrue();
            case 'f':
                return readFalse();
            case '"':
                return readString();
            case '[':
                return readArray();
            case '{':
                return readObject();
            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return readNumber();
            default:
                throw expected("value");
        }
    }

    private JsonArray readArray() throws IOException {
        read();
        skipWhiteSpace();
        if (readChar(']')) {
            return new JsonArray(List.nil());
        }
        List<JsonValue> list = List.nil();
        do {
            skipWhiteSpace();
            list = list.cons(readValue());
            skipWhiteSpace();
        } while (readChar(','));
        if (!readChar(']')) {
            throw expected("',' or ']'");
        }
        return new JsonArray(list.reverse());
    }

    private JsonObject readObject() throws IOException {
        read();
        skipWhiteSpace();
        if (readChar('}')) {
            return new JsonObject(TreeMap.empty(Ord.stringOrd));
        }
        TreeMap<String, JsonValue> contents = TreeMap.empty(Ord.stringOrd);
        do {
            skipWhiteSpace();
            String name = readName();
            skipWhiteSpace();
            if (!readChar(':')) {
                throw expected("':'");
            }
            skipWhiteSpace();
            contents = contents.set(name, readValue());
            skipWhiteSpace();
        } while (readChar(','));
        if (!readChar('}')) {
            throw expected("',' or '}'");
        }
        return new JsonObject(contents);
    }

    private String readName() throws IOException {
        if (current != '"') {
            throw expected("name");
        }
        return readStringInternal();
    }

    private JsonValue readNull() throws IOException {
        read();
        readRequiredChar('u');
        readRequiredChar('l');
        readRequiredChar('l');
        return jNull();
    }

    private JsonValue readTrue() throws IOException {
        read();
        readRequiredChar('r');
        readRequiredChar('u');
        readRequiredChar('e');
        return jBool(true);
    }

    private JsonValue readFalse() throws IOException {
        read();
        readRequiredChar('a');
        readRequiredChar('l');
        readRequiredChar('s');
        readRequiredChar('e');
        return jBool(false);
    }

    private void readRequiredChar(char ch) throws IOException {
        if (!readChar(ch)) {
            throw expected("'" + ch + "'");
        }
    }

    private JsonValue readString() throws IOException {
        return jString(readStringInternal());
    }

    private String readStringInternal() throws IOException {
        read();
        startCapture();
        while (current != '"') {
            if (current == '\\') {
                pauseCapture();
                readEscape();
                startCapture();
            } else if (current < 0x20) {
                throw expected("valid string character");
            } else {
                read();
            }
        }
        String string = endCapture();
        read();
        return string;
    }

    private void readEscape() throws IOException {
        read();
        switch (current) {
            case '"':
            case '/':
            case '\\':
                captureBuffer.append((char) current);
                break;
            case 'b':
                captureBuffer.append('\b');
                break;
            case 'f':
                captureBuffer.append('\f');
                break;
            case 'n':
                captureBuffer.append('\n');
                break;
            case 'r':
                captureBuffer.append('\r');
                break;
            case 't':
                captureBuffer.append('\t');
                break;
            case 'u':
                char[] hexChars = new char[4];
                for (int i = 0; i < 4; i++) {
                    read();
                    if (!isHexDigit()) {
                        throw expected("hexadecimal digit");
                    }
                    hexChars[i] = (char) current;
                }
                captureBuffer.append((char) Integer.parseInt(new String(hexChars), 16));
                break;
            default:
                throw expected("valid escape sequence");
        }
        read();
    }

    private JsonValue readNumber() throws IOException {
        startCapture();
        readChar('-');
        int firstDigit = current;
        if (!readDigit()) {
            throw expected("digit");
        }
        if (firstDigit != '0') {
            while (readDigit()) {
            }
        }
        readFraction();
        readExponent();
        return jNum(new BigDecimal(endCapture()));
    }

    private boolean readFraction() throws IOException {
        if (!readChar('.')) {
            return false;
        }
        if (!readDigit()) {
            throw expected("digit");
        }
        while (readDigit()) {
        }
        return true;
    }

    private boolean readExponent() throws IOException {
        if (!readChar('e') && !readChar('E')) {
            return false;
        }
        if (!readChar('+')) {
            readChar('-');
        }
        if (!readDigit()) {
            throw expected("digit");
        }
        while (readDigit()) {
        }
        return true;
    }

    private boolean readChar(char ch) throws IOException {
        if (current != ch) {
            return false;
        }
        read();
        return true;
    }

    private boolean readDigit() throws IOException {
        if (!isDigit()) {
            return false;
        }
        read();
        return true;
    }

    private void skipWhiteSpace() throws IOException {
        while (isWhiteSpace()) {
            read();
        }
    }

    private void read() throws IOException {
        if (index == fill) {
            if (captureStart != -1) {
                captureBuffer.append(buffer, captureStart, fill - captureStart);
                captureStart = 0;
            }
            bufferOffset += fill;
            fill = reader.read(buffer, 0, buffer.length);
            index = 0;
            if (fill == -1) {
                current = -1;
                return;
            }
        }
        if (current == '\n') {
            line++;
            lineOffset = bufferOffset + index;
        }
        current = buffer[index++];
    }

    private void startCapture() {
        if (captureBuffer == null) {
            captureBuffer = new StringBuilder();
        }
        captureStart = index - 1;
    }

    private void pauseCapture() {
        int end = current == -1 ? index : index - 1;
        captureBuffer.append(buffer, captureStart, end - captureStart);
        captureStart = -1;
    }

    private String endCapture() {
        int    end = current == -1 ? index : index - 1;
        String captured;
        if (captureBuffer.length() > 0) {
            captureBuffer.append(buffer, captureStart, end - captureStart);
            captured = captureBuffer.toString();
            captureBuffer.setLength(0);
        } else {
            captured = new String(buffer, captureStart, end - captureStart);
        }
        captureStart = -1;
        return captured;
    }

    private ParseFailure expected(String expected) {
        if (isEndOfText()) {
            return error("Unexpected end of input");
        }
        return error("Expected " + expected);
    }

    private ParseFailure error(String message) {
        int absIndex = bufferOffset + index;
        int column   = absIndex - lineOffset;
        int offset   = isEndOfText() ? absIndex : absIndex - 1;
        return new ParseFailure(message, offset, line, column - 1);
    }

    private boolean isWhiteSpace() {
        return current == ' ' || current == '\t' || current == '\n' || current == '\r';
    }

    private boolean isDigit() {
        return current >= '0' && current <= '9';
    }

    private boolean isHexDigit() {
        return current >= '0' && current <= '9'
          || current >= 'a' && current <= 'f'
          || current >= 'A' && current <= 'F';
    }

    private boolean isEndOfText() {
        return current == -1;
    }

}
