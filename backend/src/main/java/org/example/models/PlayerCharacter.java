package org.example.models;
import java.sql.Timestamp;

/**
 * Model class for a monster in an character.
 */
public class PlayerCharacter {
    
    private int id;
    private String name;
    private String characterClass;
    private int health;
    private int level;
    private int armorClass;
    private String description;
    private boolean isPublic;
    private String creatorUsername;
    private Timestamp createdAt;

    /**
     * Creates a new character.
     *
     * @param id The id of the character.
     * @param name the name of the character.
     * @param characterClass the class of the character
     * @param health the max health of the character
     * @param level the level of the character
     * @param armorClass the armor class of the character
     * @param description the description of the character
     * @param creatorUsername the usernmae of the creator
     * @param createdAt the time the character was created.
     */
    public PlayerCharacter(int id, String name, String characterClass, int health, int level,
        int armorClass, String description, boolean isPublic, String creatorUsername, Timestamp createdAt) 
    {
        this.id = id;
        this.name = name;
        this.characterClass = characterClass;
        this.health = health;
        this.level = level;
        this.armorClass = armorClass;
        this.description = description;
        this.isPublic = isPublic;
        this.creatorUsername = creatorUsername;
        this.createdAt = createdAt;
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
     * Sets the id of the character
     *
     * @param id the id of the character.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the name of the character.
     *
     * @return The name of the character.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the character.
     *
     * @param name the name of the character.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the class of the character.
     *
     * @return The class of the character.
     */
    public String getCharacterClass() {
        return characterClass;
    }

    /**
     * Sets the class of the character.
     *
     * @param characterClass the class of the character.
     */
    public void setCharacterClass(String characterClass) {
        this.characterClass = characterClass;
    }

    /**
     * Gets the level of the current character.
     *
     * @return The level of the current character.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Sets the level of the character.
     *
     * @param level the level of the character.
     */
    public void setLevel(int level) {
        this.level = level;
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
     * Sets the armor class of the character.
     *
     * @param armorClass the AC of the character.
     */
    public void setArmorClass(int armorClass) {
        this.armorClass = armorClass;
    }

    /**
     * Gets the max health of the character.
     *
     * @return The max health of the character.
     */
    public int getHealth() {
        return health;
    }

    /**
     * Sets the health of the character.
     *
     * @param health the health of the character.
     */
    public void setHealth(int health) {
        this.health = health;
    }

    /**
     * Gets the description of the character.
     *
     * @return The description of the character.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the character.
     *
     * @param description the description of the character.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets whether the character is public
     *
     * @return whether the character is public
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * Sets whether the character is public
     *
     * @param isPublic the public status
     */
    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * Gets the username of the creator.
     *
     * @return The username of the creator
     */
    public String getCreatorUsername() {
        return creatorUsername;
    }

    /**
     * Sets the username of the creator,
     *
     * @param username the name of the creator.
     */
    public void setCreatorUsername(String username) {
        this.creatorUsername = username;
    }

    /**
     * Gets the time the character was created.
     *
     * @return The time the character was created.
     */
    public Timestamp getCreationTime() {
        return createdAt;
    }

    /**
     * Sets the creation time.
     *
     * @param createdAt the timestamp of creation
     */
    public void setCreationTime(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

}
