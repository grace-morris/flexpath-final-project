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
    private int initiative;
    private boolean usedReaction;
    private int legendaryActionsUsed;
    private int maxLegendaryActions;

    /**
     * Creates a new monster in the encounter.
     *
     * @param id The id of the current monster in the encounter.
     * @param encounterId The id of the current encounter.
     * @param monsterId The id of the monster type
     * @param monsterName the name of the monster
     * @param armorClass the AC of the monster
     * @param maxHealth the max health for the monster
     * @param currentHealth the current health for the monster
     * @param initiative the turn order for this monster in this encounter
     * @param usedReaction whether this monster has used its reaction this round
     * @param legendaryActionsUsed how many legendary actions this monster has used this round
     * @param maxLegendaryActions how many legendary actions this monster type gets per round
     */
    public EncounterMonster(int id, int encounterId, int monsterId,
                            String monsterName, int armorClass, int maxHealth, int currentHealth,
                            int initiative, boolean usedReaction, int legendaryActionsUsed, int maxLegendaryActions) {
        this.id = id;
        this.encounterId = encounterId;
        this.monsterId = monsterId;
        this.monsterName = monsterName;
        this.armorClass = armorClass;
        this.maxHealth = maxHealth;
        this.currentHealth = currentHealth;
        this.initiative = initiative;
        this.usedReaction = usedReaction;
        this.legendaryActionsUsed = legendaryActionsUsed;
        this.maxLegendaryActions = maxLegendaryActions;
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
     * Sets the id of current monster
     * @param id id of the current monster
     */
    public void setId(int id) {
        this.id = id;
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
     * Sets the id of the encounter
     * @param id id of the encounter
     */
    public void setEncounterId(int id) {
        this.encounterId = id;
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
     * Sets the id of the monster
     * @param id id of the monster
     */
    public void setMonsterId(int id) {
        this.monsterId = id;
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
     * Sets the name of the monster.
     *
     * @param monsterName the name of the monster.
     */
    public void setMonsterName(String monsterName) {
        this.monsterName = monsterName;
    }

    /**
     * Gets the AC of the current monster.
     *
     * @return The AC of the current monster.
     */
    public int getArmorClass() {
        return armorClass;
    }

    /**
     * Sets the AC of the monster
     * @param armorClass
     */
    public void setArmorClass(int armorClass) {
        this.armorClass = armorClass;
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
     * Sets the max health for the monster
     * @param maxHealth max health of the monster
     */
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
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
     * Gets the initiative of the monster
     *
     * @return the initiative value; higher goes first
     */
    public int getInitiative() {
        return initiative;
    }

    /**
     * Sets the initiative of the monster
     *
     * @param initiative the initiative value
     */
    public void setInitiative(int initiative) {
        this.initiative = initiative;
    }

    /**
     * Checks if monster used it's reaction
     *
     * @return whether the reaction has been used
     */
    public boolean isUsedReaction() {
        return usedReaction;
    }

    /**
     * Toggles the monster's reaction
     *
     * @param usedReaction whether the reaction has been used
     */
    public void setUsedReaction(boolean usedReaction) {
        this.usedReaction = usedReaction;
    }

    /**
     * Gets remaining legendary action count
     *
     * @return the number of legendary actions used
     */
    public int getLegendaryActionsUsed() {
        return legendaryActionsUsed;
    }

    /**
     * Sets legendary action count
     *
     * @param legendaryActionsUsed the number of legendary actions used
     */
    public void setLegendaryActionsUsed(int legendaryActionsUsed) {
        this.legendaryActionsUsed = legendaryActionsUsed;
    }

    /**
     * Gets max legendary action amount
     *
     * @return the max legendary actions per round, from the monster type
     */
    public int getMaxLegendaryActions() {
        return maxLegendaryActions;
    }

    /**
     * Sets legendary action amount
     *
     * @param maxLegendaryActions the max legendary actions per round
     */
    public void setMaxLegendaryActions(int maxLegendaryActions) {
        this.maxLegendaryActions = maxLegendaryActions;
    }

}