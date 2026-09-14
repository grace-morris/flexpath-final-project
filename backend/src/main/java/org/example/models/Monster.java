package org.example.models;
import java.sql.Timestamp;

/**
 * Model class for a monster
 */
public class Monster {

    private int id;
    private String name;
    private String monsterType;
    private int health;
    private double challengeRating;
    private int armorClass;
    private String description;
    private boolean isPublic;
    private String creatorUsername;
    private Timestamp createdAt;
    private int legendaryActions;

    /**
     * Creates a new monster.
     *
     * @param id The id of the monster.
     * @param name the name of the monster.
     * @param monsterType the type of monster
     * @param health the max health of the monster
     * @param challengeRating the level of the monster
     * @param armorClass the armor class of the monster
     * @param description the description of the monster
     * @param creatorUsername the usernmae of the creator
     * @param createdAt the time the monster was created.
     * @param legendaryActions how many legendary actions this monster gets per round
     */
    public Monster(int id, String name, String monsterType, int health, double challengeRating,
                   int armorClass, String description, boolean isPublic, String creatorUsername, Timestamp createdAt,
                   int legendaryActions)
    {
        this.id = id;
        this.name = name;
        this.monsterType = monsterType;
        this.health = health;
        this.challengeRating = challengeRating;
        this.armorClass = armorClass;
        this.description = description;
        this.isPublic = isPublic;
        this.creatorUsername = creatorUsername;
        this.createdAt = createdAt;
        this.legendaryActions = legendaryActions;
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
     * Sets the id of the monster.
     *
     * @param id the id of the monster.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the name of the monster.
     *
     * @return The name of the monster.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the monster.
     *
     * @param name the name of the monster.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the type of the monster.
     *
     * @return The type of the monster.
     */
    public String getMonsterType() {
        return monsterType;
    }

    /**
     * Sets the type of the monster.
     *
     * @param monsterType the type of the monster.
     */
    public void setMonsterType(String monsterType) {
        this.monsterType = monsterType;
    }

    /**
     * Gets the level of the current monster.
     *
     * @return The level of the current monster.
     */
    public double getChallengeRating() {
        return challengeRating;
    }

    /**
     * Sets the level of the monster.
     *
     * @param challengeRating the level of the monster.
     */
    public void setChallengeRating(double challengeRating) {
        this.challengeRating = challengeRating;
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
     * Sets the armor class of the monster.
     *
     * @param armorClass the AC of the monster.
     */
    public void setArmorClass(int armorClass) {
        this.armorClass = armorClass;
    }

    /**
     * Gets the max health of the monster.
     *
     * @return The max health of the monster.
     */
    public int getHealth() {
        return health;
    }

    /**
     * Sets the health of the monster.
     *
     * @param health the health of the monster.
     */
    public void setHealth(int health) {
        this.health = health;
    }

    /**
     * Gets the description of the monster.
     *
     * @return The description of the monster.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the monster
     *
     * @param description the description of the monster
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets whether the monster is public
     *
     * @return whether the monster is public
     */
    public boolean isPublic() {
        return isPublic;
    }


    /**
     * Sets whether the monster is public
     *
     * @param isPublic the public status
     */
    public void setPublic(boolean isPublic) {
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
     * Gets the time the monster was created.
     *
     * @return The time the monster was created.
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

    /**
     * Gets how many legendary actions this monster gets per round.
     *
     * @return the number of legendary actions per round (0 for most monsters)
     */
    public int getLegendaryActions() {
        return legendaryActions;
    }

    /**
     * Sets how many legendary actions this monster gets per round.
     *
     * @param legendaryActions the number of legendary actions per round
     */
    public void setLegendaryActions(int legendaryActions) {
        this.legendaryActions = legendaryActions;
    }

}