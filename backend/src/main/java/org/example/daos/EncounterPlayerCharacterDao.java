package org.example.daos;

import org.example.exceptions.DaoException;
import org.example.models.EncounterPlayerCharacter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Data access object for player characters in an encounter
 */
@Component
public class EncounterPlayerCharacterDao {

    private final JdbcTemplate jdbcTemplate;

    public EncounterPlayerCharacterDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    /**
     * Adds a PC to an encounter
     * @param encounterId the id of the encounter
     * @param playerCharacterId the id of the character
     */
    public EncounterPlayerCharacter addCharacterToEncounter(int encounterId, int playerCharacterId) {
        String sql = "INSERT INTO encounter_player_character (encounter_id, player_character_id, current_health) " +
                     "SELECT ?, id, health FROM player_character WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, encounterId, playerCharacterId);
        if (rowsAffected == 0) {
            throw new DaoException("Player character not found.");
        }
        int newId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        return getById(newId);
    }

    /**
     * get all characters in an encounter
     * @param encounterId the id of the encounter
     */
    public List<EncounterPlayerCharacter> getByEncounterId(int encounterId) {
        String sql = "SELECT epc.id, epc.encounter_id, epc.player_character_id, epc.current_health, " +
                     "pc.name AS character_name, pc.armor_class, pc.health AS max_health " +
                     "FROM encounter_player_character epc " +
                     "JOIN player_character pc ON epc.player_character_id = pc.id " +
                     "WHERE epc.encounter_id = ?";
        return jdbcTemplate.query(sql, this::mapToEncounterPC, encounterId);
    }

    /**
     * get the current character in the encounter by ID
     * @param id the id of the character
     */
    public EncounterPlayerCharacter getById(int id) {
        String sql = "SELECT epc.id, epc.encounter_id, epc.player_character_id, epc.current_health, " +
                     "pc.name AS character_name, pc.armor_class, pc.health AS max_health " +
                     "FROM encounter_player_character epc " +
                     "JOIN player_character pc ON epc.player_character_id = pc.id " +
                     "WHERE epc.id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapToEncounterPC, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


    /**
     * Updates current health of the character
     * @param id the id of the current character
     * @param newHealth updated health for the character
     */
    public EncounterPlayerCharacter updateHealth(int id, int newHealth) {
        EncounterPlayerCharacter participant = getById(id);
        if (participant == null) {
            throw new DaoException("Encounter participant not found.");
        }
        int clamped = Math.max(0, Math.min(newHealth, participant.getMaxHealth()));
        jdbcTemplate.update("UPDATE encounter_player_character SET current_health = ? WHERE id = ?", clamped, id);
        return getById(id);
    }

    /**
     * Removes a character from the encounter
     * @param id the id of the character to remove
     */
    public int remove(int id) {
        return jdbcTemplate.update("DELETE FROM encounter_player_character WHERE id = ?", id);
    }

    /**
     * Maps a row in the ResultSet to a Encounter Player Character object.
     *
     * @param resultSet The result set to map.
     * @param rowNumber The row number.
     * @return EncounterPlayerCharacter The object.
     * @throws SQLException If an error occurs while mapping the result set.
     */
    private EncounterPlayerCharacter mapToEncounterPC(ResultSet resultSet, int rowNumber) throws SQLException {
        return new EncounterPlayerCharacter(
                resultSet.getInt("id"),
                resultSet.getInt("encounter_id"),
                resultSet.getInt("player_character_id"),
                resultSet.getString("character_name"),
                resultSet.getInt("armor_class"),
                resultSet.getInt("max_health"),
                resultSet.getInt("current_health")
        );
    }
}
