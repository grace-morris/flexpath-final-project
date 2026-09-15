package org.example.daos;

import org.example.exceptions.DaoException;
import org.example.models.PlayerCharacter;
import org.example.models.ResultsPage;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for PlayerCharacterDao
 */
@SpringBootTest
@Transactional
class PlayerCharacterDaoTest {

    @Autowired
    private PlayerCharacterDao playerCharacterDao;

    private PlayerCharacter newCharacter(String name, String characterClass, boolean isPublic, String creatorUsername) {
        PlayerCharacter character = new PlayerCharacter(0, name, characterClass, 20, 5, 16,
                "A character created by PlayerCharacterDaoTest.", isPublic, creatorUsername, null);
        return playerCharacterDao.create(character);
    }

    @Nested
    class CreateAndGetById {

        @Test
        void createdCharacter_canBeFetchedById_withAllFieldsIntact() {
            PlayerCharacter created = newCharacter("ZZTest Aragorn", "Ranger", true, "user");

            PlayerCharacter fetched = playerCharacterDao.getCharacterById(created.getId());

            assertNotNull(fetched);
            assertEquals("ZZTest Aragorn", fetched.getName());
            assertEquals("Ranger", fetched.getCharacterClass());
            assertTrue(fetched.isPublic());
            assertEquals("user", fetched.getCreatorUsername());
        }

        @Test
        void gettingACharacterThatDoesNotExist_returnsNull() {
            assertNull(playerCharacterDao.getCharacterById(-999));
        }
    }

    @Nested
    class Update {

        @Test
        void updatingACharacter_persistsTheChanges() {
            PlayerCharacter created = newCharacter("ZZTest Before", "Fighter", false, "user");
            created.setName("ZZTest After");
            created.setLevel(10);
            created.setPublic(true);

            PlayerCharacter updated = playerCharacterDao.update(created);

            assertEquals("ZZTest After", updated.getName());
            assertEquals(10, updated.getLevel());
            assertTrue(updated.isPublic());
        }

        @Test
        void updatingACharacterThatDoesNotExist_throwsDaoException() {
            PlayerCharacter fake = new PlayerCharacter(-999, "ZZTest Ghost", "Wizard", 1, 1, 1,
                    "", false, "user", null);

            assertThrows(DaoException.class, () -> playerCharacterDao.update(fake));
        }
    }

    @Nested
    class Delete {

        @Test
        void deletingACharacter_removesItFromTheDatabase() {
            PlayerCharacter created = newCharacter("ZZTest Doomed", "Cleric", true, "user");

            playerCharacterDao.delete(created.getId());

            assertNull(playerCharacterDao.getCharacterById(created.getId()));
        }
    }

    @Nested
    class Search {

        @Test
        void visibilityAll_returnsPublicCharacters_andTheUsersOwnPrivateOnes_butNotOtherPeoplesPrivateOnes() {
            newCharacter("ZZTest All Public Bard", "Bard", true, "admin");
            newCharacter("ZZTest All My Secret Rogue", "Rogue", false, "user");
            newCharacter("ZZTest All Someone Elses Secret", "Rogue", false, "admin");

            ResultsPage<PlayerCharacter> results = playerCharacterDao.search(
                    "user", false, "ZZTest All", "all", "name", null, "asc", 0, 100);

            assertEquals(2, results.getTotalCount());
            assertTrue(results.getItems().stream().anyMatch(c -> c.getName().equals("ZZTest All Public Bard")));
            assertTrue(results.getItems().stream().anyMatch(c -> c.getName().equals("ZZTest All My Secret Rogue")));
        }

        @Test
        void visibilityPublic_excludesTheUsersOwnPrivateCharacters() {
            newCharacter("ZZTest Pub My Private", "Rogue", false, "user");
            newCharacter("ZZTest Pub Someones Public", "Rogue", true, "admin");

            ResultsPage<PlayerCharacter> results = playerCharacterDao.search(
                    "user", false, "ZZTest Pub", "public", "name", null, "asc", 0, 100);

            assertEquals(1, results.getTotalCount());
            assertEquals("ZZTest Pub Someones Public", results.getItems().get(0).getName());
        }

        @Test
        void visibilityMine_excludesOtherPeoplesPublicCharacters() {
            newCharacter("ZZTest Mine Not Mine", "Wizard", true, "admin");
            newCharacter("ZZTest Mine My Own", "Wizard", true, "user");

            ResultsPage<PlayerCharacter> results = playerCharacterDao.search(
                    "user", false, "ZZTest Mine", "mine", "name", null, "asc", 0, 100);

            assertEquals(1, results.getTotalCount());
            assertEquals("ZZTest Mine My Own", results.getItems().get(0).getName());
        }

        @Test
        void characterClassFilter_onlyReturnsMatchingClass() {
            newCharacter("ZZTest Class Gandalf", "Wizard", true, "admin");
            newCharacter("ZZTest Class Boromir", "Fighter", true, "admin");

            ResultsPage<PlayerCharacter> results = playerCharacterDao.search(
                    "user", false, "ZZTest Class", "all", "name", "Wizard", "asc", 0, 100);

            assertEquals(1, results.getTotalCount());
            assertEquals("ZZTest Class Gandalf", results.getItems().get(0).getName());
        }

        @Test
        void pagination_respectsPageAndSize_andSortsByName() {
            newCharacter("ZZTest Page Ccc", "Fighter", true, "admin");
            newCharacter("ZZTest Page Aaa", "Fighter", true, "admin");
            newCharacter("ZZTest Page Bbb", "Fighter", true, "admin");

            ResultsPage<PlayerCharacter> firstPage = playerCharacterDao.search(
                    "user", false, "ZZTest Page", "all", "name", null, "asc", 0, 2);
            ResultsPage<PlayerCharacter> secondPage = playerCharacterDao.search(
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
