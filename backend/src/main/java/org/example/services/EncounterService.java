package org.example.services;

import org.example.daos.EncounterDao;
import org.example.exceptions.DaoException;
import org.example.models.Encounter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Ownership rules for encounters
 */
@Service
public class EncounterService {

    private final EncounterDao encounterDao;

    /**
     * Constructor for the service
     * @param encounterDao the dao for encounters
     */
    public EncounterService(EncounterDao encounterDao) {
        this.encounterDao = encounterDao;
    }

    /**
     * search through the encounters
     * @param username username of the user
     * @param isAdmin whether the user is admin
     * @param name name of the encounter
     * @param sortBy sorting criteria
     * @param direction sort direction
     * @return list of encounters
     */
    public List<Encounter> search(String username, boolean isAdmin, String name, String sortBy, String direction) {
        return encounterDao.search(username, isAdmin, name, sortBy, direction);
    }

    /**
     * gets whether the encounter is visible to the current user
     * @param id id of the encounter
     * @param username username of the user
     * @param isAdmin whether the user is admin
     * @return the encounter if it is visible
     */
    public Encounter getIsVisible(int id, String username, boolean isAdmin) {
        Encounter encounter = encounterDao.getEncounterById(id);
        if (encounter == null) {
            return null;
        }
        if (!isAdmin && !encounter.isPublic() && !encounter.getCreatorUsername().equals(username)) {
            throw new AccessDeniedException("This encounter is private.");
        }
        return encounter;
    }

    /**
     * create an encounter
     * @param encounter the encounter to be created
     * @param username username of the user
     * @return
     */
    public Encounter create(Encounter encounter, String username) {
        encounter.setCreatorUsername(username);
        return encounterDao.create(encounter);
    }

    /**
     * update an encounter
     * @param id id of the encounter to update
     * @param encounter encounter to update
     * @param username username of the user
     * @param isAdmin whether the user is admin
     * @return
     */
    public Encounter update(int id, Encounter encounter, String username, boolean isAdmin) {
        Encounter existing = canModify(id, username, isAdmin);
        encounter.setId(existing.getId());
        encounter.setCreatorUsername(existing.getCreatorUsername());
        return encounterDao.update(encounter);
    }

    /**
     * delete an encounter
     * @param id id of the encounter
     * @param username username of the user
     * @param isAdmin whether the user is admin
     */
    public void delete(int id, String username, boolean isAdmin) {
        canModify(id, username, isAdmin);
        encounterDao.delete(id);
    }

    /**
     * whether the user can modify the encounter
     * @param id id of the encounter
     * @param username username of the user
     * @param isAdmin whether the user is admin
     * @return the existing encounter
     */
    private Encounter canModify(int id, String username, boolean isAdmin) {
        Encounter existing = encounterDao.getEncounterById(id);
        if (existing == null) {
            throw new DaoException("Encounter not found.");
        }
        if (!isAdmin && !existing.getCreatorUsername().equals(username)) {
            throw new AccessDeniedException("You do not own this encounter.");
        }
        return existing;
    }
}
