package org.example.daos;

import org.example.exceptions.DaoException;
import org.example.models.Encounter;
import org.example.models.EncounterMonster;
import org.example.models.Monster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for EncounterMonsterDao
 */
@SpringBootTest
@Transactional
class EncounterMonsterDaoTest {

    @Autowired
    private EncounterMonsterDao encounterMonsterDao;
    @Autowired
    private MonsterDao monsterDao;
    @Autowired
    private EncounterDao encounterDao;

    private int encounterId;
    private int goblinId;

    @BeforeEach
    void setUp() {
        Encounter encounter = encounterDao.create(
                new Encounter(0, "ZZTest Fixture Encounter", "", true, "user", null, 1));
        encounterId = encounter.getId();

        Monster goblin = monsterDao.create(
                new Monster(0, "ZZTest Fixture Goblin", "Humanoid", 7, 0.25, 15,
                        "", true, "user", null, 3));
        goblinId = goblin.getId();
    }

    @Nested
    class AddMonsterToEncounter {

        @Test
        void addingAMonster_copiesItsMaxHealthAsStartingCurrentHealth() {
            EncounterMonster added = encounterMonsterDao.addMonsterToEncounter(encounterId, goblinId);

            assertEquals("ZZTest Fixture Goblin", added.getMonsterName());
            assertEquals(7, added.getMaxHealth());
            assertEquals(7, added.getCurrentHealth());
            assertEquals(3, added.getMaxLegendaryActions());
            assertEquals(0, added.getLegendaryActionsUsed());
            assertFalse(added.isUsedReaction());
        }

        @Test
        void addingAMonsterTypeThatDoesNotExist_throwsDaoException() {
            assertThrows(DaoException.class, () -> encounterMonsterDao.addMonsterToEncounter(encounterId, -999));
        }
    }

    @Nested
    class GetByEncounterIdAndGetById {

        @Test
        void getByEncounterId_returnsEveryMonsterAddedToThatEncounter() {
            encounterMonsterDao.addMonsterToEncounter(encounterId, goblinId);
            encounterMonsterDao.addMonsterToEncounter(encounterId, goblinId);

            List<EncounterMonster> list = encounterMonsterDao.getByEncounterId(encounterId);

            assertEquals(2, list.size());
        }

        @Test
        void getById_returnsNull_whenTheRowDoesNotExist() {
            assertNull(encounterMonsterDao.getById(-999));
        }
    }

    @Nested
    class UpdateHealth {

        @Test
        void updateHealth_clampsToZero_whenDamageWouldGoNegative() {
            EncounterMonster added = encounterMonsterDao.addMonsterToEncounter(encounterId, goblinId);

            EncounterMonster result = encounterMonsterDao.updateHealth(added.getId(), -50);

            assertEquals(0, result.getCurrentHealth());
        }

        @Test
        void updateHealth_clampsToMaxHealth_whenHealingWouldExceedIt() {
            EncounterMonster added = encounterMonsterDao.addMonsterToEncounter(encounterId, goblinId);

            EncounterMonster result = encounterMonsterDao.updateHealth(added.getId(), 9999);

            assertEquals(added.getMaxHealth(), result.getCurrentHealth());
        }

        @Test
        void updateHealth_forARowThatDoesNotExist_throwsDaoException() {
            assertThrows(DaoException.class, () -> encounterMonsterDao.updateHealth(-999, 5));
        }
    }

    @Nested
    class UpdateInitiative {

        @Test
        void updateInitiative_persistsTheNewValue() {
            EncounterMonster added = encounterMonsterDao.addMonsterToEncounter(encounterId, goblinId);

            EncounterMonster result = encounterMonsterDao.updateInitiative(added.getId(), 17);

            assertEquals(17, result.getInitiative());
        }

        @Test
        void updateInitiative_forARowThatDoesNotExist_throwsDaoException() {
            assertThrows(DaoException.class, () -> encounterMonsterDao.updateInitiative(-999, 10));
        }
    }

    @Nested
    class SetUsedReaction {

        @Test
        void setUsedReaction_persistsTheFlag() {
            EncounterMonster added = encounterMonsterDao.addMonsterToEncounter(encounterId, goblinId);

            EncounterMonster result = encounterMonsterDao.setUsedReaction(added.getId(), true);

            assertTrue(result.isUsedReaction());
        }
    }

    @Nested
    class UseLegendaryAction {

        @Test
        void useLegendaryAction_incrementsTheCount() {
            EncounterMonster added = encounterMonsterDao.addMonsterToEncounter(encounterId, goblinId);

            EncounterMonster result = encounterMonsterDao.useLegendaryAction(added.getId());

            assertEquals(1, result.getLegendaryActionsUsed());
        }

        @Test
        void useLegendaryAction_doesNotExceedTheMonstersMax() {
            EncounterMonster added = encounterMonsterDao.addMonsterToEncounter(encounterId, goblinId);
            // this goblin fixture has 3 max legendary actions
            encounterMonsterDao.useLegendaryAction(added.getId());
            encounterMonsterDao.useLegendaryAction(added.getId());
            encounterMonsterDao.useLegendaryAction(added.getId());

            EncounterMonster result = encounterMonsterDao.useLegendaryAction(added.getId());

            assertEquals(3, result.getLegendaryActionsUsed());
        }
    }

    @Nested
    class Remove {

        @Test
        void remove_deletesTheRow() {
            EncounterMonster added = encounterMonsterDao.addMonsterToEncounter(encounterId, goblinId);

            encounterMonsterDao.remove(added.getId());

            assertNull(encounterMonsterDao.getById(added.getId()));
        }
    }

    @Nested
    class ResetRoundState {

        @Test
        void resetRoundState_clearsUsedReactionAndLegendaryActionsUsed_forEveryMonsterInThatEncounter() {
            EncounterMonster monster1 = encounterMonsterDao.addMonsterToEncounter(encounterId, goblinId);
            EncounterMonster monster2 = encounterMonsterDao.addMonsterToEncounter(encounterId, goblinId);
            encounterMonsterDao.setUsedReaction(monster1.getId(), true);
            encounterMonsterDao.useLegendaryAction(monster1.getId());
            encounterMonsterDao.setUsedReaction(monster2.getId(), true);

            encounterMonsterDao.resetRoundState(encounterId);

            EncounterMonster refreshed1 = encounterMonsterDao.getById(monster1.getId());
            EncounterMonster refreshed2 = encounterMonsterDao.getById(monster2.getId());
            assertFalse(refreshed1.isUsedReaction());
            assertEquals(0, refreshed1.getLegendaryActionsUsed());
            assertFalse(refreshed2.isUsedReaction());
        }

        @Test
        void resetRoundState_doesNotAffectMonstersInADifferentEncounter() {
            Encounter otherEncounter = encounterDao.create(
                    new Encounter(0, "ZZTest Other Encounter", "", true, "user", null, 1));
            EncounterMonster inThisEncounter = encounterMonsterDao.addMonsterToEncounter(encounterId, goblinId);
            EncounterMonster inOtherEncounter = encounterMonsterDao.addMonsterToEncounter(otherEncounter.getId(), goblinId);
            encounterMonsterDao.setUsedReaction(inThisEncounter.getId(), true);
            encounterMonsterDao.setUsedReaction(inOtherEncounter.getId(), true);

            encounterMonsterDao.resetRoundState(encounterId);

            assertFalse(encounterMonsterDao.getById(inThisEncounter.getId()).isUsedReaction());
            assertTrue(encounterMonsterDao.getById(inOtherEncounter.getId()).isUsedReaction());
        }
    }
}
