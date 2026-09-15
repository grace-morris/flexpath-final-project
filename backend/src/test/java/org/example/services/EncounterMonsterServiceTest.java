package org.example.services;
 
import org.example.daos.EncounterDao;
import org.example.daos.EncounterMonsterDao;
import org.example.exceptions.DaoException;
import org.example.models.Encounter;
import org.example.models.EncounterMonster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
 
import java.util.List;
 
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
 
/**
 * Unit tests for {@link EncounterMonsterService}.
 * 
 * canModify() is a single shared private method, so its branches only need to be proven once 
 * AddMonster below carries the full permission-check suite, and every other method just gets one "happy path"
 * test proving *it* delegates to the right dao call with the right arguments
 */
@ExtendWith(MockitoExtension.class)
class EncounterMonsterServiceTest {
 
    @Mock
    private EncounterMonsterDao encounterMonsterDao;
    @Mock
    private EncounterDao encounterDao;
 
    private EncounterMonsterService encounterMonsterService;
 
    @BeforeEach
    void setUp() {
        encounterMonsterService = new EncounterMonsterService(encounterMonsterDao, encounterDao);
    }
 
    private Encounter encounter(int id, boolean isPublic, String creatorUsername) {
        return new Encounter(id, "Goblin Ambush", "A quick skirmish.", isPublic, creatorUsername, null, 1);
    }

    @Nested
    class GetMonsterList {
 
        @Test
        void publicEncounter_isVisibleToAnyLoggedInUser() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, true, "someone-else"));
            List<EncounterMonster> expected = List.of(mock(EncounterMonster.class));
            when(encounterMonsterDao.getByEncounterId(5)).thenReturn(expected);
 
            List<EncounterMonster> actual = encounterMonsterService.getMonsterList(5, "grace", false);
 
            assertSame(expected, actual);
        }
 
        @Test
        void privateEncounter_isVisibleToItsOwner() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "grace"));
 
            encounterMonsterService.getMonsterList(5, "grace", false);
 
            verify(encounterMonsterDao).getByEncounterId(5);
        }
 
        @Test
        void privateEncounter_isVisibleToAnAdmin_evenIfNotTheOwner() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "someone-else"));
 
            encounterMonsterService.getMonsterList(5, "grace", true);
 
            verify(encounterMonsterDao).getByEncounterId(5);
        }
 
        @Test
        void privateEncounter_ownedBySomeoneElse_throwsAccessDeniedForNonAdmin_andDaoIsNeverCalled() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "someone-else"));
 
            assertThrows(AccessDeniedException.class,
                    () -> encounterMonsterService.getMonsterList(5, "grace", false));
 
            verify(encounterMonsterDao, never()).getByEncounterId(anyInt());
        }
 
        @Test
        void encounterDoesNotExist_throwsDaoException() {
            when(encounterDao.getEncounterById(404)).thenReturn(null);
 
            assertThrows(DaoException.class, () -> encounterMonsterService.getMonsterList(404, "grace", false));
        }
    }
 
    @Nested
    class AddMonster {
 
        @Test
        void owner_canAddAMonster() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "grace"));
            EncounterMonster added = mock(EncounterMonster.class);
            when(encounterMonsterDao.addMonsterToEncounter(5, 12)).thenReturn(added);
 
            EncounterMonster result = encounterMonsterService.addMonster(5, 12, "grace", false);
 
            assertSame(added, result);
        }
 
        @Test
        void admin_canAddAMonster_toSomeoneElsesEncounter() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "someone-else"));
 
            encounterMonsterService.addMonster(5, 12, "grace", true);
 
            verify(encounterMonsterDao).addMonsterToEncounter(5, 12);
        }
 
        @Test
        void nonOwnerNonAdmin_cannotAddAMonster_andDaoIsNeverCalled() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "someone-else"));
 
            assertThrows(AccessDeniedException.class,
                    () -> encounterMonsterService.addMonster(5, 12, "grace", false));
 
            verify(encounterMonsterDao, never()).addMonsterToEncounter(anyInt(), anyInt());
        }
 
        @Test
        void addingToAnEncounterThatDoesNotExist_throwsDaoException() {
            when(encounterDao.getEncounterById(404)).thenReturn(null);
 
            assertThrows(DaoException.class,
                    () -> encounterMonsterService.addMonster(404, 12, "grace", false));
 
            verify(encounterMonsterDao, never()).addMonsterToEncounter(anyInt(), anyInt());
        }
    }
 
    @Test
    void updateHealth_delegatesToTheDaoWithTheNewHealthValue() {
        when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "grace"));
        EncounterMonster updated = mock(EncounterMonster.class);
        when(encounterMonsterDao.updateHealth(12, 8)).thenReturn(updated);
 
        EncounterMonster result = encounterMonsterService.updateHealth(5, 12, 8, "grace", false);
 
        assertSame(updated, result);
    }
 
    @Test
    void removeMonster_delegatesToTheDaoWithTheMonsterId() {
        when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "grace"));
 
        encounterMonsterService.removeMonster(5, 12, "grace", false);
 
        verify(encounterMonsterDao).remove(12);
    }
 
    @Test
    void updateInitiative_delegatesToTheDaoWithTheNewInitiativeValue() {
        when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "grace"));
        EncounterMonster updated = mock(EncounterMonster.class);
        when(encounterMonsterDao.updateInitiative(12, 20)).thenReturn(updated);
 
        EncounterMonster result = encounterMonsterService.updateInitiative(5, 12, 20, "grace", false);
 
        assertSame(updated, result);
    }
 
    @Test
    void setUsedReaction_delegatesToTheDaoWithTheFlagValue() {
        when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "grace"));
        EncounterMonster updated = mock(EncounterMonster.class);
        when(encounterMonsterDao.setUsedReaction(12, true)).thenReturn(updated);
 
        EncounterMonster result = encounterMonsterService.setUsedReaction(5, 12, true, "grace", false);
 
        assertSame(updated, result);
    }
 
    @Test
    void useLegendaryAction_delegatesToTheDaoWithTheMonsterId() {
        when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "grace"));
        EncounterMonster updated = mock(EncounterMonster.class);
        when(encounterMonsterDao.useLegendaryAction(12)).thenReturn(updated);
 
        EncounterMonster result = encounterMonsterService.useLegendaryAction(5, 12, "grace", false);
 
        assertSame(updated, result);
    }
}