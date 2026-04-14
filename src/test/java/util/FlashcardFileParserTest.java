package util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlashcardFileParserTest {

    @Test
    void parseCsv_skipsHeaderAndHandlesQuotedComma() throws Exception {
        Path file = Files.createTempFile("flashcards", ".csv");
        Files.write(file, List.of(
                "term,definition",
                "\"CPU\",\"Central, Processing Unit\"",
                "",
                "RAM,Random Access Memory",
                "invalid-line"
        ), StandardCharsets.UTF_8);

        List<FlashcardFileParser.ParsedCard> cards = FlashcardFileParser.parse(file.toFile());

        assertEquals(2, cards.size());
        assertEquals("CPU", cards.get(0).term());
        assertEquals("Central, Processing Unit", cards.get(0).definition());
        assertEquals("RAM", cards.get(1).term());
        assertEquals("Random Access Memory", cards.get(1).definition());
    }

    @Test
    void parseTsv_andPipeFiles_useCorrectDelimiter() throws Exception {
        Path tsv = Files.createTempFile("flashcards", ".tsv");
        Files.write(tsv, List.of(
                "term\tdefinition",
                "OOP\tObject-Oriented Programming"
        ), StandardCharsets.UTF_8);

        Path pipe = Files.createTempFile("flashcards", ".pipe");
        Files.write(pipe, List.of(
                "term|definition",
                "API|Application Programming Interface"
        ), StandardCharsets.UTF_8);

        List<FlashcardFileParser.ParsedCard> tsvCards = FlashcardFileParser.parse(tsv.toFile());
        List<FlashcardFileParser.ParsedCard> pipeCards = FlashcardFileParser.parse(pipe.toFile());

        assertEquals(1, tsvCards.size());
        assertEquals("OOP", tsvCards.get(0).term());
        assertEquals("Object-Oriented Programming", tsvCards.get(0).definition());

        assertEquals(1, pipeCards.size());
        assertEquals("API", pipeCards.get(0).term());
        assertEquals("Application Programming Interface", pipeCards.get(0).definition());
    }

    @Test
    void parsePsvAndUnknownExtension_skipBlankValuesAndUseFirstTwoColumns() throws Exception {
        Path psv = Files.createTempFile("flashcards", ".psv");
        Files.write(psv, List.of(
                "term|definition",
                "CLI|Command Line Interface|ignored"
        ), StandardCharsets.UTF_8);

        Path txt = Files.createTempFile("flashcards", ".txt");
        Files.write(txt, List.of(
                "Question,Answer",
                "Term,Definition",
                "Valid,Entry",
                ",missing term",
                "missing definition,   "
        ), StandardCharsets.UTF_8);

        List<FlashcardFileParser.ParsedCard> psvCards = FlashcardFileParser.parse(psv.toFile());
        List<FlashcardFileParser.ParsedCard> txtCards = FlashcardFileParser.parse(txt.toFile());

        assertEquals(1, psvCards.size());
        assertEquals("CLI", psvCards.get(0).term());
        assertEquals("Command Line Interface|ignored", psvCards.get(0).definition());

        assertEquals(3, txtCards.size());
        assertEquals("Question", txtCards.get(0).term());
        assertEquals("Answer", txtCards.get(0).definition());
        assertEquals("Term", txtCards.get(1).term());
        assertEquals("Definition", txtCards.get(1).definition());
        assertEquals("Valid", txtCards.get(2).term());
        assertEquals("Entry", txtCards.get(2).definition());
    }
}
