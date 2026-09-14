package org.example.services;
 
import org.example.daos.MonsterDao;
import org.example.exceptions.DaoException;
import org.example.models.Monster;
import org.example.models.ResultsPage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
 
/**
 * Ownership rules for monsters
 */
@Service
public class MonsterService {
 
    private final MonsterDao monsterDao;
 
    /**
     * Constructor for the service
     * @param MonsterDao the dao for monsters
     */
    public MonsterService(MonsterDao monsterDao) {
        this.monsterDao = monsterDao;
    }
 
    /**
     * search through the monsters
     * @param username username of the user
     * @param isAdmin whether the user is admin
     * @param name name of the monster
     * @param sortBy sorting criteria
     * @param type the type of monster
     * @param direction sort direction
     * @param page zero-indexed page number
     * @param size how many results per page
     * @return a page of monsters plus the total number of matches
     */
    public ResultsPage<Monster> search(String username, boolean isAdmin, String name, String sortBy,
                                        String type, String direction, int page, int size) {
        return monsterDao.search(username, isAdmin, name, sortBy, type, direction, page, size);
    }
 
    /**
     * gets whether the monster is visible to the current user
     * @param id id of the monster
     * @param username username of the user
     * @param isAdmin whether the user is admin
     * @return the monster if it is visible
     */
    public Monster getIsVisible(int id, String username, boolean isAdmin) {
        Monster monster = monsterDao.getMonsterById(id);
        if (monster == null) {
            return null;
        }
        if (!isAdmin && !monster.isPublic() && !monster.getCreatorUsername().equals(username)) {
            throw new AccessDeniedException("This monster is private.");
        }
        return monster;
    }
 
    /**
     * create an monster
     * @param monster the monster to be created
     * @param username username of the user
     * @return
     */
    public Monster create(Monster monster, String username) {
        monster.setCreatorUsername(username);
        return monsterDao.create(monster);
    }
 
    /**
     * update an monster
     * @param id id of the monster to update
     * @param monster monster to update
     * @param username username of the user
     * @param isAdmin whether the user is admin
     * @return
     */
    public Monster update(int id, Monster monster, String username, boolean isAdmin) {
        Monster existing = canModify(id, username, isAdmin);
        monster.setId(existing.getId());
        monster.setCreatorUsername(existing.getCreatorUsername());
        return monsterDao.update(monster);
    }
 
    /**
     * delete an monster
     * @param id id of the monster
     * @param username username of the user
     * @param isAdmin whether the user is admin
     */
    public void delete(int id, String username, boolean isAdmin) {
        canModify(id, username, isAdmin);
        monsterDao.delete(id);
    }
 
    /**
     * whether the user can modify the monster
     * @param id id of the monster
     * @param username username of the user
     * @param isAdmin whether the user is admin
     * @return the existing monster
     */
    private Monster canModify(int id, String username, boolean isAdmin) {
        Monster existing = monsterDao.getMonsterById(id);
        if (existing == null) {
            throw new DaoException("Monster not found.");
        }
        if (!isAdmin && !existing.getCreatorUsername().equals(username)) {
            throw new AccessDeniedException("You do not own this monster.");
        }
        return existing;
    }
}
 


