package mn.astvision.ard.playground;

import mn.astvision.ard.data.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreamTest {

    // A small, fixed dataset we can reason about by hand.
    private List<User> users;

    @BeforeEach
    void setUp() {
        // @BeforeEach runs before EVERY @Test, giving each test a fresh, independent dataset.
        users = List.of(
                user("admin", "System", "Admin", true, List.of("ADMIN", "USER")),
                user("bbold", "Bold", "Batbayar", true, List.of("USER")),
                user("tsetseg", "Tsetseg", "Munkh", false, List.of("USER")),
                user("dorj", "Dorj", "Ganbat", true, List.of("USER")),
                user("oyuna", "Oyun", "Erdene", false, List.of("USER"))
        );
    }

    // =====================================================================
    // 1. BASIC UNIT-TEST OPERATIONS (assertions)
    // =====================================================================
    @Nested
    @DisplayName("1. assertion basics")
    class AssertionBasics {

        @Test
        @DisplayName("equality, truthiness and null checks")
        void coreAssertions() {
            int sum = 2 + 3;

            // JUnit style: assertEquals(expected, actual)
            assertEquals(5, sum);
            assertTrue(sum > 0);
            assertFalse(sum > 100);

            String nothing = null;
            // AssertJ style: fluent and very readable — assertThat(actual).isXxx(...)
            assertThat(nothing).isNull();
            assertThat("ardbank").startsWith("ard").hasSize(7);
        }

        @Test
        @DisplayName("assertAll groups assertions so all are evaluated, not just the first failure")
        void groupedAssertions() {
            User admin = users.get(0);
            assertAll("admin user",
                    () -> assertEquals("admin", admin.getUsername()),
                    () -> assertTrue(admin.isActive()),
                    () -> assertEquals(2, admin.getRoles().size())
            );
        }

        @Test
        @DisplayName("asserting that code throws an exception")
        void exceptionAssertions() {
            // JUnit: assertThrows returns the thrown exception so you can inspect it.
            ArithmeticException ex = assertThrows(ArithmeticException.class, () -> divide(1, 0));
            assertEquals("/ by zero", ex.getMessage());

            // AssertJ equivalent, with message matching.
            assertThatThrownBy(() -> divide(1, 0))
                    .isInstanceOf(ArithmeticException.class)
                    .hasMessageContaining("zero");

            // And the happy path still works.
            assertEquals(5, divide(10, 2));
        }
    }

    // =====================================================================
    // 2. JAVA STREAM API
    // =====================================================================
    @Nested
    @DisplayName("2. stream operations")
    class Streams {

        @Test
        @DisplayName("filter + map + collect: usernames of the active users")
        void filterMapCollect() {
            List<String> activeUsernames = users.stream()
                    .filter(User::isActive)            // keep only active users
                    .map(User::getUsername)            // transform User -> String
                    .collect(Collectors.toList());     // gather into a List

            assertThat(activeUsernames).containsExactly("admin", "bbold", "dorj");
        }

        @Test
        @DisplayName("count, anyMatch, allMatch, noneMatch")
        void matchingAndCounting() {
            long adminCount = users.stream()
                    .filter(u -> u.getRoles().contains("ADMIN"))
                    .count();

            assertEquals(1, adminCount);
            assertTrue(users.stream().anyMatch(u -> !u.isActive()));        // at least one inactive
            assertTrue(users.stream().allMatch(u -> u.getRoles().contains("USER"))); // everyone is a USER
            assertFalse(users.stream().noneMatch(User::isActive));          // it's false that nobody is active
        }

        @Test
        @DisplayName("sorted + limit: first two usernames alphabetically")
        void sortedAndLimited() {
            List<String> firstTwo = users.stream()
                    .map(User::getUsername)
                    .sorted()                          // natural (alphabetical) order
                    .limit(2)                          // take only the first two
                    .toList();                         // Stream#toList() (Java 16+)

            assertThat(firstTwo).containsExactly("admin", "bbold");
        }

        @Test
        @DisplayName("findFirst returns an Optional")
        void findFirstOptional() {
            Optional<User> firstInactive = users.stream()
                    .filter(u -> !u.isActive())
                    .findFirst();

            assertThat(firstInactive).isPresent();
            assertEquals("tsetseg", firstInactive.get().getUsername());

            Optional<User> ghost = users.stream()
                    .filter(u -> u.getUsername().equals("does-not-exist"))
                    .findFirst();
            assertThat(ghost).isEmpty();
        }

        @Test
        @DisplayName("reduce and numeric streams: summing and averaging")
        void reduceAndNumericStreams() {
            // reduce: combine a stream into a single value (1+2+3+4+5 = 15)
            int sum = Stream.of(1, 2, 3, 4, 5).reduce(0, Integer::sum);
            assertEquals(15, sum);

            // IntStream specialises in numbers and offers sum()/average()/max()...
            int total = IntStream.rangeClosed(1, 5).sum();
            assertEquals(15, total);

            double avgRoleCount = users.stream()
                    .mapToInt(u -> u.getRoles().size())   // 2,1,1,1,1
                    .average()
                    .orElse(0);
            assertEquals(1.2, avgRoleCount);
        }

        @Test
        @DisplayName("collect to a Map and partition/group")
        void collectorsMapAndGrouping() {
            // toMap: username -> active flag
            Map<String, Boolean> activeByUsername = users.stream()
                    .collect(Collectors.toMap(User::getUsername, User::isActive));
            assertThat(activeByUsername).containsEntry("admin", true).containsEntry("tsetseg", false);

            // partitioningBy: split into true/false buckets
            Map<Boolean, List<User>> byActive = users.stream()
                    .collect(Collectors.partitioningBy(User::isActive));
            assertThat(byActive.get(true)).hasSize(3);
            assertThat(byActive.get(false)).hasSize(2);

            // groupingBy + counting: how many users hold each role
            Map<String, Long> usersPerRole = users.stream()
                    .flatMap(u -> u.getRoles().stream())   // flatten each user's roles into one stream
                    .collect(Collectors.groupingBy(role -> role, Collectors.counting()));
            assertEquals(1L, usersPerRole.get("ADMIN"));
            assertEquals(5L, usersPerRole.get("USER"));
        }

        @Test
        @DisplayName("joining: build a comma-separated string")
        void joining() {
            String csv = users.stream()
                    .map(User::getUsername)
                    .collect(Collectors.joining(", ", "[", "]"));

            assertEquals("[admin, bbold, tsetseg, dorj, oyuna]", csv);
        }

        @Test
        @DisplayName("distinct removes duplicates")
        void distinct() {
            List<Integer> unique = Stream.of(1, 1, 2, 2, 3, 3, 3)
                    .distinct()
                    .toList();

            assertThat(unique).containsExactly(1, 2, 3);
        }
    }

    // ---------------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------------
    private static User user(String username, String firstName, String lastName,
                             boolean active, List<String> roles) {
        return User.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .active(active)
                .roles(roles)
                .build();
    }

    private static int divide(int a, int b) {
        return a / b;
    }
}
