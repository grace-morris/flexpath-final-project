package org.example.services;

import org.example.daos.EncounterDao;
import org.example.daos.EncounterMonsterDao;
import org.example.daos.EncounterPlayerCharacterDao;
import org.example.exceptions.DaoException;
import org.example.models.Encounter;
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
 * Unit tests for {@link EncounterService}.
 *
 * canModify() is a single shared private method, so its branches only need to be proven once 
 * So AddMonster below carries the full permission-check suite, and every other method just gets one "happy path"
 * test proving it delegates to the right dao call with the right arguments
 */
@ExtendWith(MockitoExtension.class)
class EncounterServiceTest {

    @Mock
    private EncounterDao encounterDao;
    @Mock
    private EncounterMonsterDao encounterMonsterDao;
    @Mock
    private EncounterPlayerCharacterDao encounterPlayerCharacterDao;

    private EncounterService encounterService;

    @BeforeEach
    void setUp() {
        encounterService = new EncounterService(encounterDao, encounterMonsterDao, encounterPlayerCharacterDao);
    }

    private Encounter encounter(int id, boolean isPublic, String creatorUsername) {
        return new Encounter(id, "Goblin Ambush", "A quick skirmish on the forest road.",
                isPublic, creatorUsername, null, 1);
    }

    // ------------------------------------------------------------------
    // search
    // ------------------------------------------------------------------

    @Nested
    class Search {

        @Test
        void delegatesToDaoWithAllParameters_andReturnsWhateverTheDaoReturns() {
            ResultsPage<Encounter> expected = new ResultsPage<>(List.of(encounter(1, true, "grace")), 1);
            when(encounterDao.search("grace", false, "goblin", "public", "name", "asc", 0, 10))
                    .thenReturn(expected);

            ResultsPage<Encounter> actual = encounterService.search(
                    "grace", false, "goblin", "public", "name", "asc", 0, 10);

            assertSame(expected, actual);
            verify(encounterDao).search("grace", false, "goblin", "public", "name", "asc", 0, 10);
        }
    }

    // ------------------------------------------------------------------
    // getIsVisible
    // ------------------------------------------------------------------

    @Nested
    class GetIsVisible {

        @Test
        void encounterDoesNotExist_returnsNull() {
            when(encounterDao.getEncounterById(99)).thenReturn(null);

            assertNull(encounterService.getIsVisible(99, "grace", false));
        }

        @Test
        void publicEncounter_isVisibleToAnyLoggedInUser() {
            Encounter publicEncounter = encounter(1, true, "someone-else");
            when(encounterDao.getEncounterById(1)).thenReturn(publicEncounter);

            assertSame(publicEncounter, encounterService.getIsVisible(1, "grace", false));
        }

        @Test
        void privateEncounter_isVisibleToItsOwner() {
            Encounter privateEncounter = encounter(1, false, "grace");
            when(encounterDao.getEncounterById(1)).thenReturn(privateEncounter);

            assertSame(privateEncounter, encounterService.getIsVisible(1, "grace", false));
        }

        @Test
        void privateEncounter_isVisibleToAnAdmin_evenIfNotTheOwner() {
            Encounter privateEncounter = encounter(1, false, "someone-else");
            when(encounterDao.getEncounterById(1)).thenReturn(privateEncounter);

            assertSame(privateEncounter, encounterService.getIsVisible(1, "grace", true));
        }

        @Test
        void privateEncounter_ownedBySomeoneElse_throwsAccessDeniedForNonAdmin() {
            when(encounterDao.getEncounterById(1)).thenReturn(encounter(1, false, "someone-else"));

            assertThrows(AccessDeniedException.class,
                    () -> encounterService.getIsVisible(1, "grace", false));
        }
    }

    // ------------------------------------------------------------------
    // create
    // ------------------------------------------------------------------

    @Nested
    class Create {

        @Test
        void alwaysSetsCreatorUsernameToTheCurrentUser_regardlessOfWhatWasPassedIn() {
            Encounter incoming = encounter(0, false, "someone-else-entirely");
            when(encounterDao.create(any(Encounter.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Encounter created = encounterService.create(incoming, "grace");

            ArgumentCaptor<Encounter> captor = ArgumentCaptor.forClass(Encounter.class);
            verify(encounterDao).create(captor.capture());
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
        void owner_canUpdateTheirOwnEncounter() {
            Encounter existing = encounter(5, false, "grace");
            Encounter edits = encounter(0, true, "ignored-should-not-be-used");
            when(encounterDao.getEncounterById(5)).thenReturn(existing);
            when(encounterDao.update(any(Encounter.class))).thenAnswer(invocation -> invocation.getArgument(0));

            encounterService.update(5, edits, "grace", false);

            verify(encounterDao).update(edits);
        }

        @Test
        void update_preservesTheExistingIdAndCreatorUsername_ignoringWhateverWasSubmitted() {
            Encounter existing = encounter(5, false, "grace");
            Encounter edits = encounter(999, true, "someone-else");
            when(encounterDao.getEncounterById(5)).thenReturn(existing);
            when(encounterDao.update(any(Encounter.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Encounter result = encounterService.update(5, edits, "grace", false);

            assertEquals(5, result.getId());
            assertEquals("grace", result.getCreatorUsername());
        }

        @Test
        void nonOwnerNonAdmin_cannotUpdateSomeoneElsesEncounter() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "someone-else"));

            assertThrows(AccessDeniedException.class,
                    () -> encounterService.update(5, encounter(5, false, "someone-else"), "grace", false));

            verify(encounterDao, never()).update(any());
        }

        @Test
        void admin_canUpdateSomeoneElsesEncounter() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "someone-else"));
            when(encounterDao.update(any(Encounter.class))).thenAnswer(invocation -> invocation.getArgument(0));

            encounterService.update(5, encounter(0, false, "irrelevant"), "grace", true);

            verify(encounterDao).update(any(Encounter.class));
        }

        @Test
        void updatingAnEncounterThatDoesNotExist_throwsDaoException() {
            when(encounterDao.getEncounterById(404)).thenReturn(null);

            assertThrows(DaoException.class,
                    () -> encounterService.update(404, encounter(404, false, "grace"), "grace", false));

            verify(encounterDao, never()).update(any());
        }
    }

    // ------------------------------------------------------------------
    // delete
    // ------------------------------------------------------------------

    @Nested
    class Delete {

        @Test
        void owner_canDeleteTheirOwnEncounter() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "grace"));

            encounterService.delete(5, "grace", false);

            verify(encounterDao).delete(5);
        }

        @Test
        void admin_canDeleteSomeoneElsesEncounter() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "someone-else"));

            encounterService.delete(5, "grace", true);

            verify(encounterDao).delete(5);
        }

        @Test
        void nonOwnerNonAdmin_cannotDeleteSomeoneElsesEncounter_andDaoIsNeverCalled() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "someone-else"));

            assertThrows(AccessDeniedException.class,
                    () -> encounterService.delete(5, "grace", false));

            verify(encounterDao, never()).delete(anyInt());
        }

        @Test
        void deletingAnEncounterThatDoesNotExist_throwsDaoException() {
            when(encounterDao.getEncounterById(404)).thenReturn(null);

            assertThrows(DaoException.class,
                    () -> encounterService.delete(404, "grace", false));

            verify(encounterDao, never()).delete(anyInt());
        }
    }

    // ------------------------------------------------------------------
    // nextRound
    // ------------------------------------------------------------------

    @Nested
    class NextRound {

        @Test
        void owner_advancesTheRound_andResetsBothMonsterAndCharacterRoundState() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "grace"));
            Encounter advanced = encounter(5, false, "grace");
            advanced.setCurrentRound(2);
            when(encounterDao.incrementRound(5)).thenReturn(advanced);

            Encounter result = encounterService.nextRound(5, "grace", false);

            assertSame(advanced, result);
            verify(encounterDao).incrementRound(5);
            verify(encounterMonsterDao).resetRoundState(5);
            verify(encounterPlayerCharacterDao).resetRoundState(5);
        }

        @Test
        void nonOwnerNonAdmin_cannotAdvanceTheRound_andNothingIsReset() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "someone-else"));

            assertThrows(AccessDeniedException.class,
                    () -> encounterService.nextRound(5, "grace", false));

            verify(encounterDao, never()).incrementRound(anyInt());
            verify(encounterMonsterDao, never()).resetRoundState(anyInt());
            verify(encounterPlayerCharacterDao, never()).resetRoundState(anyInt());
        }

        @Test
        void advancingARoundForAnEncounterThatDoesNotExist_throwsDaoException() {
            when(encounterDao.getEncounterById(404)).thenReturn(null);

            assertThrows(DaoException.class,
                    () -> encounterService.nextRound(404, "grace", false));

            verify(encounterDao, never()).incrementRound(anyInt());
            verify(encounterMonsterDao, never()).resetRoundState(anyInt());
            verify(encounterPlayerCharacterDao, never()).resetRoundState(anyInt());
        }
    }
}
