import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AssassinTest {
    private final ByteArrayOutputStream outContent;
    private final PrintStream tempOut;

    public AssassinTest() {
        outContent = new ByteArrayOutputStream();
        tempOut = new PrintStream(outContent);
    }

    @Test
    void testConstructor() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AssassinManager(List.of());
        });
    }
    @ParameterizedTest
    @MethodSource
    void testPrint(List<String> victims, Queue<String> remaining, Queue<String> graveyard) {
        System.setOut(tempOut);
        AssassinManager manager = kill(victims);
        if (remaining.size() > 0) {
            manager.printKillRing();
            outContent.toString().lines().forEach(line -> {
                assertEquals(remaining.remove(), line);
            });
        }
        outContent.reset();
        manager.printGraveyard();
        outContent.toString().lines().forEach(line -> {
            assertEquals(graveyard.remove(), line);
        });
        outContent.reset();
        System.setOut(System.out);
    }

    @ParameterizedTest
    @MethodSource
    void testContains(List<String> victims) {
        AssassinManager manager = kill(victims);
        for (String victim: victims) {
            assertFalse(manager.killRingContains(victim));
            assertTrue(manager.graveyardContains(victim));
        }
    }

    @ParameterizedTest
    @MethodSource
    void testWinner(List<String> victims, String winner) {
        AssassinManager manager = kill(victims);
        assertEquals(winner, manager.winner());
    }

    @ParameterizedTest
    @MethodSource("testWinner")
    void testGameOver(List<String> victims, String winner) {
        AssassinManager manager = kill(victims);
        assertTrue(winner != null == manager.isGameOver());
    }

    @ParameterizedTest
    @MethodSource()
    void testKillExceptions(List<String> victims, String killer) {
        AssassinManager manager = kill(victims);
        if(manager.isGameOver()) {
            assertThrows(IllegalStateException.class, () -> {
                manager.kill(killer);
            });
        } else {
            assertThrows(IllegalArgumentException.class,() -> {
                manager.kill(killer);
            });
        }
    }

    private AssassinManager kill(List<String> victims) {
        List<String> list = getList();
        AssassinManager manager = new AssassinManager(list);
        for (String victim : victims) {
            manager.kill(victim);
        }
        return manager;
    }

    private ArrayList<String> getList() {
        ArrayList<String> list = new ArrayList<>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File("names.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (scanner.hasNextLine()) {
            list.add(scanner.nextLine().trim());
        }
        return list;
    }

    private static Stream<Arguments> testPrint() {
        return Stream.of(
                Arguments.of(
                    List.of(),
                    new LinkedList<>(List.of(
                        "    Don Knuth is stalking Alan Turing",
                        "    Alan Turing is stalking Ada Lovelace",
                        "    Ada Lovelace is stalking Charles Babbage",
                        "    Charles Babbage is stalking John von Neumann",
                        "    John von Neumann is stalking Grace Hopper",
                        "    Grace Hopper is stalking Bill Gates",
                        "    Bill Gates is stalking Tim Berners-Lee",
                        "    Tim Berners-Lee is stalking Alan Kay",
                        "    Alan Kay is stalking Linus Torvalds",
                        "    Linus Torvalds is stalking Alonzo Church",
                        "    Alonzo Church is stalking Don Knuth"
                    )),
                    new LinkedList<>(List.of())
                ),
                Arguments.of(
                    List.of("bill gates"),
                    new LinkedList<>(List.of(
                        "    Don Knuth is stalking Alan Turing",
                        "    Alan Turing is stalking Ada Lovelace",
                        "    Ada Lovelace is stalking Charles Babbage",
                        "    Charles Babbage is stalking John von Neumann",
                        "    John von Neumann is stalking Grace Hopper",
                        "    Grace Hopper is stalking Tim Berners-Lee",
                        "    Tim Berners-Lee is stalking Alan Kay",
                        "    Alan Kay is stalking Linus Torvalds",
                        "    Linus Torvalds is stalking Alonzo Church",
                        "    Alonzo Church is stalking Don Knuth"
                    )),
                    new LinkedList<>(List.of(
                        "    Bill Gates was killed by Grace Hopper"
                    ))
                ),
                Arguments.of(
                        List.of("don knuth"),
                        new LinkedList<>(List.of(
                                "    Alan Turing is stalking Ada Lovelace",
                                "    Ada Lovelace is stalking Charles Babbage",
                                "    Charles Babbage is stalking John von Neumann",
                                "    John von Neumann is stalking Grace Hopper",
                                "    Grace Hopper is stalking Bill Gates",
                                "    Bill Gates is stalking Tim Berners-Lee",
                                "    Tim Berners-Lee is stalking Alan Kay",
                                "    Alan Kay is stalking Linus Torvalds",
                                "    Linus Torvalds is stalking Alonzo Church",
                                "    Alonzo Church is stalking Alan Turing"
                        )),
                        new LinkedList<>(List.of(
                                "    Don Knuth was killed by Alonzo Church"
                        ))
                ),
                Arguments.of(
                    List.of(
                        "john von neumann",
                        "don knuth",
                        "grace hopper",
                        "alan turing",
                        "alonzo church",
                        "alan kay",
                        "ada lovelace",
                        "linus torvalds",
                        "tim berners-lee",
                        "charles babbage"
                    ),
                    new LinkedList<>(List.of()),
                    new LinkedList<>(List.of(
                            "    Charles Babbage was killed by Bill Gates",
                            "    Tim Berners-Lee was killed by Bill Gates",
                            "    Linus Torvalds was killed by Tim Berners-Lee",
                            "    Ada Lovelace was killed by Linus Torvalds",
                            "    Alan Kay was killed by Tim Berners-Lee",
                            "    Alonzo Church was killed by Linus Torvalds",
                            "    Alan Turing was killed by Alonzo Church",
                            "    Grace Hopper was killed by Charles Babbage",
                            "    Don Knuth was killed by Alonzo Church",
                            "    John von Neumann was killed by Charles Babbage"
                    ))
                )
        );
    }

    private static Stream<Arguments> testContains() {
        return Stream.of(
            Arguments.of(List.of(
                "bilL GAtes"
            )),
            Arguments.of(List.of(
                "john von neumann",
                "don knuth",
                "grace hopper",
                "alan turing",
                "alonzo church"
            ))
        );
    }

    private static Stream<Arguments> testWinner() {
        return Stream.of(
                Arguments.of(List.of(
                        "bill gates"
                ), null),
                Arguments.of(List.of(
                        "john von neumann",
                        "don knuth",
                        "grace hopper",
                        "alan turing",
                        "alonzo church",
                        "alan kay",
                        "ada lovelace",
                        "linus torvalds",
                        "tim berners-lee",
                        "charles babbage"
                ), "Bill Gates")
        );
    }

    private static Stream<Arguments> testKillExceptions() {
        return Stream.of(
                Arguments.of(List.of(
                        "john von neumann",
                        "don knuth",
                        "grace hopper",
                        "alan turing",
                        "alonzo church"
                ), "don knuth"),
                Arguments.of(List.of(
                        "john von neumann",
                        "don knuth",
                        "grace hopper",
                        "alan turing",
                        "alonzo church",
                        "alan kay",
                        "ada lovelace",
                        "linus torvalds",
                        "tim berners-lee",
                        "charles babbage"
                ), "don knuth")
        );
    }
}
