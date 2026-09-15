package org.example.controllers;
 
import org.example.models.EncounterMonster;
import org.example.security.AuthorizationHelper;
import org.example.services.EncounterMonsterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
 
import java.security.Principal;
import java.util.List;
import java.util.Map;
 
/**
 * Controller for the monsters in an encounter, mostly to update health
 */
@RestController
@CrossOrigin
@RequestMapping("/api/encounters/{encounterId}/monsters")
@PreAuthorize("isAuthenticated()")
public class EncounterMonsterController {
 
    @Autowired
    private EncounterMonsterService encounterMonsterService;
 
    /**
     * Gets a list of the monsters in the encounter
     * @param encounterId the id of the encounter
     * @param principal who is making the request
     * @param authentication authentication instance for the user
     * @return the list of monsters in the encounter
     */
    @GetMapping
    public List<EncounterMonster> getMonsterList(@PathVariable int encounterId,
                                                  Principal principal, Authentication authentication) {
        return encounterMonsterService.getMonsterList(
                encounterId, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }
 
    /**
     * adds a monster to the encounter
     * @param encounterId the id of the encounter
     * @param monsterId the id of the monster
     * @param principal who is making the request
     * @param authentication instance to authenticate user
     * @return
     */
    @PostMapping("/{monsterId}")
    public EncounterMonster add(@PathVariable int encounterId, @PathVariable int monsterId,
                                 Principal principal, Authentication authentication) {
        return encounterMonsterService.addMonster(
                encounterId, monsterId, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }
 
    /**
     * Adjusts the monster's health
     * @param encounterId the id of the encounter
     * @param monsterId the id of the monster
     * @param body maps "currentHealth" to the current health from the request body
     * @param principal who is making the request
     * @param authentication authentication instance for the user
     */
    @PatchMapping("/{monsterId}/health")
    public EncounterMonster updateHealth(@PathVariable int encounterId, @PathVariable int monsterId,
                                             @RequestBody Map<String, Integer> body,
                                             Principal principal, Authentication authentication) {
        int newHitPoints = body.get("currentHealth");
        return encounterMonsterService.updateHealth(
                encounterId, monsterId, newHitPoints, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }
 
    @DeleteMapping("/{monsterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable int encounterId, @PathVariable int monsterId,
                        Principal principal, Authentication authentication) {
        encounterMonsterService.removeMonster(
                encounterId, monsterId, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }
 
    /**
     * Sets the monster's initiative (turn order) for this encounter
     * @param encounterId the id of the encounter
     * @param monsterId the id of the monster
     * @param body maps "initiative" to the new initiative value
     * @param principal who is making the request
     * @param authentication authentication instance for the user
     */
    @PatchMapping("/{monsterId}/initiative")
    public EncounterMonster updateInitiative(@PathVariable int encounterId, @PathVariable int monsterId,
                                              @RequestBody Map<String, Integer> body,
                                              Principal principal, Authentication authentication) {
        int initiative = body.get("initiative");
        return encounterMonsterService.updateInitiative(
                encounterId, monsterId, initiative, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }
 
    /**
     * Sets whether the monster has used its reaction this round
     * @param encounterId the id of the encounter
     * @param monsterId the id of the monster
     * @param body maps "usedReaction" to whether the reaction has been used
     * @param principal who is making the request
     * @param authentication authentication instance for the user
     */
    @PatchMapping("/{monsterId}/reaction")
    public EncounterMonster setUsedReaction(@PathVariable int encounterId, @PathVariable int monsterId,
                                             @RequestBody Map<String, Boolean> body,
                                             Principal principal, Authentication authentication) {
        boolean usedReaction = body.get("usedReaction");
        return encounterMonsterService.setUsedReaction(
                encounterId, monsterId, usedReaction, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }
 
    /**
     * Spends one legendary action
     * @param encounterId the id of the encounter
     * @param monsterId the id of the monster
     * @param principal who is making the request
     * @param authentication authentication instance for the user
     */
    @PatchMapping("/{monsterId}/legendary-action")
    public EncounterMonster useLegendaryAction(@PathVariable int encounterId, @PathVariable int monsterId,
                                                Principal principal, Authentication authentication) {
        return encounterMonsterService.useLegendaryAction(
                encounterId, monsterId, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }
}