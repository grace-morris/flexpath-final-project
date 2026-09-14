package org.example.daos;
 
import org.example.exceptions.DaoException;
import org.example.models.Monster;
import org.example.models.ResultsPage;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
 
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
 
/**
 * Data access object for monster types (also known as the Monster Manual)
 */
@Component
public class MonsterDao {
 
    private final JdbcTemplate jdbcTemplate;
 
    public MonsterDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
 
    /**
     * Get the monster by it's ID
     * @param id the id of the monster
     * @return the monster with the id
     */
    public Monster getMonsterById(int id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM monster WHERE id = ?", this::mapToMonster, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
 
    /**
     * Add a monster to the database
     * @param monster the monster instance to be added to the database
     * @return the monster that is added
     */
    public Monster create(Monster monster) {
        String sql = "INSERT INTO monster (name, monster_type, challenge_rating, armor_class, health, " +
                     "description, is_public, creator_username) VALUES (?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql, monster.getName(), monster.getMonsterType(), monster.getChallengeRating(),
                monster.getArmorClass(), monster.getHealth(), monster.getDescription(),
                monster.isPublic(), monster.getCreatorUsername());
        int newId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        return getMonsterById(newId);
    }
 
    /**
     * Update a monster in the database
     * @param monster the monster instance to be updated
     * @return the updated monster
     */
    public Monster update(Monster monster) {
        String sql = "UPDATE monster SET name = ?, monster_type = ?, challenge_rating = ?, armor_class = ?, " +
                     "health = ?, description = ?, is_public = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, monster.getName(), monster.getMonsterType(),
                monster.getChallengeRating(), monster.getArmorClass(), monster.getHealth(),
                monster.getDescription(), monster.isPublic(), monster.getId());
        if (rowsAffected == 0) {
            throw new DaoException("Monster not found.");
        }
        return getMonsterById(monster.getId());
    }
 
    /**
     * delete a monster from the database
     * @param id the id of the monster to be deleted
     * @return the id of the deleted monster
     */
    public int delete(int id) {
        return jdbcTemplate.update("DELETE FROM monster WHERE id = ?", id);
    }
 
    /**
     * Search for public or user's monsters with a given name, paginated so a large result
     * set doesn't have to be loaded (and rendered) all at once.
     * @param username user's username
     * @param isAdmin if the user is admin
     * @param name the name of the monster
     * @param sortBy sorting criteria
     * @param type type of the monster
     * @param direction sort direction
     * @param page zero-indexed page number
     * @param size how many results per page
     * @return a page of matching monsters plus the total number of matches
    */
    public ResultsPage<Monster> search(String username, boolean isAdmin, String name, String sortBy,
                                        String type, String direction, int page, int size) {
        String sortColumn;
        if (sortBy.equals("name")) {
            sortColumn = "name";
        } else if (sortBy.equals("created_at")) {
            sortColumn = "created_at";
        } else {
            sortColumn = "name";
        }
 
        String sortDirection;
        if (direction.equalsIgnoreCase("desc")) {
            sortDirection = "DESC";
        } else {
            sortDirection = "ASC";
        }
 
        StringBuilder where = new StringBuilder("WHERE ");
        List<Object> params = new ArrayList<>();
 
        if (isAdmin) { //if admin can access all monsters
            where.append("1 = 1 ");
        } else {
            where.append("(is_public = true OR creator_username = ?) ");
            params.add(username);
        }
 
        where.append("AND name LIKE ? ");
        params.add("%" + (name == null ? "" : name) + "%");
 
        if (type != null && !type.isBlank()) {
            where.append("AND monster_type = ? ");
            params.add(type);
        }
 
        int safeSize = size < 1 ? 10 : Math.min(size, 100);
        int safePage = Math.max(page, 0);
 
        String countSql = "SELECT COUNT(*) FROM monster " + where;
        Integer totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params.toArray());
 
        String sql = "SELECT * FROM monster " + where
                + "ORDER BY " + sortColumn + " " + sortDirection + " LIMIT ? OFFSET ?";
        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(safeSize);
        pageParams.add(safePage * safeSize);
 
        List<Monster> items = jdbcTemplate.query(sql, this::mapToMonster, pageParams.toArray());
 
        return new ResultsPage<>(items, totalCount == null ? 0 : totalCount);
    }
   
    /**
     * Maps a row in the ResultSet to a Monster object.
     *
     * @param resultSet The result set to map.
     * @param rowNumber The row number.
     * @return Monster The monster object.
     * @throws SQLException If an error occurs while mapping the result set.
     */
    private Monster mapToMonster(ResultSet resultSet, int rowNumber) throws SQLException {
        return new Monster(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("monster_type"),
                resultSet.getInt("health"),
                resultSet.getDouble("challenge_rating"),
                resultSet.getInt("armor_class"),
                resultSet.getString("description"),
                resultSet.getBoolean("is_public"),
                resultSet.getString("creator_username"),
                resultSet.getTimestamp("created_at")
        );
    }
}
 


