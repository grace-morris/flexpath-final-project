package org.example.services;

import org.example.daos.MonsterDao;
import org.example.exceptions.DaoException;
import org.example.models.Monster;
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
 * Unit tests for {@link MonsterService} 
 *
 * canModify() is a single shared private method, so its branches only need to be proven once.
 */
@ExtendWith(MockitoExtension.class)
class MonsterServiceTest {

    @Mock
    private MonsterDao monsterDao;

    private MonsterService monsterService;

    @BeforeEach
    void setUp() {
        monsterService = new MonsterService(monsterDao);
    }

    private Monster monster(int id, boolean isPublic, String creatorUsername) {
        return new Monster(id, "Owlbear", "Monstrosity", 59, 3.0, 13,
                "A hulking mass of feathers and claws.", isPublic, creatorUsername, null, 0);
    }

    // ------------------------------------------------------------------
    // search
    // ------------------------------------------------------------------

    @Nested
    class Search {

        @Test
        void delegatesToDaoWithAllParameters_andReturnsWhateverTheDaoReturns() {
            ResultsPage<Monster> expected = new ResultsPage<>(List.of(monster(1, true, "grace")), 1);
            when(monsterDao.search("grace", false, "owl", "public", "name", "Beast", "asc", 0, 10))
                    .thenReturn(expected);

            ResultsPage<Monster> actual = monsterService.search(
                    "grace", false, "owl", "public", "name", "Beast", "asc", 0, 10);

            assertSame(expected, actual);
            verify(monsterDao).search("grace", false, "owl", "public", "name", "Beast", "asc", 0, 10);
        }
    }

    // ------------------------------------------------------------------
    // getIsVisible
    // ------------------------------------------------------------------

    @Nested
    class GetIsVisible {

        @Test
        void monsterDoesNotExist_returnsNull() {
            when(monsterDao.getMonsterById(99)).thenReturn(null);

            assertNull(monsterService.getIsVisible(99, "grace", false));
        }

        @Test
        void publicMonster_isVisibleToAnyLoggedInUser() {
            Monster publicMonster = monster(1, true, "someone-else");
            when(monsterDao.getMonsterById(1)).thenReturn(publicMonster);

            assertSame(publicMonster, monsterService.getIsVisible(1, "grace", false));
        }

        @Test
        void privateMonster_isVisibleToItsOwner() {
            Monster privateMonster = monster(1, false, "grace");
            when(monsterDao.getMonsterById(1)).thenReturn(privateMonster);

            assertSame(privateMonster, monsterService.getIsVisible(1, "grace", false));
        }

        @Test
        void privateMonster_isVisibleToAnAdmin_evenIfNotTheOwner() {
            Monster privateMonster = monster(1, false, "someone-else");
            when(monsterDao.getMonsterById(1)).thenReturn(privateMonster);

            assertSame(privateMonster, monsterService.getIsVisible(1, "grace", true));
        }

        @Test
        void privateMonster_ownedBySomeoneElse_throwsAccessDeniedForNonAdmin() {
            when(monsterDao.getMonsterById(1)).thenReturn(monster(1, false, "someone-else"));

            assertThrows(AccessDeniedException.class,
                    () -> monsterService.getIsVisible(1, "grace", false));
        }
    }

    // ------------------------------------------------------------------
    // create
    // ------------------------------------------------------------------

    @Nested
    class Create {

        @Test
        void alwaysSetsCreatorUsernameToTheCurrentUser_regardlessOfWhatWasPassedIn() {
            Monster incoming = monster(0, false, "someone-else-entirely");
            when(monsterDao.create(any(Monster.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Monster created = monsterService.create(incoming, "grace");

            ArgumentCaptor<Monster> captor = ArgumentCaptor.forClass(Monster.class);
            verify(monsterDao).create(captor.capture());
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
        void owner_canUpdateTheirOwnMonster() {
            Monster existing = monster(5, false, "grace");
            Monster edits = monster(0, true, "ignored-should-not-be-used");
            when(monsterDao.getMonsterById(5)).thenReturn(existing);
            when(monsterDao.update(any(Monster.class))).thenAnswer(invocation -> invocation.getArgument(0));

            monsterService.update(5, edits, "grace", false);

            verify(monsterDao).update(edits);
        }

        @Test
        void update_preservesTheExistingIdAndCreatorUsername_ignoringWhateverWasSubmitted() {
            Monster existing = monster(5, false, "grace");
            Monster edits = monster(999, true, "someone-else");
            when(monsterDao.getMonsterById(5)).thenReturn(existing);
            when(monsterDao.update(any(Monster.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Monster result = monsterService.update(5, edits, "grace", false);

            assertEquals(5, result.getId());
            assertEquals("grace", result.getCreatorUsername());
        }

        @Test
        void nonOwnerNonAdmin_cannotUpdateSomeoneElsesMonster() {
            when(monsterDao.getMonsterById(5)).thenReturn(monster(5, false, "someone-else"));

            assertThrows(AccessDeniedException.class,
                    () -> monsterService.update(5, monster(5, false, "someone-else"), "grace", false));

            verify(monsterDao, never()).update(any());
        }

        @Test
        void admin_canUpdateSomeoneElsesMonster() {
            when(monsterDao.getMonsterById(5)).thenReturn(monster(5, false, "someone-else"));
            when(monsterDao.update(any(Monster.class))).thenAnswer(invocation -> invocation.getArgument(0));

            monsterService.update(5, monster(0, false, "irrelevant"), "grace", true);

            verify(monsterDao).update(any(Monster.class));
        }

        @Test
        void updatingAMonsterThatDoesNotExist_throwsDaoException() {
            when(monsterDao.getMonsterById(404)).thenReturn(null);

            assertThrows(DaoException.class,
                    () -> monsterService.update(404, monster(404, false, "grace"), "grace", false));

            verify(monsterDao, never()).update(any());
        }
    }

    // ------------------------------------------------------------------
    // delete
    // ------------------------------------------------------------------

    @Nested
    class Delete {

        @Test
        void owner_canDeleteTheirOwnMonster() {
            when(monsterDao.getMonsterById(5)).thenReturn(monster(5, false, "grace"));

            monsterService.delete(5, "grace", false);

            verify(monsterDao).delete(5);
        }

        @Test
        void admin_canDeleteSomeoneElsesMonster() {
            when(monsterDao.getMonsterById(5)).thenReturn(monster(5, false, "someone-else"));

            monsterService.delete(5, "grace", true);

            verify(monsterDao).delete(5);
        }

        @Test
        void nonOwnerNonAdmin_cannotDeleteSomeoneElsesMonster_andDaoIsNeverCalled() {
            when(monsterDao.getMonsterById(5)).thenReturn(monster(5, false, "someone-else"));

            assertThrows(AccessDeniedException.class,
                    () -> monsterService.delete(5, "grace", false));

            verify(monsterDao, never()).delete(anyInt());
        }

        @Test
        void deletingAMonsterThatDoesNotExist_throwsDaoException() {
            when(monsterDao.getMonsterById(404)).thenReturn(null);

            assertThrows(DaoException.class,
                    () -> monsterService.delete(404, "grace", false));

            verify(monsterDao, never()).delete(anyInt());
        }
    }
}
