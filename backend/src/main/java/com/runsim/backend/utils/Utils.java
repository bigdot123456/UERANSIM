package com.runsim.backend.utils;

import com.runsim.backend.exceptions.DecodingException;
import com.runsim.backend.exceptions.EncodingException;
import com.runsim.backend.nas.core.ProtocolEnum;
import com.runsim.backend.utils.bits.Bit;
import com.runsim.backend.utils.bits.BitN;
import com.runsim.backend.utils.octets.OctetN;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Utils {

    public static <T> T[] decodeList(OctetInputStream stream, Function<OctetInputStream, T> decoder, int length) {
        int readLen = 0;
        var res = new ArrayList<T>();
        while (readLen < length) {
            int streamIndex = stream.currentIndex();
            res.add(decoder.apply(stream));
            readLen += stream.currentIndex() - streamIndex;
        }
        if (readLen > length)
            throw new DecodingException("Value length exceeds total length!");
        return (T[]) res.toArray();
    }

    public static int[] fixedBitsToOctetArray(Bit[][] bits) {
        int length = 0;
        for (int i = 0; i < bits.length; i++) {
            for (Bit bit : bits[i]) {
                if (bit != null) {
                    length = Math.max(length, i + 1);
                }
            }
        }

        int[] octets = new int[length];
        for (int i = 0; i < length; i++) {
            int octet = 0;

            for (int j = 0; j < 8; j++) {
                var bit = bits[i][j];
                if (bit == null) {
                    throw new EncodingException(j + "th bit of the " + i
                            + "th octet should not have be null, because that octet contains at least one bit which is not null.");
                }
                octet |= bit.intValue();
                octet <<= 1;
            }

            octets[i] = octet >> 1;
        }

        return octets;
    }

    public static byte[] hexStringToByteArray(String s) {
        if (!isValidHexString(s))
            throw new IllegalArgumentException("hex string contains invalid characters or has bad format");

        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static boolean isValidHexString(String s) {
        if (s == null || s.length() % 2 != 0)
            return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') continue;
            if (c >= 'a' && c <= 'f') continue;
            if (c >= 'A' && c <= 'F') continue;
            return false;
        }
        return true;
    }

    public static String byteArrayToHexString(byte[] bytes) {
        final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] getResourceFile(String name) {
        try (var stream = Utils.class.getClassLoader().getResourceAsStream(name)) {
            if (stream == null)
                throw new RuntimeException("resource not found: " + name);
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getResourceString(String name) {
        return new String(getResourceFile(name), StandardCharsets.UTF_8);
    }

    public static InputStream getResourceStream(String name) {
        return Utils.class.getClassLoader().getResourceAsStream(name);
    }

    public static String normalizeXml(String xml) {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            var builder = factory.newDocumentBuilder();
            var is = new InputSource(new StringReader(xml));
            var document = builder.parse(is);
            document.normalizeDocument();
            trimWhitespace(document.getDocumentElement());
            return nodeToString(document.getDocumentElement(), true, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String indentXml(String xml) {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            var builder = factory.newDocumentBuilder();
            var is = new InputSource(new StringReader(xml));
            var document = builder.parse(is);
            return nodeToString(document.getDocumentElement(), true, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void trimWhitespace(Node node) {
        var children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE)
                child.setTextContent(child.getTextContent().trim());
            trimWhitespace(child);
        }
    }

    private static String nodeToString(Node node, boolean omitXmlDeclaration, boolean indent) {
        var sw = new StringWriter();
        try {
            var t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXmlDeclaration ? "yes" : "no");
            t.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
            t.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sw.toString();
    }

    public static String padLeft(String string, int totalLength, char character) {
        int need = totalLength - string.length();
        if (need <= 0) return string;
        return String.valueOf(character).repeat(need) + string;
    }

    public static String padRight(String string, int totalLength, char character) {
        int need = totalLength - string.length();
        if (need <= 0) return string;
        return string + String.valueOf(character).repeat(need);
    }

    public static String insertSpaces(String string, int period) {
        if (period < 1) throw new IllegalArgumentException();
        if (string.length() % period != 0) throw new IllegalArgumentException();

        var sb = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            sb.append(string.charAt(i));
            if ((i + 1) % period == 0) sb.append(' ');
        }
        return sb.toString().trim();
    }

    public static boolean unsignedEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        long la = 0, lb = 0;
        boolean difa = false, difb = false;

        if (a instanceof Boolean) la = (boolean) a ? 1 : 0;
        else if (a instanceof Byte) la = (long) (byte) a;
        else if (a instanceof Short) la = (long) (short) a;
        else if (a instanceof Integer) la = (long) (int) a;
        else if (a instanceof Long) la = (long) a;
        else if (a instanceof BitN) la = ((BitN) a).intValue();
        else if (a instanceof OctetN) la = ((OctetN) a).longValue();
        else if (a instanceof ProtocolEnum) la = ((ProtocolEnum) a).intValue();
        else difa = true;

        if (b instanceof Boolean) lb = (boolean) b ? 1 : 0;
        else if (b instanceof Byte) lb = (long) (byte) b;
        else if (b instanceof Short) lb = (long) (short) b;
        else if (b instanceof Integer) lb = (long) (int) b;
        else if (b instanceof Long) lb = (long) b;
        else if (b instanceof BitN) lb = ((BitN) b).intValue();
        else if (b instanceof OctetN) lb = ((OctetN) b).longValue();
        else if (b instanceof ProtocolEnum) lb = ((ProtocolEnum) b).intValue();
        else difb = true;

        if (difa && difb) throw new IllegalArgumentException();
        if (difa || difb) return false;

        return la == lb;
    }

    public static <T> List<T> streamToList(Stream<T> stream) {
        return stream.collect(Collectors.toList());
    }

    /**
     * Padded left, and uses big endian
     */
    public static long toLong(byte[] arr) {
        long value = 0;
        for (int i = 0; i < arr.length; i++) {
            value |= arr[i] & 0xFF;
            if (i != arr.length - 1)
                value <<= 8;
        }
        return value;
    }

    /**
     * Padded left, and uses big endian
     */
    public static long toLong(String hex) {
        return toLong(hexStringToByteArray(hex));
    }

    /**
     * Padded left, and uses big endian
     */
    public static int toInt(byte[] arr) {
        int value = 0;
        for (int i = 0; i < arr.length; i++) {
            value |= arr[i] & 0xFF;
            if (i != arr.length - 1)
                value <<= 8;
        }
        return value;
    }

    /**
     * Padded left, and uses big endian
     */
    public static int toInt(String hex) {
        return toInt(hexStringToByteArray(hex));
    }
}
