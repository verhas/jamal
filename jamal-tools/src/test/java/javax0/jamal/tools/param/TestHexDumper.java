package javax0.jamal.tools.param;

import javax0.jamal.tools.HexDumper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

public class TestHexDumper {

    @Test
    @DisplayName("HexDumper can encode zero bytes to empty string")
    void dumpZeroBytes(){
        Assertions.assertEquals("",HexDumper.encode(new byte[0]));
    }

    @Test
    @DisplayName("HexDumper can encode one byte to two characters string")
    void dumpOneByte(){
        Assertions.assertEquals("00",HexDumper.encode(new byte[]{ 0x00}));
        Assertions.assertEquals("01",HexDumper.encode(new byte[]{ 0x01}));
        Assertions.assertEquals("02",HexDumper.encode(new byte[]{ 0x02}));
        Assertions.assertEquals("03",HexDumper.encode(new byte[]{ 0x03}));
        Assertions.assertEquals("04",HexDumper.encode(new byte[]{ 0x04}));
        Assertions.assertEquals("05",HexDumper.encode(new byte[]{ 0x05}));
        Assertions.assertEquals("06",HexDumper.encode(new byte[]{ 0x06}));
        Assertions.assertEquals("07",HexDumper.encode(new byte[]{ 0x07}));
        Assertions.assertEquals("08",HexDumper.encode(new byte[]{ 0x08}));
        Assertions.assertEquals("09",HexDumper.encode(new byte[]{ 0x09}));
        Assertions.assertEquals("0a",HexDumper.encode(new byte[]{ 0x0a}));
        Assertions.assertEquals("0b",HexDumper.encode(new byte[]{ 0x0b}));
        Assertions.assertEquals("0c",HexDumper.encode(new byte[]{ 0x0c}));
        Assertions.assertEquals("0d",HexDumper.encode(new byte[]{ 0x0d}));
        Assertions.assertEquals("0e",HexDumper.encode(new byte[]{ 0x0e}));
        Assertions.assertEquals("0f",HexDumper.encode(new byte[]{ 0x0f}));

        Assertions.assertEquals("00",HexDumper.encode(new byte[]{ 0x00}));
        Assertions.assertEquals("10",HexDumper.encode(new byte[]{ 0x10}));
        Assertions.assertEquals("20",HexDumper.encode(new byte[]{ 0x20}));
        Assertions.assertEquals("30",HexDumper.encode(new byte[]{ 0x30}));
        Assertions.assertEquals("40",HexDumper.encode(new byte[]{ 0x40}));
        Assertions.assertEquals("50",HexDumper.encode(new byte[]{ 0x50}));
        Assertions.assertEquals("60",HexDumper.encode(new byte[]{ 0x60}));
        Assertions.assertEquals("70",HexDumper.encode(new byte[]{ 0x70}));
        Assertions.assertEquals("80",HexDumper.encode(new byte[]{ (byte)0x80}));
        Assertions.assertEquals("90",HexDumper.encode(new byte[]{ (byte)0x90}));
        Assertions.assertEquals("a0",HexDumper.encode(new byte[]{ (byte)0xa0}));
        Assertions.assertEquals("b0",HexDumper.encode(new byte[]{ (byte)0xb0}));
        Assertions.assertEquals("c0",HexDumper.encode(new byte[]{ (byte)0xc0}));
        Assertions.assertEquals("d0",HexDumper.encode(new byte[]{ (byte)0xd0}));
        Assertions.assertEquals("e0",HexDumper.encode(new byte[]{ (byte)0xe0}));
        Assertions.assertEquals("f0",HexDumper.encode(new byte[]{ (byte)0xf0}));
    }

    @Test
    @DisplayName("HexDumper can encode many bytes")
    void dumpManyBytes() {
        Assertions.assertEquals("000102030405060708090a", HexDumper.encode(new byte[]{0x00, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}));
    }
}
