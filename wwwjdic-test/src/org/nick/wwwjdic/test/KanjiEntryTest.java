package org.nick.wwwjdic.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.nick.wwwjdic.KanjiEntry;
import org.nick.wwwjdic.history.FavoritesEntryParser;

public class KanjiEntryTest {

    private static final String KANJIDIC_PATH = "/home/nick/android/wwwjdic/wwwjdic-test/dict/kanjidic";

    @Test
    public void testParse() {
        String kanjidicStr = "違 3063 U9055 B162 G8 S13 S12 F344 J2 N4720 V6099 H3151 DK2014 L1644 K496 DO883 MN39067P MP11.0144 E1006 IN814 DF965 DC274 DJ385 DG713 DM1659 P3-3-10 I2q10.5 Q3430.4 DR1655 ZSP3-2-10 Ywei2 Wwi イ ちが.う ちが.い ちが.える -ちが.える たが.う たが.える {difference} {differ} ";
        KanjiEntry entry = KanjiEntry.parseKanjidic(kanjidicStr);
        assertNotNull(entry);

        String[] fields = FavoritesEntryParser.toParsedStringArray(entry, "\n");
        assertEquals("違", entry.getKanji());
        assertEquals("違", fields[0]);
        assertNull(entry.getClassicalRadicalNumber());
        assertEquals(344, entry.getFrequncyeRank().intValue());
        assertEquals("344", fields[11]);
        assertEquals(8, entry.getGrade().intValue());
        assertEquals("8", fields[12]);
        assertEquals("3063", entry.getJisCode());
        assertEquals("3063", fields[9]);
        assertEquals(2, entry.getJlptLevel().intValue());
        assertEquals("2", fields[13]);
        assertEquals("wi", entry.getKoreanReading());
        assertEquals("wi", fields[15]);
        assertEquals("wei2", entry.getPinyin());
        assertEquals("wei2", fields[16]);
        assertEquals(162, entry.getRadicalNumber());
        assertEquals("162", fields[5]);
        assertEquals("3-3-10", entry.getSkipCode());
        assertEquals("3-3-10", fields[14]);
        assertEquals(13, entry.getStrokeCount());
        assertEquals("13", fields[6]);
        assertEquals("9055", entry.getUnicodeNumber());
        assertEquals("9055", fields[10]);

        assertEquals("イ ちが.う ちが.い ちが.える -ちが.える たが.う たが.える", entry.getReading());
        assertEquals("イ", entry.getOnyomi());
        assertEquals("イ", fields[1]);
        assertEquals("ちが.う ちが.い ちが.える -ちが.える たが.う たが.える", entry.getKunyomi());
        assertEquals("ちが.う ちが.い ちが.える -ちが.える たが.う たが.える", fields[2]);
        assertEquals(2, entry.getMeanings().size());
        assertEquals("difference", entry.getMeanings().get(0));
        assertEquals("differ", entry.getMeanings().get(1));
    }

    @Test
    public void testParseNanori() {
        String kanjidicStr = "阿 3024 U963f B170 G9 S8 XN5008 F1126 J1 N4985 V6435 H346 DK256 L1295 K1515 O569 MN41599 MP11.0798 IN2258 DM1304 P1-3-5 I2d5.6 Q7122.0 Ya1 Ye1 Ya5 Ya2 Ya4 Wa Wog ア オ おもね.る くま T1 ほとり あず あわ おか きた な {Africa} {flatter} {fawn upon} {corner} {nook} {recess}";
        KanjiEntry entry = KanjiEntry.parseKanjidic(kanjidicStr);
        assertNotNull(entry);

        assertEquals("阿", entry.getKanji());
        assertNull(entry.getClassicalRadicalNumber());
        assertEquals(1126, entry.getFrequncyeRank().intValue());
        assertEquals(9, entry.getGrade().intValue());
        assertEquals("3024", entry.getJisCode());
        assertEquals(1, entry.getJlptLevel().intValue());
        // assertEquals("a og ", entry.getKoreanReading());
        // assertEquals("a1 e1 a5 a2 a4", entry.getPinyin());
        assertEquals(170, entry.getRadicalNumber());
        assertEquals("1-3-5", entry.getSkipCode());
        assertEquals(8, entry.getStrokeCount());
        assertEquals("963f", entry.getUnicodeNumber());

        assertEquals("ア オ おもね.る くま ほとり あず あわ おか きた な", entry.getReading());
        assertEquals("ア オ", entry.getOnyomi());
        assertEquals("おもね.る くま", entry.getKunyomi());
        assertEquals(6, entry.getMeanings().size());
        assertEquals("ほとり あず あわ おか きた な", entry.getNanori());
        assertEquals("Africa", entry.getMeanings().get(0));
        assertEquals("flatter", entry.getMeanings().get(1));
        assertEquals("fawn upon", entry.getMeanings().get(2));
        assertEquals("corner", entry.getMeanings().get(3));
        assertEquals("nook", entry.getMeanings().get(4));
        assertEquals("recess", entry.getMeanings().get(5));
    }

    @Test
    public void testParseNanoriNoKunyomi() {
        String kanjidicStr = "絢 303C U7d62 B120 G9 S12 F2315 J1 N3530 V4481 H1347 DK911 L2664 O2150 MN27427 MP8.1051 IN2194 P1-6-6 I6a6.14 Q2792.0 Yxuan4 Whyeon ケン T1 じゅん あや {kimono design}";
        KanjiEntry entry = KanjiEntry.parseKanjidic(kanjidicStr);
        assertNotNull(entry);

        assertEquals("絢", entry.getKanji());
        assertNull(entry.getClassicalRadicalNumber());
        assertEquals(2315, entry.getFrequncyeRank().intValue());
        assertEquals(9, entry.getGrade().intValue());
        assertEquals("303C", entry.getJisCode());
        assertEquals(1, entry.getJlptLevel().intValue());
        assertEquals("hyeon", entry.getKoreanReading());
        assertEquals("xuan4", entry.getPinyin());
        assertEquals(120, entry.getRadicalNumber());
        assertEquals("1-6-6", entry.getSkipCode());
        assertEquals(12, entry.getStrokeCount());
        assertEquals("7d62", entry.getUnicodeNumber());

        assertEquals("ケン じゅん あや", entry.getReading());
        assertEquals("ケン", entry.getOnyomi());
        assertEquals("", entry.getKunyomi());
        assertEquals("じゅん あや", entry.getNanori());
        assertEquals(1, entry.getMeanings().size());
        assertEquals("kimono design", entry.getMeanings().get(0));
    }

    @Test
    public void testParseNanoriLong() {
        String kanjidicStr = "生 4038 U751f B100 G1 S5 F29 J4 N2991 V3715 H3497 DK2179 L1555 K29 O214 DO67 MN21670 MP7.1027 E42 IN44 DS34 DF71 DH44 DT43 DC9 DJ49 DB2.4 DG1327 DM1569 P4-5-2 I0a5.29 Q2510.0 DR2472 Ysheng1 Wsaeng セイ ショウ い.きる い.かす い.ける う.まれる うま.れる う.まれ うまれ う.む お.う は.える は.やす き なま なま- な.る な.す む.す -う T1 あさ いき いく いけ うぶ うまい え おい ぎゅう くるみ ごせ さ じょう すぎ そ そう ちる なば にう にゅう ふ み もう よい りゅう {life} {genuine} {birth}";
        KanjiEntry entry = KanjiEntry.parseKanjidic(kanjidicStr);
        assertNotNull(entry);

        assertEquals("生", entry.getKanji());
        assertNull(entry.getClassicalRadicalNumber());
        assertEquals(29, entry.getFrequncyeRank().intValue());
        assertEquals(1, entry.getGrade().intValue());
        assertEquals("4038", entry.getJisCode());
        assertEquals(4, entry.getJlptLevel().intValue());
        assertEquals("saeng", entry.getKoreanReading());
        assertEquals("sheng1", entry.getPinyin());
        assertEquals(100, entry.getRadicalNumber());
        assertEquals("4-5-2", entry.getSkipCode());
        assertEquals(5, entry.getStrokeCount());
        assertEquals("751f", entry.getUnicodeNumber());
        assertEquals(
                "セイ ショウ い.きる い.かす い.ける う.まれる うま.れる う.まれ うまれ う.む お.う は.える は.やす き なま なま- な.る な.す む.す -う あさ いき いく いけ うぶ うまい え おい ぎゅう くるみ ごせ さ じょう すぎ そ そう ちる なば にう にゅう ふ み もう よい りゅう",
                entry.getReading());
        assertEquals("セイ ショウ", entry.getOnyomi());
        assertEquals(
                "い.きる い.かす い.ける う.まれる うま.れる う.まれ うまれ う.む お.う は.える は.やす き なま なま- な.る な.す む.す -う",
                entry.getKunyomi());
        assertEquals(
                "あさ いき いく いけ うぶ うまい え おい ぎゅう くるみ ごせ さ じょう すぎ そ そう ちる なば にう にゅう ふ み もう よい りゅう",
                entry.getNanori());
        assertEquals(3, entry.getMeanings().size());
        assertEquals("life", entry.getMeanings().get(0));
        assertEquals("genuine", entry.getMeanings().get(1));
        assertEquals("birth", entry.getMeanings().get(2));
    }

    @Test
    public void testParseMultipleKoreanPinyin() {
        String kanjidicStr = "阿 3024 U963f B170 G9 S8 XN5008 F1126 J1 N4985 V6435 H346 DK256 L1295 K1515 O569 MN41599 MP11.0798 IN2258 DM1304 P1-3-5 I2d5.6 Q7122.0 Ya1 Ye1 Ya5 Ya2 Ya4 Wa Wog ア オ おもね.る くま T1 ほとり あず あわ おか きた な {Africa} {flatter} {fawn upon} {corner} {nook} {recess}";
        KanjiEntry entry = KanjiEntry.parseKanjidic(kanjidicStr);
        assertNotNull(entry);

        assertEquals("阿", entry.getKanji());
        assertNull(entry.getClassicalRadicalNumber());
        assertEquals(1126, entry.getFrequncyeRank().intValue());
        assertEquals(9, entry.getGrade().intValue());
        assertEquals("3024", entry.getJisCode());
        assertEquals(1, entry.getJlptLevel().intValue());
        assertEquals("a og", entry.getKoreanReading());
        assertEquals("a1 e1 a5 a2 a4", entry.getPinyin());
        assertEquals(170, entry.getRadicalNumber());
        assertEquals("1-3-5", entry.getSkipCode());
        assertEquals(8, entry.getStrokeCount());
        assertEquals("963f", entry.getUnicodeNumber());
    }

    @Test
    public void testParseNoPinyin() {
        String kanjidicStr = "椛 3371 U691b B75 G9 S11 V2683 O1363 MN15065X MP6.0420 P1-4-7 I4a7.14 Q4491.4 Whwa かば もみじ {birch} {maple}";
        KanjiEntry entry = KanjiEntry.parseKanjidic(kanjidicStr);
        assertNotNull(entry);

        assertEquals("椛", entry.getKanji());
        assertEquals("hwa", entry.getKoreanReading());
        assertNull(entry.getPinyin());
        assertEquals("", entry.getOnyomi());
        assertEquals("かば もみじ", entry.getKunyomi());
        assertEquals("", entry.getNanori());
        assertEquals(2, entry.getMeanings().size());
        assertEquals("birch", entry.getMeanings().get(0));
        assertEquals("maple", entry.getMeanings().get(1));
    }

    @Test
    public void testParseRadicalName() {
        String kanjidicStr = "巛 565F U5ddb B47 S3 V1527 H9 MN8669 MP4.0326 P1-1-2 I0a3.2 Q2233.7 Ychuan1 Wcheon セン かわ T2 まがりがわ {curving river radical (no.47)}";
        KanjiEntry entry = KanjiEntry.parseKanjidic(kanjidicStr);
        assertNotNull(entry);

        assertEquals("巛", entry.getKanji());
        assertNull(entry.getClassicalRadicalNumber());
        assertNull(entry.getFrequncyeRank());
        assertNull(entry.getGrade());
        assertEquals("565F", entry.getJisCode());
        assertNull(entry.getJlptLevel());
        assertEquals("cheon", entry.getKoreanReading());
        assertEquals("chuan1", entry.getPinyin());
        assertEquals(47, entry.getRadicalNumber());
        assertEquals("1-1-2", entry.getSkipCode());
        assertEquals(3, entry.getStrokeCount());
        assertEquals("5ddb", entry.getUnicodeNumber());

        assertEquals("セン かわ まがりがわ", entry.getReading());
        assertEquals("セン", entry.getOnyomi());
        assertEquals("かわ", entry.getKunyomi());
        assertEquals("", entry.getNanori());
        assertEquals("まがりがわ", entry.getRadicalName());
        assertEquals(1, entry.getMeanings().size());
        assertEquals("curving river radical (no.47)", entry.getMeanings()
                .get(0));
    }

    @Test
    public void testParseNanoriRadicalName() {
        String kanjidicStr = "工 3929 U5de5 B48 G2 S3 F299 J3 N1451 V1532 H3381 DK2118 L76 K169 O39 DO77 MN8714 MP4.0340 E113 IN139 DS71 DF161 DH125 DT84 DC173 DJ379 DB2.16 DG550 DM76 P4-3-1 I0a3.6 Q1010.0 DR3172 Ygong1 Wgong コウ ク グ T1 もく T2 たくみ {craft} {construction} {katakana e radical (no. 48)}";
        KanjiEntry entry = KanjiEntry.parseKanjidic(kanjidicStr);
        assertNotNull(entry);

        assertEquals("工", entry.getKanji());
        assertNull(entry.getClassicalRadicalNumber());
        assertEquals(299, entry.getFrequncyeRank().intValue());
        assertEquals(2, entry.getGrade().intValue());
        assertEquals("3929", entry.getJisCode());
        assertEquals(3, entry.getJlptLevel().intValue());
        assertEquals("gong", entry.getKoreanReading());
        assertEquals("gong1", entry.getPinyin());
        assertEquals(48, entry.getRadicalNumber());
        assertEquals("4-3-1", entry.getSkipCode());
        assertEquals(3, entry.getStrokeCount());
        assertEquals("5de5", entry.getUnicodeNumber());

        assertEquals("コウ ク グ もく たくみ", entry.getReading());
        assertEquals("コウ ク グ", entry.getOnyomi());
        assertEquals("", entry.getKunyomi());
        assertEquals("もく", entry.getNanori());
        assertEquals("たくみ", entry.getRadicalName());
        assertEquals(3, entry.getMeanings().size());
        assertEquals("craft", entry.getMeanings().get(0));
        assertEquals("construction", entry.getMeanings().get(1));
        assertEquals("katakana e radical (no. 48)", entry.getMeanings().get(2));
    }

    @Test
    public void testKanjidic() throws Exception {
        FileInputStream fis = new FileInputStream(KANJIDIC_PATH);
        List<String> lines = new ArrayList<String>();
        BufferedReader r = new BufferedReader(new InputStreamReader(fis,
                "EUC-JP"));
        String line = null;

        while ((line = r.readLine()) != null) {
            lines.add(line);
        }

        r.close();

        for (String l : lines) {
            if (l.charAt(0) == '#') {
                continue;
            }

            System.out.println("parsing : " + l);
            KanjiEntry entry = KanjiEntry.parseKanjidic(l);
            assertNotNull(entry);
            assertNotNull(entry.getKanji());
            assertNotNull(entry.getReading());
            // assertFalse(entry.getMeanings().isEmpty());
            String[] fields = FavoritesEntryParser.toParsedStringArray(entry,
                    "\n");
            assertNotNull(fields);
            assertEquals(17, fields.length);
        }

    }
}
