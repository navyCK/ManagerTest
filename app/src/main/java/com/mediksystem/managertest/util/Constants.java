package com.mediksystem.managertest.util;

import java.nio.ByteOrder;
import java.util.regex.Pattern;

public class Constants {
    public static final Pattern[] BASE_TARGET_FOR_REPLACE = {Pattern.compile("\\\\"), Pattern.compile("\r\n|\n|\r"), Pattern.compile("\t"), Pattern.compile("\"")};
    public static final String[] BASE_RESULT_FOR_REPLACE = {"\\\\\\\\", "\\\\n", "\\\\t", "\\\\\""};

    public static final int BUFFER_SIZE = 8*1024*1024;

    public static String[] BYTE_TO_HEXA_STRING_UPPER = new String[] {
            "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F",
            "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1A", "1B", "1C", "1D", "1E", "1F",
            "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2A", "2B", "2C", "2D", "2E", "2F",
            "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3A", "3B", "3C", "3D", "3E", "3F",
            "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4A", "4B", "4C", "4D", "4E", "4F",
            "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5A", "5B", "5C", "5D", "5E", "5F",
            "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6A", "6B", "6C", "6D", "6E", "6F",
            "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "7A", "7B", "7C", "7D", "7E", "7F",
            "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8A", "8B", "8C", "8D", "8E", "8F",
            "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9A", "9B", "9C", "9D", "9E", "9F",
            "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "AA", "AB", "AC", "AD", "AE", "AF",
            "B0", "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "BA", "BB", "BC", "BD", "BE", "BF",
            "C0", "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "CA", "CB", "CC", "CD", "CE", "CF",
            "D0", "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "DA", "DB", "DC", "DD", "DE", "DF",
            "E0", "E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8", "E9", "EA", "EB", "EC", "ED", "EE", "EF",
            "F0", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "FA", "FB", "FC", "FD", "FE", "FF"
    };

    public static final ByteTransfer BYTE_TRANSFER = ByteOrder.nativeOrder()==ByteOrder.BIG_ENDIAN ? ByteTransfer.BIG_ENDIAN : ByteTransfer.LITTLE_ENDIAN;

    public static final String REPLACE_BACKSLASH = "\\\\";
    public static final String REPLACE_TAB = "\\t";
    public static final String REPLACE_CARRIAGE_RETURN = "\\n";
    public static final String REPLACE_QUOTE = "\\\"";

    public static Pattern PHONE_FORMAT = Pattern.compile("[0-9]{2,3}-[0-9]{3,5}-[0-9]{4}");
    public static Pattern PHONE_FORMAT2 = Pattern.compile("[0-9]{3,5}-[0-9]{4}");
    public static Pattern EMAIL_FORMAT = Pattern.compile("[0-9a-zA-Z_\\-\\.]+@[0-9a-zA-Z]+\\.[a-zA-Z]{2,4}");
    public static Pattern ID_FORMAT = Pattern.compile("[a-z0-9]{2,29}$");
    public static Pattern NOT_PASSWORD_POSSIBLE_CHARACTOR = Pattern.compile("[^a-zA-Z0-9\\{\\}\\[\\]\\/?\\.,;:|\\)*~`!^\\-_+<>@\\#$%&\\\\\\=\\(\\'\\\"]");
    public static Pattern ENGLISH = Pattern.compile("[a-zA-Z]");
    public static Pattern ONLY_ENGLISH = Pattern.compile("[a-zA-Z]*");
    public static Pattern ONLY_NUMBER = Pattern.compile("[0-9]*");
    public static Pattern HAS_PASSWORD_SYMBOL = Pattern.compile("[\\\\{\\}\\[\\]\\/?.,;:|\\)*~`!^\\-_+<>@\\#$%&\\\\\\=\\(\\'\\\"]");
    public static final int START = 0xAC00;
    public static final int END = 0xD7A3;
    public static final char F1[] = {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};
    public static final char F2[] = {'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'};
    public static final char F3[] = {(char)-1, 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};
    public static final int F2F[] = {-1, -1, -1, -1, -1, -1, -1, -1, -1, 8, 8, 8, -1, -1, 13, 13, 13, -1, -1, 18, -1};
    public static final int F3F[] = {-1, -1, -1, 1, -1, 4, 4, -1, -1, 8, 8, 8, 8, 8, 8, 8, -1, -1, 17, -1, -1, -1, -1, -1, -1, -1, -1, -1};


    public static final char DELIMITER = '=';
    public static final char STRING_DELIMETER = '"';

    public static final char CODE_START_CHAR = '<';
    public static final char CODE_END_CHAR = '>';

    public static final String CODE_FOR_NULL = "null";

    public static final String FAIL_TO_CREATE_NEW_FILE = "파일생성 실패";
    public static final String IMPOSSIBLE_TO_USE_FILE = "파일을 사용할 수 없습니다.";
    public static final String WITHIN_DATA_LOADING = "데이터 로딩중에";
    public static final String NOT_SUPPORTED_DATA = "지원하지 않는 데이터";
    public static final String TOO_LONG_DATA = "문자치고는 데이터 길이가 너무김";
    public static final String NO_DATA = "데이터 없음";
    public static final String INCONSISTENT_TYPE = "타입 불일치";
}
