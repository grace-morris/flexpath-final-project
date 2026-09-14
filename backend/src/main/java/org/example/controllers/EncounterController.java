package org.example.controllers;

import org.example.models.Encounter;
import org.example.models.ResultsPage;
import org.example.security.AuthorizationHelper;
import org.example.services.EncounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

/**
 * REST controller for encounters
 */
@RestController
@CrossOrigin
@RequestMapping("/api/encounters")
@PreAuthorize("isAuthenticated()")
public class EncounterController {

    @Autowired
    private EncounterService encounterService;

    /**
     * search through the encounters
     * @param name name of the encounter
     * @param sortBy sorting criteria
     * @param direction sort direction
     * @param principal who is making the request
     * @param authentication authentication instance for the user
     * @return list of searched-for encounters
     */
    @GetMapping
    public ResultsPage<Encounter> search(@RequestParam(defaultValue = "") String name,
                                   @RequestParam(defaultValue = "name") String sortBy, String visibility,
                                   @RequestParam(defaultValue = "asc") String direction, int page, int size,
                                   Principal principal, Authentication authentication) {
        boolean isAdmin = AuthorizationHelper.isAdmin(authentication);
        return encounterService.search(principal.getName(), isAdmin, name, visibility, sortBy, direction, page, size);
    }

    /**
     * get an encounter by ID
     * @param id id of the encounter
     * @param principal who is making the request
     * @param authentication authentication instance for the user
     * @return the encounter with that id
     */
    @GetMapping("/{id}")
    public Encounter get(@PathVariable int id, Principal principal, Authentication authentication) {
        Encounter encounter = encounterService.getIsVisible(
                id, principal.getName(), AuthorizationHelper.isAdmin(authentication));
        if (encounter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Encounter not found");
        }
        return encounter;
    }

    /**
     * create an encounter
     * @param encounter the encounter to be created
     * @param principal who is making the request
     * @return the created encounter
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Encounter create(@RequestBody Encounter encounter, Principal principal) {
        return encounterService.create(encounter, principal.getName());
    }

    /**
     * update an encounter
     * @param id the id of the encounter
     * @param encounter the encounter to be updated
     * @param principal who is making the request
     * @param authentication the authentication instance for the user
     * @return the updated encounter
     */
    @PutMapping("/{id}")
    public Encounter update(@PathVariable int id, @RequestBody Encounter encounter,
                             Principal principal, Authentication authentication) {
        return encounterService.update(id, encounter, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }

    /**
     * delete an encounter
     * @param id id of the encounter
     * @param principal who is making the request
     * @param authentication the authentication instance for the user
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id, Principal principal, Authentication authentication) {
        encounterService.delete(id, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }
}
