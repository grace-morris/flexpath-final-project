package org.example.daos;

import org.example.exceptions.DaoException;
import org.example.models.Monster;
import org.example.models.ResultsPage;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for MonsterDao
 */
@SpringBootTest
@Transactional
class MonsterDaoTest {

    @Autowired
    private MonsterDao monsterDao;

    /**
     * Creates and persists a monster for a test to use
     */
    private Monster newMonster(String name, String monsterType, boolean isPublic, String creatorUsername) {
        Monster monster = new Monster(0, name, monsterType, 10, 1.0, 12,
                "A monster created by MonsterDaoTest.", isPublic, creatorUsername, null, 0);
        return monsterDao.create(monster);
    }

    @Nested
    class CreateAndGetById {

        @Test
        void createdMonster_canBeFetchedById_withAllFieldsIntact() {
            Monster created = newMonster("ZZTest Goblin", "Humanoid", true, "user");

            Monster fetched = monsterDao.getMonsterById(created.getId());

            assertNotNull(fetched);
            assertEquals("ZZTest Goblin", fetched.getName());
            assertEquals("Humanoid", fetched.getMonsterType());
            assertTrue(fetched.isPublic());
            assertEquals("user", fetched.getCreatorUsername());
            assertEquals(0, fetched.getLegendaryActions());
        }

        @Test
        void gettingAMonsterThatDoesNotExist_returnsNull() {
            assertNull(monsterDao.getMonsterById(-999));
        }
    }

    @Nested
    class Update {

        @Test
        void updatingAMonster_persistsTheChanges() {
            Monster created = newMonster("ZZTest Before", "Beast", false, "user");
            created.setName("ZZTest After");
            created.setHealth(999);
            created.setPublic(true);

            Monster updated = monsterDao.update(created);

            assertEquals("ZZTest After", updated.getName());
            assertEquals(999, updated.getHealth());
            assertTrue(updated.isPublic());
        }

        @Test
        void updatingAMonsterThatDoesNotExist_throwsDaoException() {
            Monster fake = new Monster(-999, "ZZTest Ghost", "Undead", 1, 1.0, 1,
                    "", false, "user", null, 0);

            assertThrows(DaoException.class, () -> monsterDao.update(fake));
        }
    }

    @Nested
    class Delete {

        @Test
        void deletingAMonster_removesItFromTheDatabase() {
            Monster created = newMonster("ZZTest Doomed", "Undead", true, "user");

            monsterDao.delete(created.getId());

            assertNull(monsterDao.getMonsterById(created.getId()));
        }
    }

    @Nested
    class Search {

        @Test
        void visibilityAll_returnsPublicMonsters_andTheUsersOwnPrivateOnes_butNotOtherPeoplesPrivateOnes() {
            newMonster("ZZTest All Public Orc", "Humanoid", true, "admin");
            newMonster("ZZTest All My Secret Boss", "Dragon", false, "user");
            newMonster("ZZTest All Someone Elses Secret", "Dragon", false, "admin");

            ResultsPage<Monster> results = monsterDao.search(
                    "user", false, "ZZTest All", "all", "name", null, "asc", 0, 100);

            assertEquals(2, results.getTotalCount());
            assertTrue(results.getItems().stream().anyMatch(m -> m.getName().equals("ZZTest All Public Orc")));
            assertTrue(results.getItems().stream().anyMatch(m -> m.getName().equals("ZZTest All My Secret Boss")));
        }

        @Test
        void visibilityPublic_excludesTheUsersOwnPrivateMonsters() {
            newMonster("ZZTest Pub My Private", "Dragon", false, "user");
            newMonster("ZZTest Pub Someones Public", "Dragon", true, "admin");

            ResultsPage<Monster> results = monsterDao.search(
                    "user", false, "ZZTest Pub", "public", "name", null, "asc", 0, 100);

            assertEquals(1, results.getTotalCount());
            assertEquals("ZZTest Pub Someones Public", results.getItems().get(0).getName());
        }

        @Test
        void visibilityMine_excludesOtherPeoplesPublicMonsters() {
            newMonster("ZZTest Mine Not Mine", "Humanoid", true, "admin");
            newMonster("ZZTest Mine My Own", "Humanoid", true, "user");

            ResultsPage<Monster> results = monsterDao.search(
                    "user", false, "ZZTest Mine", "mine", "name", null, "asc", 0, 100);

            assertEquals(1, results.getTotalCount());
            assertEquals("ZZTest Mine My Own", results.getItems().get(0).getName());
        }

        @Test
        void nonAdminWithoutAVisibilityFilter_seesPublicMonstersAndTheirOwnPrivateOnes_defaultBehavior() {
            newMonster("ZZTest Default My Private", "Beast", false, "user");
            newMonster("ZZTest Default Someones Private", "Beast", false, "admin");
            newMonster("ZZTest Default Someones Public", "Beast", true, "admin");

            ResultsPage<Monster> results = monsterDao.search(
                    "user", false, "ZZTest Default", "garbage", "name", null, "asc", 0, 100);

            assertEquals(2, results.getTotalCount());
            assertFalse(results.getItems().stream().anyMatch(m -> m.getName().equals("ZZTest Default Someones Private")));
        }

        @Test
        void adminWithoutAVisibilityFilter_seesEverything() {
            newMonster("ZZTest Admin Someones Private", "Beast", false, "user");

            ResultsPage<Monster> results = monsterDao.search(
                    "admin", true, "ZZTest Admin", "garbage", "name", null, "asc", 0, 100);

            assertEquals(1, results.getTotalCount());
        }

        @Test
        void nameFilter_isCaseInsensitiveAndMatchesPartialNames() {
            newMonster("ZZTest Ancient Red Dragon", "Dragon", true, "admin");

            ResultsPage<Monster> results = monsterDao.search(
                    "user", false, "zztest red dragon", "all", "name", null, "asc", 0, 100);

            assertEquals(1, results.getTotalCount());
        }

        @Test
        void typeFilter_onlyReturnsMatchingType() {
            newMonster("ZZTest Type Fire Elemental", "Elemental", true, "admin");
            newMonster("ZZTest Type Fire Giant", "Giant", true, "admin");

            ResultsPage<Monster> results = monsterDao.search(
                    "user", false, "ZZTest Type Fire", "all", "name", "Elemental", "asc", 0, 100);

            assertEquals(1, results.getTotalCount());
            assertEquals("ZZTest Type Fire Elemental", results.getItems().get(0).getName());
        }

        @Test
        void pagination_respectsPageAndSize_andSortsByName() {
            newMonster("ZZTest Page Ccc", "Beast", true, "admin");
            newMonster("ZZTest Page Aaa", "Beast", true, "admin");
            newMonster("ZZTest Page Bbb", "Beast", true, "admin");

            ResultsPage<Monster> firstPage = monsterDao.search(
                    "user", false, "ZZTest Page", "all", "name", null, "asc", 0, 2);
            ResultsPage<Monster> secondPage = monsterDao.search(
                    "user", false, "ZZTest Page", "all", "name", null, "asc", 1, 2);

            assertEquals(3, firstPage.getTotalCount());
            assertEquals(2, firstPage.getItems().size());
            assertEquals("ZZTest Page Aaa", firstPage.getItems().get(0).getName());
            assertEquals("ZZTest Page Bbb", firstPage.getItems().get(1).getName());

            assertEquals(1, secondPage.getItems().size());
            assertEquals("ZZTest Page Ccc", secondPage.getItems().get(0).getName());
        }
    }
}
