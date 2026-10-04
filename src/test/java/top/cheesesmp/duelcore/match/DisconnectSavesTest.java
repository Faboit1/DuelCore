package top.cheesesmp.duelcore.match;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/** An idle quit only counts as a dropped connection mid-fight and while not behind. */
class DisconnectSavesTest {

    @Test
    void onlyWhileFighting() {
        assertTrue(DisconnectSaves.idleRuleApplies(Match.State.FIGHTING, 0, 0));
        for (Match.State s : Match.State.values()) {
            if (s != Match.State.FIGHTING) assertFalse(DisconnectSaves.idleRuleApplies(s, 1, 0), s.name());
        }
    }

    @Test
    void notWhenBehind() {
        assertFalse(DisconnectSaves.idleRuleApplies(Match.State.FIGHTING, 1, 2));
        assertTrue(DisconnectSaves.idleRuleApplies(Match.State.FIGHTING, 2, 2));
        assertTrue(DisconnectSaves.idleRuleApplies(Match.State.FIGHTING, 3, 1));
    }

    @Test
    void noMatch() {
        assertFalse(DisconnectSaves.idleRuleApplies(null, UUID.randomUUID()));
    }
}
