package org.example.controllers;
 
import org.example.models.EncounterPlayerCharacter;
import org.example.security.AuthorizationHelper;
import org.example.services.EncounterPlayerCharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
 
import java.security.Principal;
import java.util.List;
import java.util.Map;
 
/**
 * Controller for the characters in an encounter, mostly to update health
 */
@RestController
@CrossOrigin
@RequestMapping("/api/encounters/{encounterId}/characters")
@PreAuthorize("isAuthenticated()")
public class EncounterPlayerCharacterController {
 
    @Autowired
    private EncounterPlayerCharacterService encounterPlayerCharacterService;
 
    /**
     * gets a list of the characters in the encounter
     * @param encounterId the id of the encounter
     * @param principal who is making the request
     * @param authentication authentication instance for the user
     * @return the list of characters in the encounter
     */
    @GetMapping
    public List<EncounterPlayerCharacter> getPlayerCharacterList(@PathVariable int encounterId,
                                                                  Principal principal, Authentication authentication) {
        return encounterPlayerCharacterService.getPlayerCharacterList(
                encounterId, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }
 
    /**
     * adds a character to the encounter
     * @param encounterId the id of the encounter
     * @param characterId the id of the character
     * @param principal who is making the request
     * @param authentication instance to authenticate user
     * @return
     */
    @PostMapping("/{characterId}")
    public EncounterPlayerCharacter add(@PathVariable int encounterId, @PathVariable int characterId,
                                        Principal principal, Authentication authentication) {
        return encounterPlayerCharacterService.addPlayerCharacter(
                encounterId, characterId, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }
 
    /**
     * changes the character's health
     * @param encounterId the id of the encounter
     * @param characterId the id of the character
     * @param body maps "currentHealth" to the current health from the request body
     * @param principal who is making the request
     * @param authentication authentication instance for the user
     */
    @PatchMapping("/{characterId}/health")
    public EncounterPlayerCharacter updateHealth(@PathVariable int encounterId, @PathVariable int characterId,
                                                 @RequestBody Map<String, Integer> body,
                                                 Principal principal, Authentication authentication) {
        int newHealth = body.get("currentHealth");
        return encounterPlayerCharacterService.updateHealth(
                encounterId, characterId, newHealth, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }
 
    @DeleteMapping("/{characterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable int encounterId, @PathVariable int characterId,
                       Principal principal, Authentication authentication) {
        encounterPlayerCharacterService.removePlayerCharacter(
                encounterId, characterId, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }
 
    /**
     * sets initiative order
     * @param encounterId the id of the encounter
     * @param characterId the id of the character
     * @param body maps "initiative" to the new initiative value
     * @param principal who is making the request
     * @param authentication authentication instance for the user
     */
    @PatchMapping("/{characterId}/initiative")
    public EncounterPlayerCharacter updateInitiative(@PathVariable int encounterId, @PathVariable int characterId,
                                                     @RequestBody Map<String, Integer> body,
                                                     Principal principal, Authentication authentication) {
        int initiative = body.get("initiative");
        return encounterPlayerCharacterService.updateInitiative(
                encounterId, characterId, initiative, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }
 
    /**
     * toggle for reaction
     * @param encounterId the id of the encounter
     * @param characterId the id of the character
     * @param body maps "usedReaction" to whether the toggle
     * @param principal who is making the request
     * @param authentication authentication instance for the user
     */
    @PatchMapping("/{characterId}/reaction")
    public EncounterPlayerCharacter setUsedReaction(@PathVariable int encounterId, @PathVariable int characterId,
                                                    @RequestBody Map<String, Boolean> body,
                                                    Principal principal, Authentication authentication) {
        boolean usedReaction = body.get("usedReaction");
        return encounterPlayerCharacterService.setUsedReaction(
                encounterId, characterId, usedReaction, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }
}