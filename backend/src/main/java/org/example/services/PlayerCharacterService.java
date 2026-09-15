package org.example.services;
 
import org.example.daos.PlayerCharacterDao;
import org.example.exceptions.DaoException;
import org.example.models.PlayerCharacter;
import org.example.models.ResultsPage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
 
/**
 * Ownership rules for characters
 */
@Service
public class PlayerCharacterService {
 
    private final PlayerCharacterDao playerCharacterDao;
 
    /**
     * Constructor for the service
     * @param PlayerCharacterDao the dao for playerCharacters
     */
    public PlayerCharacterService(PlayerCharacterDao playerCharacterDao) {
        this.playerCharacterDao = playerCharacterDao;
    }
 
    /**
     * search through the characters
     * @param username username of the user
     * @param isAdmin whether the user is admin
     * @param name name of the character
     * @param visibility public, mine, or all
     * @param sortBy sorting criteria
     * @param characterClass the class of character
     * @param direction sort direction
     * @param page zero-indexed page number
     * @param size how many results per page
     * @return a page of characters plus the total number of matches
     */
    public ResultsPage<PlayerCharacter> search(String username, boolean isAdmin, String name, String visibility, String sortBy,
                                                String characterClass, String direction, int page, int size) {
        return playerCharacterDao.search(username, isAdmin, name, visibility, sortBy, characterClass, direction, page, size);
    }
 
    /**
     * gets whether the character is visible to the current user
     * @param id id of the character
     * @param username username of the user
     * @param isAdmin whether the user is admin
     * @return the character if it is visible
     */
    public PlayerCharacter getIsVisible(int id, String username, boolean isAdmin) {
        PlayerCharacter playerCharacter = playerCharacterDao.getCharacterById(id);
        if (playerCharacter == null) {
            return null;
        }
        if (!isAdmin && !playerCharacter.isPublic() && !playerCharacter.getCreatorUsername().equals(username)) {
            throw new AccessDeniedException("This character is private.");
        }
        return playerCharacter;
    }
 
    /**
     * create an character
     * @param playerCharacter the character to be created
     * @param username username of the user
     * @return
     */
    public PlayerCharacter create(PlayerCharacter playerCharacter, String username) {
        playerCharacter.setCreatorUsername(username);
        return playerCharacterDao.create(playerCharacter);
    }
 
    /**
     * update an character
     * @param id id of the character to update
     * @param playerCharacter character to update
     * @param username username of the user
     * @param isAdmin whether the user is admin
     * @return
     */
    public PlayerCharacter update(int id, PlayerCharacter playerCharacter, String username, boolean isAdmin) {
        PlayerCharacter existing = canModify(id, username, isAdmin);
        playerCharacter.setId(existing.getId());
        playerCharacter.setCreatorUsername(existing.getCreatorUsername());
        return playerCharacterDao.update(playerCharacter);
    }
 
    /**
     * delete an character
     * @param id id of the character
     * @param username username of the user
     * @param isAdmin whether the user is admin
     */
    public void delete(int id, String username, boolean isAdmin) {
        canModify(id, username, isAdmin);
        playerCharacterDao.delete(id);
    }
 
    /**
     * whether the user can modify the character
     * @param id id of the character
     * @param username username of the user
     * @param isAdmin whether the user is admin
     * @return the existing character
     */
    private PlayerCharacter canModify(int id, String username, boolean isAdmin) {
        PlayerCharacter existing = playerCharacterDao.getCharacterById(id);
        if (existing == null) {
            throw new DaoException("Character not found.");
        }
        if (!isAdmin && !existing.getCreatorUsername().equals(username)) {
            throw new AccessDeniedException("You do not own this character.");
        }
        return existing;
    }
}
 


