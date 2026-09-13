package org.example.models;

/**
 * Model class for a monster in an encounter.
 */
public class EncounterMonster {
    /**
     * The id, the foreign id's, the name, AC, and health of the monster.
     */
    private int id;
    private int encounterId;
    private int monsterId;
    private String monsterName;
    private int armorClass;
    private int maxHealth;
    private int currentHealth;

    /**
     * Creates a new monster in the encounter.
     *
     * @param id The id of the current monster in the encounter.
     * @param encounterId The id of the current encounter.
     * @param monsterId The id of the monster type
     * @param monsterName the name of the monster
     * @param armorClass the AC of the monster
     * @param maxHealth the maximum hit points for this monster
     * @param currentHealth the current hit points for the monster
     */
    public EncounterMonster(int id, int encounterId, int monsterId, 
    String monsterName, int armorClass, int maxHealth, int currentHealth) {
        this.id = id;
        this.encounterId = encounterId;
        this.monsterId = monsterId;
        this.monsterName = monsterName;
        this.armorClass = armorClass;
        this.maxHealth = maxHealth;
        this.currentHealth = currentHealth;
    }

    /**
     * Gets the id of the current monster.
     *
     * @return The id of the current monster.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the id of the encounter.
     *
     * @return The id of the encounter.
     */
    public int getEncounterId() {
        return encounterId;
    }

    /**
     * Gets the id of the monster type.
     *
     * @return The id of the monster type.
     */
    public int getMonsterId() {
        return monsterId;
    }

    /**
     * Gets the name of the monster.
     *
     * @return The name of the monster.
     */
    public String getMonsterName() {
        return monsterName;
    }

    /**
     * Gets the armor class of the current monster.
     *
     * @return The AC of the current monster.
     */
    public int getArmorClass() {
        return armorClass;
    }

    /**
     * Gets the max health of the monster.
     *
     * @return The max health of the monster.
     */
    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * Gets the current health of the current monster.
     *
     * @return The current health of the current monster.
     */
    public int getCurrentHealth() {
        return currentHealth;
    }

    /**
     * Sets the current health of the monster.
     *
     * @param currentHealth the current health of the monster.
     */
    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    /**
     * Sets the name of the monster.
     *
     * @param monsterName the name of the monster.
     */
    public void setMonsterName(String monsterName) {
        this.monsterName = monsterName;
    }

}
