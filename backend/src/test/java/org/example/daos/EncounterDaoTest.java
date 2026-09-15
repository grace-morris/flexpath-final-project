package org.example.daos;

import org.example.exceptions.DaoException;
import org.example.models.Encounter;
import org.example.models.ResultsPage;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for EncounterDao
 */
@SpringBootTest
@Transactional
class EncounterDaoTest {

    @Autowired
    private EncounterDao encounterDao;

    private Encounter newEncounter(String name, boolean isPublic, String creatorUsername) {
        Encounter encounter = new Encounter(0, name, "An encounter created by EncounterDaoTest.",
                isPublic, creatorUsername, null, 1);
        return encounterDao.create(encounter);
    }

    @Nested
    class CreateAndGetById {

        @Test
        void createdEncounter_canBeFetchedById_andStartsOnRoundOne() {
            Encounter created = newEncounter("ZZTest Goblin Ambush", true, "user");

            Encounter fetched = encounterDao.getEncounterById(created.getId());

            assertNotNull(fetched);
            assertEquals("ZZTest Goblin Ambush", fetched.getName());
            assertTrue(fetched.isPublic());
            assertEquals("user", fetched.getCreatorUsername());
            assertEquals(1, fetched.getCurrentRound());
        }

        @Test
        void gettingAnEncounterThatDoesNotExist_returnsNull() {
            assertNull(encounterDao.getEncounterById(-999));
        }
    }

    @Nested
    class Update {

        @Test
        void updatingAnEncounter_persistsTheChanges() {
            Encounter created = newEncounter("ZZTest Before", false, "user");
            created.setName("ZZTest After");
            created.setPublic(true);

            Encounter updated = encounterDao.update(created);

            assertEquals("ZZTest After", updated.getName());
            assertTrue(updated.isPublic());
        }

        @Test
        void updatingAnEncounterThatDoesNotExist_throwsDaoException() {
            Encounter fake = new Encounter(-999, "ZZTest Ghost", "", false, "user", null, 1);

            assertThrows(DaoException.class, () -> encounterDao.update(fake));
        }
    }

    @Nested
    class Delete {

        @Test
        void deletingAnEncounter_removesItFromTheDatabase() {
            Encounter created = newEncounter("ZZTest Doomed", true, "user");

            encounterDao.delete(created.getId());

            assertNull(encounterDao.getEncounterById(created.getId()));
        }
    }

    @Nested
    class IncrementRound {

        @Test
        void incrementingTheRound_addsOneToCurrentRound() {
            Encounter created = newEncounter("ZZTest Round Tracker", true, "user");

            Encounter afterFirst = encounterDao.incrementRound(created.getId());
            assertEquals(2, afterFirst.getCurrentRound());

            Encounter afterSecond = encounterDao.incrementRound(created.getId());
            assertEquals(3, afterSecond.getCurrentRound());
        }

        @Test
        void incrementingTheRoundForAnEncounterThatDoesNotExist_throwsDaoException() {
            assertThrows(DaoException.class, () -> encounterDao.incrementRound(-999));
        }
    }

    @Nested
    class Search {

        @Test
        void visibilityAll_returnsPublicEncounters_andTheUsersOwnPrivateOnes_butNotOtherPeoplesPrivateOnes() {
            newEncounter("ZZTest All Public Battle", true, "admin");
            newEncounter("ZZTest All My Secret Battle", false, "user");
            newEncounter("ZZTest All Someone Elses Secret", false, "admin");

            ResultsPage<Encounter> results = encounterDao.search(
                    "user", false, "ZZTest All", "all", "name", "asc", 0, 100);

            assertEquals(2, results.getTotalCount());
            assertTrue(results.getItems().stream().anyMatch(e -> e.getName().equals("ZZTest All Public Battle")));
            assertTrue(results.getItems().stream().anyMatch(e -> e.getName().equals("ZZTest All My Secret Battle")));
        }

        @Test
        void visibilityPublic_excludesTheUsersOwnPrivateEncounters() {
            newEncounter("ZZTest Pub My Private", false, "user");
            newEncounter("ZZTest Pub Someones Public", true, "admin");

            ResultsPage<Encounter> results = encounterDao.search(
                    "user", false, "ZZTest Pub", "public", "name", "asc", 0, 100);

            assertEquals(1, results.getTotalCount());
            assertEquals("ZZTest Pub Someones Public", results.getItems().get(0).getName());
        }

        @Test
        void visibilityMine_excludesOtherPeoplesPublicEncounters() {
            newEncounter("ZZTest Mine Not Mine", true, "admin");
            newEncounter("ZZTest Mine My Own", true, "user");

            ResultsPage<Encounter> results = encounterDao.search(
                    "user", false, "ZZTest Mine", "mine", "name", "asc", 0, 100);

            assertEquals(1, results.getTotalCount());
            assertEquals("ZZTest Mine My Own", results.getItems().get(0).getName());
        }

        @Test
        void pagination_respectsPageAndSize_andSortsByName() {
            newEncounter("ZZTest Page Ccc", true, "admin");
            newEncounter("ZZTest Page Aaa", true, "admin");
            newEncounter("ZZTest Page Bbb", true, "admin");

            ResultsPage<Encounter> firstPage = encounterDao.search(
                    "user", false, "ZZTest Page", "all", "name", "asc", 0, 2);
            ResultsPage<Encounter> secondPage = encounterDao.search(
                    "user", false, "ZZTest Page", "all", "name", "asc", 1, 2);

            assertEquals(3, firstPage.getTotalCount());
            assertEquals(2, firstPage.getItems().size());
            assertEquals("ZZTest Page Aaa", firstPage.getItems().get(0).getName());
            assertEquals("ZZTest Page Bbb", firstPage.getItems().get(1).getName());
            assertEquals(1, secondPage.getItems().size());
            assertEquals("ZZTest Page Ccc", secondPage.getItems().get(0).getName());
        }
    }
}
