package org.example.daos;

import org.example.exceptions.DaoException;
import org.example.models.Encounter;
import org.example.models.EncounterPlayerCharacter;
import org.example.models.PlayerCharacter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for EncounterPlayerCharacterDao - same shape as EncounterMonsterDaoTest
 */
@SpringBootTest
@Transactional
class EncounterPlayerCharacterDaoTest {

    @Autowired
    private EncounterPlayerCharacterDao encounterPlayerCharacterDao;
    @Autowired
    private PlayerCharacterDao playerCharacterDao;
    @Autowired
    private EncounterDao encounterDao;

    private int encounterId;
    private int aragornId;

    @BeforeEach
    void setUp() {
        Encounter encounter = encounterDao.create(
                new Encounter(0, "ZZTest Fixture Encounter", "", true, "user", null, 1));
        encounterId = encounter.getId();

        PlayerCharacter aragorn = playerCharacterDao.create(
                new PlayerCharacter(0, "ZZTest Fixture Aragorn", "Ranger", 20, 5, 16,
                        "", true, "user", null));
        aragornId = aragorn.getId();
    }

    @Nested
    class AddCharacterToEncounter {

        @Test
        void addingACharacter_copiesItsMaxHealthAsStartingCurrentHealth() {
            EncounterPlayerCharacter added = encounterPlayerCharacterDao.addCharacterToEncounter(encounterId, aragornId);

            assertEquals("ZZTest Fixture Aragorn", added.getPlayerCharacterName());
            assertEquals(20, added.getMaxHealth());
            assertEquals(20, added.getCurrentHealth());
            assertFalse(added.isUsedReaction());
        }

        @Test
        void addingACharacterThatDoesNotExist_throwsDaoException() {
            assertThrows(DaoException.class,
                    () -> encounterPlayerCharacterDao.addCharacterToEncounter(encounterId, -999));
        }
    }

    @Nested
    class GetByEncounterIdAndGetById {

        @Test
        void getByEncounterId_returnsEveryCharacterAddedToThatEncounter() {
            encounterPlayerCharacterDao.addCharacterToEncounter(encounterId, aragornId);
            encounterPlayerCharacterDao.addCharacterToEncounter(encounterId, aragornId);

            List<EncounterPlayerCharacter> list = encounterPlayerCharacterDao.getByEncounterId(encounterId);

            assertEquals(2, list.size());
        }

        @Test
        void getById_returnsNull_whenTheRowDoesNotExist() {
            assertNull(encounterPlayerCharacterDao.getById(-999));
        }
    }

    @Nested
    class UpdateHealth {

        @Test
        void updateHealth_clampsToZero_whenDamageWouldGoNegative() {
            EncounterPlayerCharacter added = encounterPlayerCharacterDao.addCharacterToEncounter(encounterId, aragornId);

            EncounterPlayerCharacter result = encounterPlayerCharacterDao.updateHealth(added.getId(), -50);

            assertEquals(0, result.getCurrentHealth());
        }

        @Test
        void updateHealth_clampsToMaxHealth_whenHealingWouldExceedIt() {
            EncounterPlayerCharacter added = encounterPlayerCharacterDao.addCharacterToEncounter(encounterId, aragornId);

            EncounterPlayerCharacter result = encounterPlayerCharacterDao.updateHealth(added.getId(), 9999);

            assertEquals(added.getMaxHealth(), result.getCurrentHealth());
        }

        @Test
        void updateHealth_forARowThatDoesNotExist_throwsDaoException() {
            assertThrows(DaoException.class, () -> encounterPlayerCharacterDao.updateHealth(-999, 5));
        }
    }

    @Nested
    class UpdateInitiative {

        @Test
        void updateInitiative_persistsTheNewValue() {
            EncounterPlayerCharacter added = encounterPlayerCharacterDao.addCharacterToEncounter(encounterId, aragornId);

            EncounterPlayerCharacter result = encounterPlayerCharacterDao.updateInitiative(added.getId(), 17);

            assertEquals(17, result.getInitiative());
        }

        @Test
        void updateInitiative_forARowThatDoesNotExist_throwsDaoException() {
            assertThrows(DaoException.class, () -> encounterPlayerCharacterDao.updateInitiative(-999, 10));
        }
    }

    @Nested
    class SetUsedReaction {

        @Test
        void setUsedReaction_persistsTheFlag() {
            EncounterPlayerCharacter added = encounterPlayerCharacterDao.addCharacterToEncounter(encounterId, aragornId);

            EncounterPlayerCharacter result = encounterPlayerCharacterDao.setUsedReaction(added.getId(), true);

            assertTrue(result.isUsedReaction());
        }
    }

    @Nested
    class Remove {

        @Test
        void remove_deletesTheRow() {
            EncounterPlayerCharacter added = encounterPlayerCharacterDao.addCharacterToEncounter(encounterId, aragornId);

            encounterPlayerCharacterDao.remove(added.getId());

            assertNull(encounterPlayerCharacterDao.getById(added.getId()));
        }
    }

    @Nested
    class ResetRoundState {

        @Test
        void resetRoundState_clearsUsedReaction_forEveryCharacterInThatEncounter() {
            EncounterPlayerCharacter character1 = encounterPlayerCharacterDao.addCharacterToEncounter(encounterId, aragornId);
            EncounterPlayerCharacter character2 = encounterPlayerCharacterDao.addCharacterToEncounter(encounterId, aragornId);
            encounterPlayerCharacterDao.setUsedReaction(character1.getId(), true);
            encounterPlayerCharacterDao.setUsedReaction(character2.getId(), true);

            encounterPlayerCharacterDao.resetRoundState(encounterId);

            assertFalse(encounterPlayerCharacterDao.getById(character1.getId()).isUsedReaction());
            assertFalse(encounterPlayerCharacterDao.getById(character2.getId()).isUsedReaction());
        }

        @Test
        void resetRoundState_doesNotAffectCharactersInADifferentEncounter() {
            Encounter otherEncounter = encounterDao.create(
                    new Encounter(0, "ZZTest Other Encounter", "", true, "user", null, 1));
            EncounterPlayerCharacter inThisEncounter = encounterPlayerCharacterDao.addCharacterToEncounter(encounterId, aragornId);
            EncounterPlayerCharacter inOtherEncounter =
                    encounterPlayerCharacterDao.addCharacterToEncounter(otherEncounter.getId(), aragornId);
            encounterPlayerCharacterDao.setUsedReaction(inThisEncounter.getId(), true);
            encounterPlayerCharacterDao.setUsedReaction(inOtherEncounter.getId(), true);

            encounterPlayerCharacterDao.resetRoundState(encounterId);

            assertFalse(encounterPlayerCharacterDao.getById(inThisEncounter.getId()).isUsedReaction());
            assertTrue(encounterPlayerCharacterDao.getById(inOtherEncounter.getId()).isUsedReaction());
        }
    }
}
