package org.example.controllers;

import org.example.models.PlayerCharacter;
import org.example.security.AuthorizationHelper;
import org.example.services.PlayerCharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

/**
 * REST controller for characters
 */
@RestController
@CrossOrigin
@RequestMapping("/api/characters")
@PreAuthorize("isAuthenticated()")
public class PlayerCharacterController {

    @Autowired
    private PlayerCharacterService playerCharacterService;

    /**
     * search through the characters
     * @param name name of the character
     * @param sortBy sorting criteria
     * @param direction sort direction
     * @param principal who is making the request
     * @param authentication authentication instance for the user
     * @return list of searched-for characters
     */
    @GetMapping
    public List<PlayerCharacter> search(@RequestParam(defaultValue = "") String name,
                                   @RequestParam(defaultValue = "name") String sortBy,
                                   String characterClass, @RequestParam(defaultValue = "asc") String direction,
                                   Principal principal, Authentication authentication) {
        boolean isAdmin = AuthorizationHelper.isAdmin(authentication);
        return playerCharacterService.search(principal.getName(), isAdmin, name, sortBy, characterClass, direction);
    }

    /**
     * get an character by ID
     * @param id id of the character
     * @param principal who is making the request
     * @param authentication authentication instance for the user
     * @return the character with that id
     */
    @GetMapping("/{id}")
    public PlayerCharacter get(@PathVariable int id, Principal principal, Authentication authentication) {
        PlayerCharacter playerCharacter = playerCharacterService.getIsVisible(
                id, principal.getName(), AuthorizationHelper.isAdmin(authentication));
        if (playerCharacter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PlayerCharacter not found");
        }
        return playerCharacter;
    }

    /**
     * create an character
     * @param character the character to be created
     * @param principal who is making the request
     * @return the created character
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public PlayerCharacter create(@RequestBody PlayerCharacter playerCharacter, Principal principal) {
        return playerCharacterService.create(playerCharacter, principal.getName());
    }

    /**
     * update an character
     * @param id the id of the character
     * @param playerCharacter the character to be updated
     * @param principal who is making the request
     * @param authentication the authentication instance for the user
     * @return the updated playerCharacter
     */
    @PutMapping("/{id}")
    public PlayerCharacter update(@PathVariable int id, @RequestBody PlayerCharacter playerCharacter,
                             Principal principal, Authentication authentication) {
        return playerCharacterService.update(id, playerCharacter, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }

    /**
     * delete an character
     * @param id id of the character
     * @param principal who is making the request
     * @param authentication the authentication instance for the user
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id, Principal principal, Authentication authentication) {
        playerCharacterService.delete(id, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }
}
