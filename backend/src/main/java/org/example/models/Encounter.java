package org.example.models;
import java.sql.Timestamp;
 
/**
 * Model class for a monster in an encounter.
 */
public class Encounter {
    
    private int id;
    private String name;
    private String description;
    private boolean isPublic;
    private String creatorUsername;
    private Timestamp createdAt;
 
    /**
     * Creates a new monster in the encounter.
     *
     * @param id The id of the encounter.
     * @param name the name of the encounter.
     * @param description the description of the encounter
     * @param creatorUsername the usernmae of the creator
     * @param createdAt the time the encounter was created.
     */
    public Encounter(int id, String name, String description, boolean isPublic,
    String creatorUsername, Timestamp createdAt) 
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.creatorUsername = creatorUsername;
        this.createdAt = createdAt;
    }
 
    /**
     * Gets the id of the current encounter.
     *
     * @return The id of the current encounter.
     */
    public int getId() {
        return id;
    }
 
 
    /**
     * Sets the id of the encounter
     *
     * @param id the id of the encounter
     */
    public void setId(int id) {
        this.id = id;
    }
 
    /**
     * Gets the name of the encounter.
     *
     * @return The name of the encounter.
     */
    public String getName() {
        return name;
    }
 
 
    /**
     * Sets the name of the encounter.
     *
     * @param name the name of the encounter.
     */
    public void setName(String name) {
        this.name = name;
    }
 
    /**
     * Gets the description of the encounter.
     *
     * @return The description of the encounter.
     */
    public String getDescription() {
        return description;
    }
 
 
    /**
     * Sets the description of the encounter.
     *
     * @param description the description of the encounter.
     */
    public void setDescription(String description) {
        this.description = description;
    }
 
    /**
     * Gets whether the encounter is public
     *
     * @return whether the encounter is public
     */
    public boolean isPublic() {
        return isPublic;
    }
 
 
    /**
     * Sets whether the encounter is public
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
     * Gets the time the encounter was created.
     *
     * @return The time the encounter was created.
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
 


