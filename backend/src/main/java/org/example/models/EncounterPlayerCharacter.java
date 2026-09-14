package org.example.models;

/**
 * Model class for a character in an encounter.
 */
public class EncounterPlayerCharacter {
    /**
     * The id, the foreign id's, the name, AC, and health of the character.
     */
    private int id;
    private int encounterId;
    private int playerCharacterId;
    private String playerCharacterName;
    private int armorClass;
    private int maxHealth;
    private int currentHealth;
    private int initiative;
    private boolean usedReaction;

    /**
     * Creates a new character in the encounter.
     *
     * @param id The id of the current character in the encounter.
     * @param encounterId The id of the current encounter.
     * @param playerCharacterId The id of the character
     * @param playerCharacterName the name of the character
     * @param armorClass the AC of the character
     * @param maxHealth the max health for the character
     * @param currentHealth the current health of the character
     * @param initiative the turn order for this character in this encounter
     * @param usedReaction whether this character has used its reaction this round
     */
    public EncounterPlayerCharacter(int id, int encounterId, int playerCharacterId,
                                    String playerCharacterName, int armorClass, int maxHealth, int currentHealth,
                                    int initiative, boolean usedReaction) {
        this.id = id;
        this.encounterId = encounterId;
        this.playerCharacterId = playerCharacterId;
        this.playerCharacterName = playerCharacterName;
        this.armorClass = armorClass;
        this.maxHealth = maxHealth;
        this.currentHealth = currentHealth;
        this.initiative = initiative;
        this.usedReaction = usedReaction;
    }

    /**
     * Gets the id of the current character.
     *
     * @return The id of the current character.
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
     * Gets the id of the character type.
     *
     * @return The id of the character type.
     */
    public int getPlayerCharacterId() {
        return playerCharacterId;
    }

    /**
     * Gets the name of the character.
     *
     * @return The name of the character.
     */
    public String getPlayerCharacterName() {
        return playerCharacterName;
    }

    /**
     * Gets the armor class of the current character.
     *
     * @return The AC of the current character.
     */
    public int getArmorClass() {
        return armorClass;
    }

    /**
     * Gets the max health of the character.
     *
     * @return The max health of the character.
     */
    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * Gets the current health of the current character.
     *
     * @return The current health of the current character.
     */
    public int getCurrentHealth() {
        return currentHealth;
    }

    /**
     * Sets the current health of the character.
     *
     * @param currentHealth the current health of the character.
     */
    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    /**
     * Sets the name of the character.
     *
     * @param playerCharacterName the current health of the character.
     */
    public void setPlayerCharacterName(String playerCharacterName) {
        this.playerCharacterName = playerCharacterName;
    }

    /**
     * Gets the turn order for this character in this encounter.
     *
     * @return the initiative value; higher goes first
     */
    public int getInitiative() {
        return initiative;
    }

    /**
     * Sets the turn order for this character in this encounter.
     *
     * @param initiative the initiative value
     */
    public void setInitiative(int initiative) {
        this.initiative = initiative;
    }

    /**
     * Gets whether this character has used its reaction this round.
     *
     * @return whether the reaction has been used
     */
    public boolean isUsedReaction() {
        return usedReaction;
    }

    /**
     * Sets whether this character has used its reaction this round.
     *
     * @param usedReaction whether the reaction has been used
     */
    public void setUsedReaction(boolean usedReaction) {
        this.usedReaction = usedReaction;
    }

}