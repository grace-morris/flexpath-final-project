package org.example.daos;

import org.example.exceptions.DaoException;
import org.example.models.Encounter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for encounters.
 */
@Component
public class EncounterDao {
    /**
     * The JDBC template for querying the database.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a new user data access object.
     *
     * @param dataSource The data source for the DAO.
     */
    public EncounterDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Gets all user's encounters
     * @param username the user to get the encounters
     * @return List of Encounters
     */
    public List<Encounter> getUsersEncounters(String username) {
        try {
            return jdbcTemplate.query("SELECT * FROM encounters WHERE creatorUsername = ?", this::mapToEncounter, username);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Gets all public encounters
     *
     * @return List of Encounters
     */
    public List<Encounter> getPublicEncounters() {
        return jdbcTemplate.query("SELECT * FROM encounter WHERE is_public = TRUE;", this::mapToEncounter);
    }

    /**
     * Gets encounter by Id
     *
     * @return Encounter with id
    */
     public Encounter getEncounterById(int id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM encounter WHERE id = ?", this::mapToEncounter, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }   

    /**
     * Create new encounter
     * @param encounter the encounter to be created.
     * @return Encounter the new encounter.
    */
     public Encounter create(Encounter encounter) {
        String sql = "INSERT INTO encounter (name, description, is_public, creator_username) VALUES (?,?,?,?)";
        jdbcTemplate.update(sql, encounter.getName(), encounter.getDescription(),
                encounter.isPublic(), encounter.getCreatorUsername());
        int newId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        return getEncounterById(newId);
    }

    /**
     * Update an encounter
     * @param encounter the encounter to be updated
     * @return Encounter the new encounter.
    */
    public Encounter update(Encounter encounter) {
        String sql = "UPDATE encounter SET name = ?, description = ?, is_public = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, encounter.getName(), encounter.getDescription(),
                encounter.isPublic(), encounter.getId());
        if (rowsAffected == 0) {
            throw new DaoException("Encounter not found.");
        }
        return getEncounterById(encounter.getId());
    }

    /**
     * Delete an encounter
     * @param id the id of the encounter to be deleted
     * @return Encounter the new encounter.
    */
    public int delete(int id) {
        return jdbcTemplate.update("DELETE FROM encounter WHERE id = ?", id);
    }

    /**
     * Search for public or user's encounters with a given name
     * @param id the id of the encounter to be searched for
     * @return List<Encounter> the list of encounters searched for
    */    
    public List<Encounter> search(String username, boolean isAdmin, String name, String sortBy, String direction) {
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

        StringBuilder sql = new StringBuilder("SELECT * FROM encounter WHERE ");
        List<Object> params = new ArrayList<>();

        if (isAdmin) {
            sql.append("1 = 1 ");
        } else {
            sql.append("(is_public = true OR owner_username = ?) ");
            params.add(username);
        }

        sql.append("AND name LIKE ? ");
        params.add("%" + (name == null ? "" : name) + "%");

        sql.append("ORDER BY ").append(sortColumn).append(" ").append(sortDirection);

        return jdbcTemplate.query(sql.toString(), this::mapToEncounter, params.toArray());
    }
   
    /**
     * Maps a row in the ResultSet to an Encounter object.
     *
     * @param resultSet The result set to map.
     * @param rowNumber The row number.
     * @return Encounter The encounter object.
     * @throws SQLException If an error occurs while mapping the result set.
     */
    private Encounter mapToEncounter(ResultSet resultSet, int rowNumber) throws SQLException {
        return new Encounter(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getBoolean("is_public"),
                resultSet.getString("creator_username"),
                resultSet.getTimestamp("created_at")
        );
    }
}
