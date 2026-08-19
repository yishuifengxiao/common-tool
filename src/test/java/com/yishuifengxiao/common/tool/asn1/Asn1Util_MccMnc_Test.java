package com.yishuifengxiao.common.tool.asn1;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * MCC/MNC 编解码单元测试
 *
 * <p>验证 {@link Asn1Util#encodeMccMnc(String, String)} 和
 * {@link Asn1Util#decodeMccMnc(String)} 的正向编码、逆向解码、往返一致性
 * 以及异常处理。</p>
 *
 * @author qingteng
 * @version 1.0.0
 */
@Slf4j
@DisplayName("Asn1Util MCC/MNC编解码测试")
public class Asn1Util_MccMnc_Test {

    // ==================== encodeMccMnc 测试 ====================

    @Nested
    @DisplayName("encodeMccMnc 编码测试")
    class EncodeTest {

        /**
         * 3位MNC的编码
         */
        @Test
        @DisplayName("3位MNC编码-310/013→133010")
        void testEncode_3DigitMnc() {
            String result = Asn1Util.encodeMccMnc("310", "013");
            log.info("encodeMccMnc(310, 013) = {}", result);
            assertEquals("133010", result);
        }

        /**
         * 2位MNC的编码（第三位用0xF填充）
         */
        @Test
        @DisplayName("2位MNC编码-310/01→13F010")
        void testEncode_2DigitMnc() {
            String result = Asn1Util.encodeMccMnc("310", "01");
            log.info("encodeMccMnc(310, 01) = {}", result);
            assertEquals("13F010", result);
        }

        /**
         * 中国移动 MCC=460 MNC=00（2位）
         */
        @Test
        @DisplayName("真实场景-中国移动460/00→64F000")
        void testEncode_ChinaMobile() {
            String result = Asn1Util.encodeMccMnc("460", "00");
            log.info("encodeMccMnc(460, 00) = {}", result);
            assertEquals("64F000", result);
        }

        /**
         * 中国联通 MCC=460 MNC=01（2位）
         */
        @Test
        @DisplayName("真实场景-中国联通460/01→64F010")
        void testEncode_ChinaUnicom() {
            // mccDigits=[4,6,0], mncDigits=[0,1], mnc3=0xF
            // b1=(6<<4)|4=0x64, b2=(0xF<<4)|0=0xF0, b3=(1<<4)|0=0x10
            String result = Asn1Util.encodeMccMnc("460", "01");
            log.info("encodeMccMnc(460, 01) = {}", result);
            assertEquals("64F010", result);
        }

        /**
         * 3位MNC全零
         */
        @Test
        @DisplayName("3位MNC全零-001/000→000100")
        void testEncode_AllZeros_3Digit() {
            // mccDigits=[0,0,1], mncDigits=[0,0,0], mnc3=0
            // b1=(0<<4)|0=0x00, b2=(0<<4)|1=0x01, b3=(0<<4)|0=0x00
            String result = Asn1Util.encodeMccMnc("001", "000");
            log.info("encodeMccMnc(001, 000) = {}", result);
            assertEquals("000100", result);
        }

        /**
         * 2位MNC全零
         */
        @Test
        @DisplayName("2位MNC全零-001/00→00F100")
        void testEncode_AllZeros_2Digit() {
            // mccDigits=[0,0,1], mncDigits=[0,0], mnc3=0xF
            // b1=0x00, b2=(0xF<<4)|1=0xF1, b3=0x00
            String result = Asn1Util.encodeMccMnc("001", "00");
            log.info("encodeMccMnc(001, 00) = {}", result);
            assertEquals("00F100", result);
        }

        /**
         * MCC不是3位 → 抛异常
         */
        @Test
        @DisplayName("MCC非3位抛异常")
        void testEncode_InvalidMccLength() {
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.encodeMccMnc("31", "013"));
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.encodeMccMnc("3100", "013"));
        }

        /**
         * MCC含非数字 → 抛异常
         */
        @Test
        @DisplayName("MCC含非数字抛异常")
        void testEncode_InvalidMccChar() {
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.encodeMccMnc("31A", "013"));
        }

        /**
         * MNC不是2~3位 → 抛异常
         */
        @Test
        @DisplayName("MNC非2~3位抛异常")
        void testEncode_InvalidMncLength() {
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.encodeMccMnc("310", "1"));
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.encodeMccMnc("310", "0133"));
        }

        /**
         * MNC含非数字 → 抛异常
         */
        @Test
        @DisplayName("MNC含非数字抛异常")
        void testEncode_InvalidMncChar() {
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.encodeMccMnc("310", "0A"));
        }

        /**
         * MCC为null → 抛异常
         */
        @Test
        @DisplayName("MCC为null抛异常")
        void testEncode_NullMcc() {
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.encodeMccMnc(null, "013"));
        }

        /**
         * MNC为null → 抛异常
         */
        @Test
        @DisplayName("MNC为null抛异常")
        void testEncode_NullMnc() {
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.encodeMccMnc("310", null));
        }
    }

    // ==================== decodeMccMnc 测试 ====================

    @Nested
    @DisplayName("decodeMccMnc 解码测试")
    class DecodeTest {

        /**
         * 3位MNC的解码
         */
        @Test
        @DisplayName("3位MNC解码-133010→[310,013]")
        void testDecode_3DigitMnc() {
            String[] result = Asn1Util.decodeMccMnc("133010");
            log.info("decodeMccMnc(133010) = [{}, {}]", result[0], result[1]);
            assertArrayEquals(new String[]{"310", "013"}, result);
        }

        /**
         * 2位MNC的解码（0xF填充）
         */
        @Test
        @DisplayName("2位MNC解码-13F010→[310,01]")
        void testDecode_2DigitMnc() {
            String[] result = Asn1Util.decodeMccMnc("13F010");
            log.info("decodeMccMnc(13F010) = [{}, {}]", result[0], result[1]);
            assertArrayEquals(new String[]{"310", "01"}, result);
        }

        /**
         * 小写hex也能正确解码
         */
        @Test
        @DisplayName("小写hex解码-13f010→[310,01]")
        void testDecode_LowerCase() {
            String[] result = Asn1Util.decodeMccMnc("13f010");
            log.info("decodeMccMnc(13f010) = [{}, {}]", result[0], result[1]);
            assertArrayEquals(new String[]{"310", "01"}, result);
        }

        /**
         * 中国移动 64F000 → [460, 00]
         */
        @Test
        @DisplayName("真实场景-中国移动64F000→[460,00]")
        void testDecode_ChinaMobile() {
            String[] result = Asn1Util.decodeMccMnc("64F000");
            log.info("decodeMccMnc(64F000) = [{}, {}]", result[0], result[1]);
            assertArrayEquals(new String[]{"460", "00"}, result);
        }

        /**
         * 全零3位MNC解码
         */
        @Test
        @DisplayName("全零3位MNC解码-000100→[001,000]")
        void testDecode_AllZeros_3Digit() {
            String[] result = Asn1Util.decodeMccMnc("000100");
            log.info("decodeMccMnc(000100) = [{}, {}]", result[0], result[1]);
            assertArrayEquals(new String[]{"001", "000"}, result);
        }

        /**
         * 全零2位MNC解码
         */
        @Test
        @DisplayName("全零2位MNC解码-00F100→[001,00]")
        void testDecode_AllZeros_2Digit() {
            String[] result = Asn1Util.decodeMccMnc("00F100");
            log.info("decodeMccMnc(00F100) = [{}, {}]", result[0], result[1]);
            assertArrayEquals(new String[]{"001", "00"}, result);
        }

        /**
         * 非法MCC数字（nibble > 9）→ 抛异常
         */
        @Test
        @DisplayName("MCC数字无效(nibble>A)抛异常")
        void testDecode_InvalidMccDigit() {
            // b1=0xAB → mcc0=0xB(11), 超过9
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc("AB3010"));
        }

        /**
         * 非法MNC填充值（0xA~0xE）→ 抛异常
         */
        @Test
        @DisplayName("MNC填充值无效(0xA~0xE)抛异常")
        void testDecode_InvalidMncFiller() {
            // b2的高半字节为0xA，既不是0xF(2位)也不是0-9(3位)
            // b2 = 0xA0 → mnc3 = 0xA
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc("13A010"));
        }

        /**
         * 长度不是6位 → 抛异常
         */
        @Test
        @DisplayName("长度非6位抛异常")
        void testDecode_InvalidLength() {
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc("13301"));
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc("1330100"));
        }

        /**
         * 含非hex字符 → 抛异常
         */
        @Test
        @DisplayName("含非hex字符抛异常")
        void testDecode_InvalidChar() {
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc("13301G"));
        }

        /**
         * null → 抛异常
         */
        @Test
        @DisplayName("null抛异常")
        void testDecode_Null() {
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc(null));
        }
    }

    // ==================== 边界异常测试 ====================

    @Nested
    @DisplayName("decodeMccMnc 边界异常测试")
    class EdgeCaseTest {

        // ===== 各 nibble 单独越界（0xA~0xF）=====

        @Test
        @DisplayName("MCC[0]越界(b1低半字节=A)→0A3010抛异常")
        void testMcc0_NibbleOverflow() {
            // b1=0x0A → mcc0=0xA(10) 越界
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc("0A3010"));
        }

        @Test
        @DisplayName("MCC[1]越界(b1高半字节=A)→A03010抛异常")
        void testMcc1_NibbleOverflow() {
            // b1=0xA0 → mcc1=0xA(10) 越界
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc("A03010"));
        }

        @Test
        @DisplayName("MCC[2]越界(b2低半字节=F)→130F10抛异常")
        void testMcc2_NibbleOverflow() {
            // b2=0x0F → mnc3=0(有效3位), mcc2=0xF(15) 越界
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc("130F10"));
        }

        @Test
        @DisplayName("MNC[0]越界(b3低半字节=A)→13300A抛异常")
        void testMnc0_NibbleOverflow() {
            // b3=0x0A → mnc0=0xA(10) 越界
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc("13300A"));
        }

        @Test
        @DisplayName("MNC[1]越界(b3高半字节=A)→1330A0抛异常")
        void testMnc1_NibbleOverflow() {
            // b3=0xA0 → mnc1=0xA(10) 越界
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc("1330A0"));
        }

        // ===== MNC 填充值边界: 0x9(有效) ↔ 0xA~0xE(无效) ↔ 0xF(有效) =====

        @Test
        @DisplayName("MNC填充值=0x9(有效3位MNC末位9)→139010=[310,019]")
        void testMncFiller_Boundary_9_Valid() {
            // b2=0x90 → mnc3=9(3位MNC), mcc2=0
            String[] result = Asn1Util.decodeMccMnc("139010");
            assertArrayEquals(new String[]{"310", "019"}, result);
        }

        @Test
        @DisplayName("MNC填充值=0xA(无效,0x9+1)→13A010抛异常")
        void testMncFiller_Boundary_A_Invalid() {
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc("13A010"));
        }

        @Test
        @DisplayName("MNC填充值=0xB~0xD(全部无效)抛异常")
        void testMncFiller_MiddleRange_Invalid() {
            for (char c : new char[]{'B', 'C', 'D'}) {
                String hex = "13" + c + "010";
                assertThrows(IllegalArgumentException.class,
                        () -> Asn1Util.decodeMccMnc(hex),
                        "MNC filler 0x" + c + " should be invalid");
            }
        }

        @Test
        @DisplayName("MNC填充值=0xE(无效,0xF-1)→13E010抛异常")
        void testMncFiller_Boundary_E_Invalid() {
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc("13E010"));
        }

        @Test
        @DisplayName("MNC填充值=0xF(有效2位MNC填充)→13F010=[310,01]")
        void testMncFiller_Boundary_F_Valid() {
            String[] result = Asn1Util.decodeMccMnc("13F010");
            assertArrayEquals(new String[]{"310", "01"}, result);
        }

        // ===== 极值 =====

        @Test
        @DisplayName("全F(所有nibble=0xF越界)→FFFFFF抛异常")
        void testAllF() {
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc("FFFFFF"));
        }

        @Test
        @DisplayName("全9(所有nibble=9,有效)→999999=[999,999]")
        void testAllNine() {
            String[] result = Asn1Util.decodeMccMnc("999999");
            log.info("decodeMccMnc(999999) = [{}, {}]", result[0], result[1]);
            assertArrayEquals(new String[]{"999", "999"}, result);
        }

        @Test
        @DisplayName("全0(所有nibble=0,有效3位MNC)→000000=[000,000]")
        void testAllZero() {
            String[] result = Asn1Util.decodeMccMnc("000000");
            log.info("decodeMccMnc(000000) = [{}, {}]", result[0], result[1]);
            assertArrayEquals(new String[]{"000", "000"}, result);
        }

        // ===== 空白/长度/格式 =====

        @Test
        @DisplayName("空字符串抛异常")
        void testEmptyString() {
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc(""));
        }

        @Test
        @DisplayName("纯空白字符串抛异常")
        void testWhitespaceOnly() {
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc("   "));
        }

        @Test
        @DisplayName("首尾空格trim后正常解码→'  133010  '=[310,013]")
        void testLeadingTrailingSpaces() {
            String[] result = Asn1Util.decodeMccMnc("  133010  ");
            assertArrayEquals(new String[]{"310", "013"}, result);
        }

        @Test
        @DisplayName("首尾Tab(trim支持char<=32)正常解码")
        void testLeadingTrailingTabs() {
            String[] result = Asn1Util.decodeMccMnc("\t133010\t");
            assertArrayEquals(new String[]{"310", "013"}, result);
        }

        @Test
        @DisplayName("单字符抛异常")
        void testSingleChar() {
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc("1"));
        }

        @Test
        @DisplayName("超长字符串(12位)抛异常")
        void testTooLong() {
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc("133010133010"));
        }

        @Test
        @DisplayName("中间含换行符抛异常")
        void testNewlineInMiddle() {
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc("133\n10"));
        }

        // ===== 复合越界 =====

        @Test
        @DisplayName("mnc3=0xF(2位MNC)但mcc2=9(b2=0xF9)有效→13F910=[319,01]")
        void testMcc2Nine_With2DigitMnc() {
            // b2=0xF9 → mnc3=0xF(2位), mcc2=9
            String[] result = Asn1Util.decodeMccMnc("13F910");
            log.info("decodeMccMnc(13F910) = [{}, {}]", result[0], result[1]);
            assertArrayEquals(new String[]{"319", "01"}, result);
        }

        @Test
        @DisplayName("mnc3=0(3位MNC)且mcc2=0xF(b2=0x0F)越界→130F10抛异常")
        void testMcc2Overflow_WithValid3DigitMnc() {
            // b2=0x0F → mnc3=0(有效3位), mcc2=0xF(越界)
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc("130F10"));
        }

        @Test
        @DisplayName("多个nibble同时越界(仅检第一个)→AF3000抛异常")
        void testMultipleNibbleOverflow() {
            // b1=0xAF → mcc0=0xF(越界), mcc1=0xA(越界)
            assertThrows(IllegalArgumentException.class,
                    () -> Asn1Util.decodeMccMnc("AF3000"));
        }
    }

    // ==================== 往返一致性测试 ====================

    @Nested
    @DisplayName("编解码往返一致性测试")
    class RoundTripTest {

        /**
         * 编码后解码应还原原始值（3位MNC）
         */
        @Test
        @DisplayName("往返一致-3位MNC")
        void testRoundTrip_3DigitMnc() {
            String mcc = "310", mnc = "013";
            String encoded = Asn1Util.encodeMccMnc(mcc, mnc);
            String[] decoded = Asn1Util.decodeMccMnc(encoded);
            log.info("往返测试(3位): {}+{} → {} → {}+{}", mcc, mnc, encoded, decoded[0], decoded[1]);
            assertEquals(mcc, decoded[0], "MCC往返不一致");
            assertEquals(mnc, decoded[1], "MNC往返不一致");
        }

        /**
         * 编码后解码应还原原始值（2位MNC）
         */
        @Test
        @DisplayName("往返一致-2位MNC")
        void testRoundTrip_2DigitMnc() {
            String mcc = "460", mnc = "00";
            String encoded = Asn1Util.encodeMccMnc(mcc, mnc);
            String[] decoded = Asn1Util.decodeMccMnc(encoded);
            log.info("往返测试(2位): {}+{} → {} → {}+{}", mcc, mnc, encoded, decoded[0], decoded[1]);
            assertEquals(mcc, decoded[0], "MCC往返不一致");
            assertEquals(mnc, decoded[1], "MNC往返不一致");
        }

        /**
         * 批量往返测试：多组真实MCC/MNC
         */
        @Test
        @DisplayName("批量往返测试-多组真实MCC/MNC")
        void testRoundTrip_Batch() {
            String[][] cases = {
                    {"310", "013"},   // 美国 T-Mobile
                    {"310", "01"},    // 2位MNC
                    {"460", "00"},    // 中国移动
                    {"460", "01"},    // 中国联通
                    {"460", "011"},   // 中国电信3位
                    {"001", "001"},   // 全1
                    {"999", "999"},   // 全9
                    {"000", "00"},    // 全零2位
                    {"000", "000"},   // 全零3位
                    {"262", "042"},   // 德国
            };

            for (String[] c : cases) {
                String encoded = Asn1Util.encodeMccMnc(c[0], c[1]);
                String[] decoded = Asn1Util.decodeMccMnc(encoded);
                log.info("往返: {}+{} → {} → {}+{}", c[0], c[1], encoded, decoded[0], decoded[1]);
                assertEquals(c[0], decoded[0],
                        String.format("MCC往返不一致: %s→%s→%s", c[0], encoded, decoded[0]));
                assertEquals(c[1], decoded[1],
                        String.format("MNC往返不一致: %s→%s→%s", c[1], encoded, decoded[1]));
            }
        }

        /**
         * 解码后再编码应还原原始hex字符串
         */
        @Test
        @DisplayName("解码后再编码还原原hex串")
        void testReverseRoundTrip() {
            String[] hexCases = {"133010", "13F010", "64F000", "64F010", "000100", "00F100"};

            for (String hex : hexCases) {
                String[] decoded = Asn1Util.decodeMccMnc(hex);
                String reEncoded = Asn1Util.encodeMccMnc(decoded[0], decoded[1]);
                log.info("逆向往返: {} → {}+{} → {}", hex, decoded[0], decoded[1], reEncoded);
                assertEquals(hex, reEncoded,
                        String.format("hex往返不一致: %s→%s+%s→%s", hex, decoded[0], decoded[1], reEncoded));
            }
        }
    }
}
