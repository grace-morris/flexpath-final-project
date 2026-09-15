package org.example.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.daos.MonsterDao;
import org.example.models.Monster;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller test for MonsterController - the one thing the Service and Dao
 * tests can't prove, because they call Java methods directly rather than
 * going over HTTP: does the security actually work at the HTTP boundary?
 * Is @PreAuthorize really enforced, does an AccessDeniedException really
 * turn into a 403 response, does @RequestBody/@ResponseStatus actually wire
 * up the way the annotations claim?
 *
 * @WithMockUser fakes a logged-in user (with or without the ADMIN
 * authority) without needing a real JWT - it works regardless of *how* the
 * app authenticates people in production, since it just populates the same
 * security context a real login would. AuthorizationHelper checks for the
 * exact authority "ADMIN" (no "ROLE_" prefix, unlike Spring's usual
 * convention), so admin tests use authorities = "ADMIN", not roles.
 *
 * Fixture data (and @WithMockUser usernames) are limited to "user" and
 * "admin" on purpose - those are the only two usernames create-database.sql
 * seeds, and monster.creator_username has a foreign key to users.username,
 * so anything else would fail with a constraint violation the moment a
 * fixture tried to insert it.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MonsterControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MonsterDao monsterDao;

    private Monster newMonster(String name, boolean isPublic, String creatorUsername) {
        Monster monster = new Monster(0, name, "Humanoid", 10, 1.0, 12,
                "A monster created by MonsterControllerTest.", isPublic, creatorUsername, null, 0);
        return monsterDao.create(monster);
    }

    @Nested
    class Authentication {

        @Test
        void anUnauthenticatedRequest_isRejectedWithoutReachingTheController() throws Exception {
            mockMvc.perform(get("/api/monsters"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class Get {

        @Test
        @WithMockUser(username = "user")
        void aPublicMonster_isVisibleToAnyLoggedInUser() throws Exception {
            Monster monster = newMonster("ZZTest Public Orc", true, "admin");

            mockMvc.perform(get("/api/monsters/" + monster.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("ZZTest Public Orc"));
        }

        @Test
        @WithMockUser(username = "user")
        void aMonsterThatDoesNotExist_returns404() throws Exception {
            mockMvc.perform(get("/api/monsters/999999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "user")
        void aPrivateMonsterOwnedBySomeoneElse_returns403() throws Exception {
            Monster monster = newMonster("ZZTest Someones Secret", false, "admin");

            mockMvc.perform(get("/api/monsters/" + monster.getId()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class Search {

        @Test
        @WithMockUser(username = "user")
        void returnsAPageOfResults_matchingTheGivenFilters() throws Exception {
            newMonster("ZZTest Search Goblin", true, "user");

            mockMvc.perform(get("/api/monsters")
                            .param("name", "ZZTest Search")
                            .param("visibility", "all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCount").value(1))
                    .andExpect(jsonPath("$.items[0].name").value("ZZTest Search Goblin"));
        }
    }

    @Nested
    class Create {

        @Test
        @WithMockUser(username = "user")
        void createsTheMonster_ignoringAnyCreatorUsernameInTheRequestBody() throws Exception {
            Map<String, Object> body = Map.of(
                    "name", "ZZTest New Monster",
                    "monsterType", "Beast",
                    "challengeRating", 1.0,
                    "armorClass", 12,
                    "health", 10,
                    "description", "",
                    "public", true,
                    "creatorUsername", "admin",
                    "legendaryActions", 0
            );

            mockMvc.perform(post("/api/monsters")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("ZZTest New Monster"))
                    .andExpect(jsonPath("$.creatorUsername").value("user"));
        }
    }

    @Nested
    class Update {

        @Test
        @WithMockUser(username = "user")
        void owner_canUpdateTheirOwnMonster() throws Exception {
            Monster monster = newMonster("ZZTest Before", false, "user");
            monster.setName("ZZTest After");
            monster.setHealth(999);

            mockMvc.perform(put("/api/monsters/" + monster.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(monster)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("ZZTest After"))
                    .andExpect(jsonPath("$.health").value(999));
        }

        @Test
        @WithMockUser(username = "user")
        void nonOwnerNonAdmin_cannotUpdateSomeoneElsesMonster() throws Exception {
            Monster monster = newMonster("ZZTest Not Yours", false, "admin");

            mockMvc.perform(put("/api/monsters/" + monster.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(monster)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "admin", authorities = "ADMIN")
        void anAdmin_canUpdateSomeoneElsesMonster() throws Exception {
            Monster monster = newMonster("ZZTest Admin Target", false, "user");
            monster.setName("ZZTest Admin Edited");

            mockMvc.perform(put("/api/monsters/" + monster.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(monster)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("ZZTest Admin Edited"));
        }

        @Test
        @WithMockUser(username = "user")
        void updatingAMonsterThatDoesNotExist_currentlyReturns500_notANice404() throws Exception {
            // Documenting current behavior, not endorsing it: MonsterService's
            // canModify() throws a plain DaoException (no @ResponseStatus, no
            // ResponseStatusException, and there's no @ControllerAdvice anywhere
            // in the app) when the id doesn't exist, so it falls through to
            // Spring Boot's default handling for an uncaught RuntimeException,
            // which is a generic 500. GET-by-id explicitly avoids this with its
            // own ResponseStatusException(NOT_FOUND) - update()/delete() don't.
            // Worth a small fix (an @ExceptionHandler mapping DaoException to
            // 404) - flagging it here rather than changing it unasked.
            Monster fake = newMonster("ZZTest Placeholder", false, "user");
            fake.setId(999999);

            mockMvc.perform(put("/api/monsters/999999")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(fake)))
                    .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    class Delete {

        @Test
        @WithMockUser(username = "user")
        void owner_canDeleteTheirOwnMonster() throws Exception {
            Monster monster = newMonster("ZZTest Doomed", true, "user");

            mockMvc.perform(delete("/api/monsters/" + monster.getId()))
                    .andExpect(status().isNoContent());

            assertNull(monsterDao.getMonsterById(monster.getId()));
        }

        @Test
        @WithMockUser(username = "user")
        void nonOwnerNonAdmin_cannotDeleteSomeoneElsesMonster() throws Exception {
            Monster monster = newMonster("ZZTest Protected", true, "admin");

            mockMvc.perform(delete("/api/monsters/" + monster.getId()))
                    .andExpect(status().isForbidden());

            assertNotNull(monsterDao.getMonsterById(monster.getId()));
        }
    }
}
