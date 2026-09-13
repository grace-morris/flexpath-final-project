package org.example.daos;

import org.example.exceptions.DaoException;
import org.example.models.Monster;
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
     * Search for public or user's monsters with a given name
     * @param username user's username
     * @param isAdmin if the user is admin
     * @param name the name of the monster
     * @param sortBy sorting criteria
     * @param type type of the monster
     * @return List<Monster> the list of monsters.
    */    
    public List<Monster> search(String username, boolean isAdmin, String name, String sortBy, String type, String direction) {
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

        StringBuilder sql = new StringBuilder("SELECT * FROM monster WHERE ");
        List<Object> params = new ArrayList<>();

        if (isAdmin) { //if admin can access all monsters
            sql.append("1 = 1 ");
        } else {
            sql.append("(is_public = true OR creator_username = ?) ");
            params.add(username);
        }

        sql.append("AND name LIKE ? ");
        params.add("%" + (name == null ? "" : name) + "%");

        if (type != null && !type.isBlank()) {
            sql.append("AND monster_type = ? ");
            params.add(type);
        }

        sql.append("ORDER BY ").append(sortColumn).append(" ").append(sortDirection);

        return jdbcTemplate.query(sql.toString(), this::mapToMonster, params.toArray());
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
