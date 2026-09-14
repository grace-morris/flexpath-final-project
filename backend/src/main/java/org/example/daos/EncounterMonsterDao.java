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
     * add a monster to an encounter
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
                "em.initiative, em.used_reaction, em.legendary_actions_used, " +
                "m.name AS monster_name, m.armor_class, m.health AS max_health, " +
                "m.legendary_actions AS max_legendary_actions " +
                "FROM encounter_monster em " +
                "JOIN monster m ON em.monster_id = m.id " +
                "WHERE em.encounter_id = ?";
        return jdbcTemplate.query(sql, this::mapToEncounterMonster, encounterId);
    }

    /**
     * gets the specific monster in the encounter by id
     * @param id the id of the current monster
     */
    public EncounterMonster getById(int id) {
        String sql = "SELECT em.id, em.encounter_id, em.monster_id, em.current_health, " +
                "em.initiative, em.used_reaction, em.legendary_actions_used, " +
                "m.name AS monster_name, m.armor_class, m.health AS max_health, " +
                "m.legendary_actions AS max_legendary_actions " +
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
     * updates current health of the monster
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
     * updates the initiative
     * @param id the id of the monster
     * @param initiative the new initiative value
     */
    public EncounterMonster updateInitiative(int id, int initiative) {
        int rowsAffected = jdbcTemplate.update("UPDATE encounter_monster SET initiative = ? WHERE id = ?", initiative, id);
        if (rowsAffected == 0) {
            throw new DaoException("Encounter monster not found.");
        }
        return getById(id);
    }

    /**
     * sets whether the monster has used its reaction
     * @param id the id of the current monster
     * @param usedReaction whether the reaction has been used
     */
    public EncounterMonster setUsedReaction(int id, boolean usedReaction) {
        int rowsAffected = jdbcTemplate.update("UPDATE encounter_monster SET used_reaction = ? WHERE id = ?", usedReaction, id);
        if (rowsAffected == 0) {
            throw new DaoException("Encounter monster not found.");
        }
        return getById(id);
    }

    /**
     * spends one of the monster's legendary actions
     * @param id the id of the current monster in the encounter
     * @returns monster with updated legendary actions
     */
    public EncounterMonster useLegendaryAction(int id) {
        EncounterMonster current = getById(id);
        if (current == null) {
            throw new DaoException("Encounter monster not found.");
        }
        int updated = Math.max(0, Math.min(current.getLegendaryActionsUsed() + 1, current.getMaxLegendaryActions()));
        jdbcTemplate.update("UPDATE encounter_monster SET legendary_actions_used = ? WHERE id = ?", updated, id);
        return getById(id);
    }

    /**
     * removes a monster from the encounter
     * @param id the id of the monster to remove
     */
    public int remove(int id) {
        return jdbcTemplate.update("DELETE FROM encounter_monster WHERE id = ?", id);
    }

    /**
     * resets every monster's resources on round change
     * @param encounterId the id of the encounter
     */
    public void resetRoundState(int encounterId) {
        jdbcTemplate.update(
                "UPDATE encounter_monster SET used_reaction = false, legendary_actions_used = 0 WHERE encounter_id = ?",
                encounterId);
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
                resultSet.getInt("current_health"),
                resultSet.getInt("initiative"),
                resultSet.getBoolean("used_reaction"),
                resultSet.getInt("legendary_actions_used"),
                resultSet.getInt("max_legendary_actions")
        );
    }
}