package org.example.daos;

import org.example.exceptions.DaoException;
import org.example.models.Encounter;
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
 * Data access object for encounters.
 */
@Component
public class EncounterDao {
    
    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a new user datasource object
     *
     * @param dataSource The data source for the DAO.
     */
    public EncounterDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Gets all the user's encounters
     * @param username the user to get the encounters
     * @return List of Encounters
     */
    public List<Encounter> getUsersEncounters(String username) {
        try {
            return jdbcTemplate.query("SELECT * FROM encounter WHERE creator_username = ?", this::mapToEncounter, username);
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
     * Gets encounter by id
     *
     * @return id of the encounter
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
     * Advances an encounter to the next round.
     * @param id the id of the encounter
     * @return the encounter with its round incremented
     */
    public Encounter incrementRound(int id) {
        int rowsAffected = jdbcTemplate.update("UPDATE encounter SET current_round = current_round + 1 WHERE id = ?", id);
        if (rowsAffected == 0) {
            throw new DaoException("Encounter not found.");
        }
        return getEncounterById(id);
    }

    /**
     * Search for public or user's encounters with a given name, optionally narrowed to
     * only public encounters or only the user's own encounters, and paginated so a large
     * result set doesn't have to be loaded (and rendered) all at once.
     *
     * @param username the requesting user
     * @param isAdmin whether the requesting user is admin
     * @param name name of the encounter
     * @param visibility public or private
     * @param sortBy sorting criteria
     * @param direction sort direction
     * @param page page number
     * @param size how many results per page
     * @return a page of matching encounters, total matches
     */
    public ResultsPage<Encounter> search(String username, boolean isAdmin, String name, String visibility,
                                         String sortBy, String direction, int page, int size) {
        String sortColumn;
        if (sortBy.equals("created_at")) {
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

        if ("public".equalsIgnoreCase(visibility)) {
            where.append("is_public = true ");
        } else if ("mine".equalsIgnoreCase(visibility)) {
            where.append("creator_username = ? ");
            params.add(username);
        } else if (isAdmin) {
            where.append("1 = 1 ");
        } else {
            where.append("(is_public = true OR creator_username = ?) ");
            params.add(username);
        }

        where.append("AND name LIKE ? ");
        params.add("%" + (name == null ? "" : name) + "%");

        int safeSize = size < 1 ? 10 : Math.min(size, 100);
        int safePage = Math.max(page, 0);

        String countSql = "SELECT COUNT(*) FROM encounter " + where;
        Integer totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params.toArray());

        String sql = "SELECT * FROM encounter " + where
                + "ORDER BY " + sortColumn + " " + sortDirection + " LIMIT ? OFFSET ?";
        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(safeSize);
        pageParams.add(safePage * safeSize);

        List<Encounter> items = jdbcTemplate.query(sql, this::mapToEncounter, pageParams.toArray());

        return new ResultsPage<>(items, totalCount == null ? 0 : totalCount);
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
                resultSet.getTimestamp("created_at"),
                resultSet.getInt("current_round")
        );
    }
}