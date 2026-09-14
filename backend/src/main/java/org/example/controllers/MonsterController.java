package org.example.controllers;
 
import org.example.models.Monster;
import org.example.models.ResultsPage;
import org.example.security.AuthorizationHelper;
import org.example.services.MonsterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
 
import java.security.Principal;
 
/**
 * REST controller for monsters
 */
@RestController
@CrossOrigin
@RequestMapping("/api/monsters")
@PreAuthorize("isAuthenticated()")
public class MonsterController {
 
    @Autowired
    private MonsterService monsterService;
 
    /**
     * search through the monsters
     * @param name name of the monster
     * @param sortBy sorting criteria
     * @param direction sort direction
     * @param principal who is making the request
     * @param authentication authentication instance for the user
     * @return list of searched-for monsters
     */
    @GetMapping
    public ResultsPage<Monster> search(@RequestParam(defaultValue = "") String name,
                                   @RequestParam(defaultValue = "name") String sortBy,
                                   String type, @RequestParam(defaultValue = "asc") String direction,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   Principal principal, Authentication authentication) {
        boolean isAdmin = AuthorizationHelper.isAdmin(authentication);
        return monsterService.search(principal.getName(), isAdmin, name, sortBy, type, direction, page, size);
    }
 
    /**
     * get a monster by id
     * @param id id of the monster
     * @param principal who is making the request
     * @param authentication authentication instance for the user
     * @return the monster with that id
     */
    @GetMapping("/{id}")
    public Monster get(@PathVariable int id, Principal principal, Authentication authentication) {
        Monster monster = monsterService.getIsVisible(
                id, principal.getName(), AuthorizationHelper.isAdmin(authentication));
        if (monster == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Monster not found");
        }
        return monster;
    }
 
    /**
     * create a monster
     * @param monster the monster to be created
     * @param principal who is making the request
     * @return the created monster
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Monster create(@RequestBody Monster monster, Principal principal) {
        return monsterService.create(monster, principal.getName());
    }
 
    /**
     * update a monster
     * @param id the id of the monster
     * @param monster the monster to be updated
     * @param principal who is making the request
     * @param authentication the authentication instance for the user
     * @return the updated monster
     */
    @PutMapping("/{id}")
    public Monster update(@PathVariable int id, @RequestBody Monster monster,
                             Principal principal, Authentication authentication) {
        return monsterService.update(id, monster, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }
 
    /**
     * delete a monster
     * @param id id of the monster
     * @param principal who is making the request
     * @param authentication the authentication instance for the user
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id, Principal principal, Authentication authentication) {
        monsterService.delete(id, principal.getName(), AuthorizationHelper.isAdmin(authentication));
    }
}
 


