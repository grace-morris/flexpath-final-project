package org.example.services;

import org.example.daos.EncounterDao;
import org.example.daos.EncounterMonsterDao;
import org.example.exceptions.DaoException;
import org.example.models.Encounter;
import org.example.models.EncounterMonster;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Determines the actions of the admin (add, damage/heal, or remove)
 * and not regular users
 */
@Service
public class EncounterMonsterService {

    private final EncounterMonsterDao encounterMonsterDao;
    private final EncounterDao encounterDao;

    /**
     * Constructor for the service
     * @param encounterMonsterDao the dao for the specifc monster's class
     * @param encounterDao the dao for the encounter
     */
    public EncounterMonsterService(EncounterMonsterDao encounterMonsterDao, EncounterDao encounterDao) {
        this.encounterMonsterDao = encounterMonsterDao;
        this.encounterDao = encounterDao;
    }

    /**
     * Gets the list of monsters in the encounter
     * @param encounterId the Id for the encounter
     * @return the list of monsters in this encounter
     */
    public List<EncounterMonster> getMonsterList(int encounterId) {
        return encounterMonsterDao.getByEncounterId(encounterId);
    }

    /**
     * Adds a monster to the encounter.
     * @param encounterId the id of the encounter to modify
     * @param monsterId the id of the monster type to add
     * @param username the username of the user
     * @param isAdmin whether the user is admin
     * @return the monster to be added
     */
    public EncounterMonster addMonster(int encounterId, int monsterId, String username, boolean isAdmin) {
        canModify(encounterId, username, isAdmin);
        return encounterMonsterDao.addMonsterToEncounter(encounterId, monsterId);
    }

    /**
     * Update the health of the monster in the encounter
     * @param encounterId the id of the encounter
     * @param monsterId the id of the monster
     * @param newHealth the updated health of the monster
     * @param username the username of the user
     * @param isAdmin whether the user is admin
     * @return the updated monster
     */
    public EncounterMonster updateHealth(int encounterId, int monsterId, int newHealth,
                                         String username, boolean isAdmin) {
        canModify(encounterId, username, isAdmin);
        return encounterMonsterDao.updateHealth(monsterId, newHealth);
    }

    /**
     * Remove a monster from the encounter
     * @param encounterId the id of the encounter
     * @param monsterId the id of the monster
     * @param username the username of the user
     * @param isAdmin whether the user is admin
     */
    public void removeMonster(int encounterId, int monsterId, String username, boolean isAdmin) {
        canModify(encounterId, username, isAdmin);
        encounterMonsterDao.remove(monsterId);
    }

    /**
     * Update the initiative (turn order) of the monster in the encounter
     * @param encounterId the id of the encounter
     * @param monsterId the id of the monster
     * @param initiative the new initiative value
     * @param username the username of the user
     * @param isAdmin whether the user is admin
     * @return the updated monster
     */
    public EncounterMonster updateInitiative(int encounterId, int monsterId, int initiative,
                                             String username, boolean isAdmin) {
        canModify(encounterId, username, isAdmin);
        return encounterMonsterDao.updateInitiative(monsterId, initiative);
    }

    /**
     * Toggle whether the monster has used its reaction this round
     * @param encounterId the id of the encounter
     * @param monsterId the id of the monster
     * @param usedReaction whether the reaction has been used
     * @param username the username of the user
     * @param isAdmin whether the user is admin
     * @return the updated monster
     */
    public EncounterMonster setUsedReaction(int encounterId, int monsterId, boolean usedReaction,
                                            String username, boolean isAdmin) {
        canModify(encounterId, username, isAdmin);
        return encounterMonsterDao.setUsedReaction(monsterId, usedReaction);
    }

    /**
     * Spend one of the monster's legendary actions for this round
     * @param encounterId the id of the encounter
     * @param monsterId the id of the monster
     * @param username the username of the user
     * @param isAdmin whether the user is admin
     * @return the updated monster
     */
    public EncounterMonster useLegendaryAction(int encounterId, int monsterId, String username, boolean isAdmin) {
        canModify(encounterId, username, isAdmin);
        return encounterMonsterDao.useLegendaryAction(monsterId);
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
