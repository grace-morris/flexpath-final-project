package org.example.services;
 
import org.example.daos.EncounterDao;
import org.example.daos.EncounterPlayerCharacterDao;
import org.example.exceptions.DaoException;
import org.example.models.Encounter;
import org.example.models.EncounterPlayerCharacter;
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
 * Unit tests for {@link EncounterPlayerCharacterService}.
 *
 * Same shape as EncounterMonsterServiceTest, minus legendary actions
 */
@ExtendWith(MockitoExtension.class)
class EncounterPlayerCharacterServiceTest {
 
    @Mock
    private EncounterPlayerCharacterDao encounterPlayerCharacterDao;
    @Mock
    private EncounterDao encounterDao;
 
    private EncounterPlayerCharacterService encounterPlayerCharacterService;
 
    @BeforeEach
    void setUp() {
        encounterPlayerCharacterService = new EncounterPlayerCharacterService(encounterPlayerCharacterDao, encounterDao);
    }
 
    private Encounter encounter(int id, boolean isPublic, String creatorUsername) {
        return new Encounter(id, "Goblin Ambush", "A quick skirmish.", isPublic, creatorUsername, null, 1);
    }

    @Nested
    class GetPlayerCharacterList {
 
        @Test
        void publicEncounter_isVisibleToAnyLoggedInUser() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, true, "someone-else"));
            List<EncounterPlayerCharacter> expected = List.of(mock(EncounterPlayerCharacter.class));
            when(encounterPlayerCharacterDao.getByEncounterId(5)).thenReturn(expected);
 
            List<EncounterPlayerCharacter> actual = encounterPlayerCharacterService.getPlayerCharacterList(5, "grace", false);
 
            assertSame(expected, actual);
        }
 
        @Test
        void privateEncounter_isVisibleToItsOwner() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "grace"));
 
            encounterPlayerCharacterService.getPlayerCharacterList(5, "grace", false);
 
            verify(encounterPlayerCharacterDao).getByEncounterId(5);
        }
 
        @Test
        void privateEncounter_isVisibleToAnAdmin_evenIfNotTheOwner() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "someone-else"));
 
            encounterPlayerCharacterService.getPlayerCharacterList(5, "grace", true);
 
            verify(encounterPlayerCharacterDao).getByEncounterId(5);
        }
 
        @Test
        void privateEncounter_ownedBySomeoneElse_throwsAccessDeniedForNonAdmin_andDaoIsNeverCalled() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "someone-else"));
 
            assertThrows(AccessDeniedException.class,
                    () -> encounterPlayerCharacterService.getPlayerCharacterList(5, "grace", false));
 
            verify(encounterPlayerCharacterDao, never()).getByEncounterId(anyInt());
        }
 
        @Test
        void encounterDoesNotExist_throwsDaoException() {
            when(encounterDao.getEncounterById(404)).thenReturn(null);
 
            assertThrows(DaoException.class,
                    () -> encounterPlayerCharacterService.getPlayerCharacterList(404, "grace", false));
        }
    }
 
    @Nested
    class AddPlayerCharacter {
 
        @Test
        void owner_canAddACharacter() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "grace"));
            EncounterPlayerCharacter added = mock(EncounterPlayerCharacter.class);
            when(encounterPlayerCharacterDao.addCharacterToEncounter(5, 12)).thenReturn(added);
 
            EncounterPlayerCharacter result = encounterPlayerCharacterService.addPlayerCharacter(5, 12, "grace", false);
 
            assertSame(added, result);
        }
 
        @Test
        void admin_canAddACharacter_toSomeoneElsesEncounter() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "someone-else"));
 
            encounterPlayerCharacterService.addPlayerCharacter(5, 12, "grace", true);
 
            verify(encounterPlayerCharacterDao).addCharacterToEncounter(5, 12);
        }
 
        @Test
        void nonOwnerNonAdmin_cannotAddACharacter_andDaoIsNeverCalled() {
            when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "someone-else"));
 
            assertThrows(AccessDeniedException.class,
                    () -> encounterPlayerCharacterService.addPlayerCharacter(5, 12, "grace", false));
 
            verify(encounterPlayerCharacterDao, never()).addCharacterToEncounter(anyInt(), anyInt());
        }
 
        @Test
        void addingToAnEncounterThatDoesNotExist_throwsDaoException() {
            when(encounterDao.getEncounterById(404)).thenReturn(null);
 
            assertThrows(DaoException.class,
                    () -> encounterPlayerCharacterService.addPlayerCharacter(404, 12, "grace", false));
 
            verify(encounterPlayerCharacterDao, never()).addCharacterToEncounter(anyInt(), anyInt());
        }
    }
 

    @Test
    void updateHealth_delegatesToTheDaoWithTheNewHealthValue() {
        when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "grace"));
        EncounterPlayerCharacter updated = mock(EncounterPlayerCharacter.class);
        when(encounterPlayerCharacterDao.updateHealth(12, 8)).thenReturn(updated);
 
        EncounterPlayerCharacter result = encounterPlayerCharacterService.updateHealth(5, 12, 8, "grace", false);
 
        assertSame(updated, result);
    }
 
    @Test
    void removePlayerCharacter_delegatesToTheDaoWithTheCharacterId() {
        when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "grace"));
 
        encounterPlayerCharacterService.removePlayerCharacter(5, 12, "grace", false);
 
        verify(encounterPlayerCharacterDao).remove(12);
    }
 
    @Test
    void updateInitiative_delegatesToTheDaoWithTheNewInitiativeValue() {
        when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "grace"));
        EncounterPlayerCharacter updated = mock(EncounterPlayerCharacter.class);
        when(encounterPlayerCharacterDao.updateInitiative(12, 20)).thenReturn(updated);
 
        EncounterPlayerCharacter result = encounterPlayerCharacterService.updateInitiative(5, 12, 20, "grace", false);
 
        assertSame(updated, result);
    }
 
    @Test
    void setUsedReaction_delegatesToTheDaoWithTheFlagValue() {
        when(encounterDao.getEncounterById(5)).thenReturn(encounter(5, false, "grace"));
        EncounterPlayerCharacter updated = mock(EncounterPlayerCharacter.class);
        when(encounterPlayerCharacterDao.setUsedReaction(12, true)).thenReturn(updated);
 
        EncounterPlayerCharacter result = encounterPlayerCharacterService.setUsedReaction(5, 12, true, "grace", false);
 
        assertSame(updated, result);
    }
}
 