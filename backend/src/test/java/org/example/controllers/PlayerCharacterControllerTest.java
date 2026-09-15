package org.example.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.daos.PlayerCharacterDao;
import org.example.models.PlayerCharacter;
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
 * Controller test for PlayerCharacterController - same shape and reasoning
 * as MonsterControllerTest (see that class for the full explanation of
 * @WithMockUser, the authorities = "ADMIN" requirement, and why fixture
 * usernames are limited to "user" and "admin"). PlayerCharacterController's
 * own ownership rules are identical to MonsterController's, so this file
 * mirrors it method-for-method.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PlayerCharacterControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PlayerCharacterDao playerCharacterDao;

    private PlayerCharacter newCharacter(String name, boolean isPublic, String creatorUsername) {
        PlayerCharacter character = new PlayerCharacter(0, name, "Ranger", 20, 5, 16,
                "A character created by PlayerCharacterControllerTest.", isPublic, creatorUsername, null);
        return playerCharacterDao.create(character);
    }

    @Nested
    class Authentication {

        @Test
        void anUnauthenticatedRequest_isRejectedWithoutReachingTheController() throws Exception {
            mockMvc.perform(get("/api/characters"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class Get {

        @Test
        @WithMockUser(username = "user")
        void aPublicCharacter_isVisibleToAnyLoggedInUser() throws Exception {
            PlayerCharacter character = newCharacter("ZZTest Public Aragorn", true, "admin");

            mockMvc.perform(get("/api/characters/" + character.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("ZZTest Public Aragorn"));
        }

        @Test
        @WithMockUser(username = "user")
        void aCharacterThatDoesNotExist_returns404() throws Exception {
            mockMvc.perform(get("/api/characters/999999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "user")
        void aPrivateCharacterOwnedBySomeoneElse_returns403() throws Exception {
            PlayerCharacter character = newCharacter("ZZTest Someones Secret Character", false, "admin");

            mockMvc.perform(get("/api/characters/" + character.getId()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class Search {

        @Test
        @WithMockUser(username = "user")
        void returnsAPageOfResults_matchingTheGivenFilters() throws Exception {
            newCharacter("ZZTest Search Legolas", true, "user");

            mockMvc.perform(get("/api/characters")
                            .param("name", "ZZTest Search")
                            .param("visibility", "all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCount").value(1))
                    .andExpect(jsonPath("$.items[0].name").value("ZZTest Search Legolas"));
        }
    }

    @Nested
    class Create {

        @Test
        @WithMockUser(username = "user")
        void createsTheCharacter_ignoringAnyCreatorUsernameInTheRequestBody() throws Exception {
            Map<String, Object> body = Map.of(
                    "name", "ZZTest New Character",
                    "characterClass", "Wizard",
                    "health", 15,
                    "level", 3,
                    "armorClass", 11,
                    "description", "",
                    "public", true,
                    "creatorUsername", "admin"
            );

            mockMvc.perform(post("/api/characters")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("ZZTest New Character"))
                    .andExpect(jsonPath("$.creatorUsername").value("user"));
        }
    }

    @Nested
    class Update {

        @Test
        @WithMockUser(username = "user")
        void owner_canUpdateTheirOwnCharacter() throws Exception {
            PlayerCharacter character = newCharacter("ZZTest Before", false, "user");
            character.setName("ZZTest After");
            character.setHealth(999);

            mockMvc.perform(put("/api/characters/" + character.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(character)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("ZZTest After"))
                    .andExpect(jsonPath("$.health").value(999));
        }

        @Test
        @WithMockUser(username = "user")
        void nonOwnerNonAdmin_cannotUpdateSomeoneElsesCharacter() throws Exception {
            PlayerCharacter character = newCharacter("ZZTest Not Yours", false, "admin");

            mockMvc.perform(put("/api/characters/" + character.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(character)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "admin", authorities = "ADMIN")
        void anAdmin_canUpdateSomeoneElsesCharacter() throws Exception {
            PlayerCharacter character = newCharacter("ZZTest Admin Target", false, "user");
            character.setName("ZZTest Admin Edited");

            mockMvc.perform(put("/api/characters/" + character.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(character)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("ZZTest Admin Edited"));
        }
    }

    @Nested
    class Delete {

        @Test
        @WithMockUser(username = "user")
        void owner_canDeleteTheirOwnCharacter() throws Exception {
            PlayerCharacter character = newCharacter("ZZTest Doomed", true, "user");

            mockMvc.perform(delete("/api/characters/" + character.getId()))
                    .andExpect(status().isNoContent());

            assertNull(playerCharacterDao.getCharacterById(character.getId()));
        }

        @Test
        @WithMockUser(username = "user")
        void nonOwnerNonAdmin_cannotDeleteSomeoneElsesCharacter() throws Exception {
            PlayerCharacter character = newCharacter("ZZTest Protected", true, "admin");

            mockMvc.perform(delete("/api/characters/" + character.getId()))
                    .andExpect(status().isForbidden());

            assertNotNull(playerCharacterDao.getCharacterById(character.getId()));
        }
    }
}
