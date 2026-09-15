package org.example.services;

import org.example.daos.PlayerCharacterDao;
import org.example.exceptions.DaoException;
import org.example.models.PlayerCharacter;
import org.example.models.ResultsPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PlayerCharacterService}.
 *
 * canModify() is a single shared private method, so its branches only need to be proven once 
 * So AddMonster below carries the full permission-check suite, and every other method just gets one "happy path"
 * test proving it delegates to the right dao call with the right arguments
 */
@ExtendWith(MockitoExtension.class)
class PlayerCharacterServiceTest {

    @Mock
    private PlayerCharacterDao playerCharacterDao;

    private PlayerCharacterService playerCharacterService;

    @BeforeEach
    void setUp() {
        playerCharacterService = new PlayerCharacterService(playerCharacterDao);
    }

    private PlayerCharacter character(int id, boolean isPublic, String creatorUsername) {
        return new PlayerCharacter(id, "Aragorn", "Ranger", 20, 5, 16,
                "A weathered ranger of the North.", isPublic, creatorUsername, null);
    }

    // ------------------------------------------------------------------
    // search
    // ------------------------------------------------------------------

    @Nested
    class Search {

        @Test
        void delegatesToDaoWithAllParameters_andReturnsWhateverTheDaoReturns() {
            ResultsPage<PlayerCharacter> expected = new ResultsPage<>(List.of(character(1, true, "grace")), 1);
            when(playerCharacterDao.search("grace", false, "arag", "public", "name", "Ranger", "asc", 0, 10))
                    .thenReturn(expected);

            ResultsPage<PlayerCharacter> actual = playerCharacterService.search(
                    "grace", false, "arag", "public", "name", "Ranger", "asc", 0, 10);

            assertSame(expected, actual);
            verify(playerCharacterDao).search("grace", false, "arag", "public", "name", "Ranger", "asc", 0, 10);
        }
    }

    // ------------------------------------------------------------------
    // getIsVisible
    // ------------------------------------------------------------------

    @Nested
    class GetIsVisible {

        @Test
        void characterDoesNotExist_returnsNull() {
            when(playerCharacterDao.getCharacterById(99)).thenReturn(null);

            assertNull(playerCharacterService.getIsVisible(99, "grace", false));
        }

        @Test
        void publicCharacter_isVisibleToAnyLoggedInUser() {
            PlayerCharacter publicCharacter = character(1, true, "someone-else");
            when(playerCharacterDao.getCharacterById(1)).thenReturn(publicCharacter);

            assertSame(publicCharacter, playerCharacterService.getIsVisible(1, "grace", false));
        }

        @Test
        void privateCharacter_isVisibleToItsOwner() {
            PlayerCharacter privateCharacter = character(1, false, "grace");
            when(playerCharacterDao.getCharacterById(1)).thenReturn(privateCharacter);

            assertSame(privateCharacter, playerCharacterService.getIsVisible(1, "grace", false));
        }

        @Test
        void privateCharacter_isVisibleToAnAdmin_evenIfNotTheOwner() {
            PlayerCharacter privateCharacter = character(1, false, "someone-else");
            when(playerCharacterDao.getCharacterById(1)).thenReturn(privateCharacter);

            assertSame(privateCharacter, playerCharacterService.getIsVisible(1, "grace", true));
        }

        @Test
        void privateCharacter_ownedBySomeoneElse_throwsAccessDeniedForNonAdmin() {
            when(playerCharacterDao.getCharacterById(1)).thenReturn(character(1, false, "someone-else"));

            assertThrows(AccessDeniedException.class,
                    () -> playerCharacterService.getIsVisible(1, "grace", false));
        }
    }

    // ------------------------------------------------------------------
    // create
    // ------------------------------------------------------------------

    @Nested
    class Create {

        @Test
        void alwaysSetsCreatorUsernameToTheCurrentUser_regardlessOfWhatWasPassedIn() {
            PlayerCharacter incoming = character(0, false, "someone-else-entirely");
            when(playerCharacterDao.create(any(PlayerCharacter.class))).thenAnswer(invocation -> invocation.getArgument(0));

            PlayerCharacter created = playerCharacterService.create(incoming, "grace");

            ArgumentCaptor<PlayerCharacter> captor = ArgumentCaptor.forClass(PlayerCharacter.class);
            verify(playerCharacterDao).create(captor.capture());
            assertEquals("grace", captor.getValue().getCreatorUsername());
            assertEquals("grace", created.getCreatorUsername());
        }
    }

    // ------------------------------------------------------------------
    // update
    // ------------------------------------------------------------------

    @Nested
    class Update {

        @Test
        void owner_canUpdateTheirOwnCharacter() {
            PlayerCharacter existing = character(5, false, "grace");
            PlayerCharacter edits = character(0, true, "ignored-should-not-be-used");
            when(playerCharacterDao.getCharacterById(5)).thenReturn(existing);
            when(playerCharacterDao.update(any(PlayerCharacter.class))).thenAnswer(invocation -> invocation.getArgument(0));

            playerCharacterService.update(5, edits, "grace", false);

            verify(playerCharacterDao).update(edits);
        }

        @Test
        void update_preservesTheExistingIdAndCreatorUsername_ignoringWhateverWasSubmitted() {
            PlayerCharacter existing = character(5, false, "grace");
            PlayerCharacter edits = character(999, true, "someone-else");
            when(playerCharacterDao.getCharacterById(5)).thenReturn(existing);
            when(playerCharacterDao.update(any(PlayerCharacter.class))).thenAnswer(invocation -> invocation.getArgument(0));

            PlayerCharacter result = playerCharacterService.update(5, edits, "grace", false);

            assertEquals(5, result.getId());
            assertEquals("grace", result.getCreatorUsername());
        }

        @Test
        void nonOwnerNonAdmin_cannotUpdateSomeoneElsesCharacter() {
            when(playerCharacterDao.getCharacterById(5)).thenReturn(character(5, false, "someone-else"));

            assertThrows(AccessDeniedException.class,
                    () -> playerCharacterService.update(5, character(5, false, "someone-else"), "grace", false));

            verify(playerCharacterDao, never()).update(any());
        }

        @Test
        void admin_canUpdateSomeoneElsesCharacter() {
            when(playerCharacterDao.getCharacterById(5)).thenReturn(character(5, false, "someone-else"));
            when(playerCharacterDao.update(any(PlayerCharacter.class))).thenAnswer(invocation -> invocation.getArgument(0));

            playerCharacterService.update(5, character(0, false, "irrelevant"), "grace", true);

            verify(playerCharacterDao).update(any(PlayerCharacter.class));
        }

        @Test
        void updatingACharacterThatDoesNotExist_throwsDaoException() {
            when(playerCharacterDao.getCharacterById(404)).thenReturn(null);

            assertThrows(DaoException.class,
                    () -> playerCharacterService.update(404, character(404, false, "grace"), "grace", false));

            verify(playerCharacterDao, never()).update(any());
        }
    }

    // ------------------------------------------------------------------
    // delete
    // ------------------------------------------------------------------

    @Nested
    class Delete {

        @Test
        void owner_canDeleteTheirOwnCharacter() {
            when(playerCharacterDao.getCharacterById(5)).thenReturn(character(5, false, "grace"));

            playerCharacterService.delete(5, "grace", false);

            verify(playerCharacterDao).delete(5);
        }

        @Test
        void admin_canDeleteSomeoneElsesCharacter() {
            when(playerCharacterDao.getCharacterById(5)).thenReturn(character(5, false, "someone-else"));

            playerCharacterService.delete(5, "grace", true);

            verify(playerCharacterDao).delete(5);
        }

        @Test
        void nonOwnerNonAdmin_cannotDeleteSomeoneElsesCharacter_andDaoIsNeverCalled() {
            when(playerCharacterDao.getCharacterById(5)).thenReturn(character(5, false, "someone-else"));

            assertThrows(AccessDeniedException.class,
                    () -> playerCharacterService.delete(5, "grace", false));

            verify(playerCharacterDao, never()).delete(anyInt());
        }

        @Test
        void deletingACharacterThatDoesNotExist_throwsDaoException() {
            when(playerCharacterDao.getCharacterById(404)).thenReturn(null);

            assertThrows(DaoException.class,
                    () -> playerCharacterService.delete(404, "grace", false));

            verify(playerCharacterDao, never()).delete(anyInt());
        }
    }
}
