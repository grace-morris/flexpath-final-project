package org.example.services;

import org.example.daos.EncounterDao;
import org.example.daos.EncounterPlayerCharacterDao;
import org.example.exceptions.DaoException;
import org.example.models.Encounter;
import org.example.models.EncounterPlayerCharacter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Determines the actions of the admin (add, damage/heal, or remove)
 * and not regular users
 */
@Service
public class EncounterPlayerCharacterService {

    private final EncounterPlayerCharacterDao encounterPlayerCharacterDao;
    private final EncounterDao encounterDao;

    /**
     * Constructor for the service
     * @param encounterPlayerCharacterDao the dao for the specifc character's class
     * @param encounterDao the dao for the encounter
     */
    public EncounterPlayerCharacterService(EncounterPlayerCharacterDao encounterPlayerCharacterDao, EncounterDao encounterDao) {
        this.encounterPlayerCharacterDao = encounterPlayerCharacterDao;
        this.encounterDao = encounterDao;
    }

    /**
     * Gets the list of characters in the encounter
     * @param encounterId the Id for the encounter
     * @return the list of characters in this encounter
     */
    public List<EncounterPlayerCharacter> getPlayerCharacterList(int encounterId) {
        return encounterPlayerCharacterDao.getByEncounterId(encounterId);
    }

    /**
     * Adds a character to the encounter.
     * @param encounterId the id of the encounter to modify
     * @param characterId the id of the character type to add
     * @param username the username of the user
     * @param isAdmin whether the user is admin
     * @return the character to be added
     */
    public EncounterPlayerCharacter addPlayerCharacter(int encounterId, int characterId, String username, boolean isAdmin) {
        canModify(encounterId, username, isAdmin);
        return encounterPlayerCharacterDao.addCharacterToEncounter(encounterId, characterId);
    }

    /**
     * Update the health of the character in the encounter
     * @param encounterId the id of the encounter
     * @param characterId the id of the character
     * @param newHealth the updated health of the character
     * @param username the username of the user
     * @param isAdmin whether the user is admin
     * @return the updated character
     */
    public EncounterPlayerCharacter updateHealth(int encounterId, int characterId, int newHealth,
                                             String username, boolean isAdmin) {
        canModify(encounterId, username, isAdmin);
        return encounterPlayerCharacterDao.updateHealth(characterId, newHealth);
    }

    /**
     * Remove a character from the encounter
     * @param encounterId the id of the encounter
     * @param characterId the id of the character
     * @param username the username of the user
     * @param isAdmin whether the user is admin
     */
    public void removePlayerCharacter(int encounterId, int characterId, String username, boolean isAdmin) {
        canModify(encounterId, username, isAdmin);
        encounterPlayerCharacterDao.remove(characterId);
    }

    /**
     * Checks ownership of the encounter. 
     * Chose this approach instead of @Preauthorize because @Preauthorize
     * can't see into the database.
     * @param encounterId the id of the encounter
     * @param username the username of the user
     * @param isAdmin whether the user is Admin
     */
    private void canModify(int encounterId, String username, boolean isAdmin) {
        Encounter encounter = encounterDao.getEncounterById(encounterId);
        if (encounter == null) {
            throw new DaoException("Encounter not found.");
        }
        if (!isAdmin && !encounter.getCreatorUsername().equals(username)) {
            throw new AccessDeniedException("You do not own this encounter.");
        }
    }
}
