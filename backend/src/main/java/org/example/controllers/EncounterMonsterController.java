package org.example.controllers;

import org.example.models.EncounterMonster;
import org.example.security.AuthorizationHelper;
import org.example.services.EncounterMonsterService;
import org.springframework.beans.factory.annotation.Autowired;
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
     * @return the list of monsters in the encounter
     */
    @GetMapping
    public List<EncounterMonster> getMonsterList(@PathVariable int encounterId) {
        return encounterMonsterService.getMonsterList(encounterId);
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
    public void remove(@PathVariable int encounterId, @PathVariable int monsterId,
                        Principal principal, Authentication authentication) {
        encounterMonsterService.removeMonster(
                encounterId, monsterId, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }
}
