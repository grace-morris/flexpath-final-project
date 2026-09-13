package org.example.daos;

import org.example.exceptions.DaoException;
import org.example.models.PlayerCharacter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for player characters
 */
@Component
public class PlayerCharacterDao {

    private final JdbcTemplate jdbcTemplate;

    public PlayerCharacterDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Get the character by id
     * @param id the id of the character to find
     * @return the character with that id
     */
    public PlayerCharacter getCharacterById(int id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM player_character WHERE id = ?", this::mapToPC, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Add a PC to the database
     * @param character the character to add
     * @return the added character
     */
    public PlayerCharacter create(PlayerCharacter character) {
        String sql = "INSERT INTO player_character (name, character_class, level, armor_class, health, " +
                     "description, is_public, creator_username) VALUES (?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql, character.getName(), character.getCharacterClass(), character.getLevel(),
                character.getArmorClass(), character.getHealth(), character.getDescription(),
                character.isPublic(), character.getCreatorUsername());
        int newId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        return getCharacterById(newId);
    }

    /**
     * Update a PC in the database
     * @param character the character instance to update
     * @return the updated character
     */
    public PlayerCharacter update(PlayerCharacter character) {
        String sql = "UPDATE player_character SET name = ?, character_class = ?, level = ?, armor_class = ?, " +
                     "health = ?, description = ?, is_public = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, character.getName(), character.getCharacterClass(),
                character.getLevel(), character.getArmorClass(), character.getHealth(),
                character.getDescription(), character.isPublic(), character.getId());
        if (rowsAffected == 0) {
            throw new DaoException("Player character not found.");
        }
        return getCharacterById(character.getId());
    }

    /**
     * Delete a character from the database
     * @param id the id of the character to delete
     * @return the id of the deleted character
     */
    public int delete(int id) {
        return jdbcTemplate.update("DELETE FROM player_character WHERE id = ?", id);
    }

    /**
     * Search for public or user's monsters with a given name
     * @param username user's username
     * @param isAdmin if the user is admin
     * @param name the name of the character
     * @param sortBy sorting criteria
     * @param characterClass class of the character
     * @return List<Monster> the list of characters.
    */    
    public List<PlayerCharacter> search(String username, boolean isAdmin, String name, String sortBy, String characterClass, String direction) {
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

        StringBuilder sql = new StringBuilder("SELECT * FROM player_character WHERE ");
        List<Object> params = new ArrayList<>();

        if (isAdmin) { //if admin can access all characters
            sql.append("1 = 1 ");
        } else {
            sql.append("(is_public = true OR creator_username = ?) ");
            params.add(username);
        }

        sql.append("AND name LIKE ? ");
        params.add("%" + (name == null ? "" : name) + "%");

        if (characterClass != null && !characterClass.isBlank()) {
            sql.append("AND character_class = ? ");
            params.add(characterClass);
        }

        sql.append("ORDER BY ").append(sortColumn).append(" ").append(sortDirection);

        return jdbcTemplate.query(sql.toString(), this::mapToPC, params.toArray());
    }
   
    /**
     * Maps a row in the ResultSet to a PlayerCharacter object.
     *
     * @param resultSet The result set to map.
     * @param rowNumber The row number.
     * @return PlayerCharacter The PlayerCharacter object.
     * @throws SQLException If an error occurs while mapping the result set.
     */
    private PlayerCharacter mapToPC(ResultSet resultSet, int rowNum) throws SQLException {
        return new PlayerCharacter(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("character_class"),
                resultSet.getInt("health"),
                resultSet.getInt("level"),
                resultSet.getInt("armor_class"),
                resultSet.getString("description"),
                resultSet.getBoolean("is_public"),
                resultSet.getString("creator_username"),
                resultSet.getTimestamp("created_at")
        );
    }
}
