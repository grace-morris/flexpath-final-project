package org.example.daos;

import org.example.exceptions.DaoException;
import org.example.models.EncounterMonster;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Data access object for monsters in an encounter.
 */
@Component
public class EncounterMonsterDao {

    private final JdbcTemplate jdbcTemplate;

    public EncounterMonsterDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Adds a monster to an encounter
     * @param encounterId the id of the encounter
     * @param monsterId the id of the monster
     */
    public EncounterMonster addMonsterToEncounter(int encounterId, int monsterId) {
        String sql = "INSERT INTO encounter_monster (encounter_id, monster_id, current_health) " +
                     "SELECT ?, id, health FROM monster WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, encounterId, monsterId);
        if (rowsAffected == 0) {
            throw new DaoException("Monster not found.");
        }
        int newId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        return getById(newId);
    }

    /**
     * get all monsters in an encounter
     * @param encounterId the id of the encounter
     */
    public List<EncounterMonster> getByEncounterId(int encounterId) {
        String sql = "SELECT em.id, em.encounter_id, em.monster_id, em.current_health, " +
                     "m.name AS monster_name, m.armor_class, m.health AS max_health " +
                     "FROM encounter_monster em " +
                     "JOIN monster m ON em.monster_id = m.id " +
                     "WHERE em.encounter_id = ?";
        return jdbcTemplate.query(sql, this::mapToEncounterMonster, encounterId);
    }

    /**
     * gets the specific monster in the encounter by ID
     * @param id the id of the current monster in the encounter
     */
    public EncounterMonster getById(int id) {
        String sql = "SELECT em.id, em.encounter_id, em.monster_id, em.current_health, " +
                     "m.name AS monster_name, m.armor_class, m.health AS max_health " +
                     "FROM encounter_monster em " +
                     "JOIN monster m ON em.monster_id = m.id " +
                     "WHERE em.id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapToEncounterMonster, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Updates current health of the monster
     * @param id the id of the current monster
     * @param newHealth updated health for the monster
     */
    public EncounterMonster updateHealth(int id, int newHealth) {
        EncounterMonster currentMonster = getById(id);
        if (currentMonster == null) {
            throw new DaoException("Encounter monster not found.");
        }
        int clamped = Math.max(0, Math.min(newHealth, currentMonster.getMaxHealth()));
        jdbcTemplate.update("UPDATE encounter_monster SET current_health = ? WHERE id = ?", clamped, id);
        return getById(id);
    }

    /**
     * Removes a monster from the encounter
     * @param id the id of the monster to remove
     */
    public int remove(int id) {
        return jdbcTemplate.update("DELETE FROM encounter_monster WHERE id = ?", id);
    }

    /**
     * Maps a row in the ResultSet to an Encounter Monster object.
     *
     * @param resultSet The result set to map.
     * @param rowNumber The row number.
     * @return EncounterMonster The EncounterMonster object.
     * @throws SQLException If an error occurs while mapping the result set.
     */
    private EncounterMonster mapToEncounterMonster(ResultSet resultSet, int rowNumber) throws SQLException {
        return new EncounterMonster(
                resultSet.getInt("id"),
                resultSet.getInt("encounter_id"),
                resultSet.getInt("monster_id"),
                resultSet.getString("monster_name"),
                resultSet.getInt("armor_class"),
                resultSet.getInt("max_health"),
                resultSet.getInt("current_health")
        );
    }
}

