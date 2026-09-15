package org.example.controllers;

import org.example.daos.EncounterDao;
import org.example.daos.EncounterPlayerCharacterDao;
import org.example.daos.PlayerCharacterDao;
import org.example.models.Encounter;
import org.example.models.EncounterPlayerCharacter;
import org.example.models.PlayerCharacter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller test for EncounterPlayerCharacterController - mirror of
 * EncounterMonsterControllerTest (see that class for the full explanation),
 * just for characters instead of monsters. Same reasoning applies: the
 * GetPlayerCharacterList nested class is what proves the visibility fix in
 * EncounterPlayerCharacterService.getPlayerCharacterList() actually works
 * end-to-end over real HTTP, and every other action uses canModify()
 * (ownership required), not canView() (visibility is enough).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EncounterPlayerCharacterControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EncounterDao encounterDao;
    @Autowired
    private PlayerCharacterDao playerCharacterDao;
    @Autowired
    private EncounterPlayerCharacterDao encounterPlayerCharacterDao;

    private int aragornId;

    @BeforeEach
    void setUp() {
        PlayerCharacter aragorn = playerCharacterDao.create(new PlayerCharacter(0, "ZZTest Fixture Aragorn",
                "Ranger", 20, 5, 16, "", true, "user", null));
        aragornId = aragorn.getId();
    }

    private Encounter newEncounter(boolean isPublic, String creatorUsername) {
        return encounterDao.create(new Encounter(0, "ZZTest Fixture Encounter", "", isPublic, creatorUsername, null, 1));
    }

    @Nested
    class GetPlayerCharacterList {

        @Test
        @WithMockUser(username = "user")
        void aPublicEncountersCharacterList_isVisibleToAnyLoggedInUser() throws Exception {
            Encounter encounter = newEncounter(true, "admin");
            encounterPlayerCharacterDao.addCharacterToEncounter(encounter.getId(), aragornId);

            mockMvc.perform(get("/api/encounters/" + encounter.getId() + "/characters"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @WithMockUser(username = "user")
        void aPrivateEncountersCharacterList_isRejectedForAStranger() throws Exception {
            Encounter encounter = newEncounter(false, "admin");
            encounterPlayerCharacterDao.addCharacterToEncounter(encounter.getId(), aragornId);

            mockMvc.perform(get("/api/encounters/" + encounter.getId() + "/characters"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "user")
        void aPrivateEncountersCharacterList_isVisibleToItsOwner() throws Exception {
            Encounter encounter = newEncounter(false, "user");
            encounterPlayerCharacterDao.addCharacterToEncounter(encounter.getId(), aragornId);

            mockMvc.perform(get("/api/encounters/" + encounter.getId() + "/characters"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @WithMockUser(username = "admin", authorities = "ADMIN")
        void aPrivateEncountersCharacterList_isVisibleToAnAdmin() throws Exception {
            Encounter encounter = newEncounter(false, "user");
            encounterPlayerCharacterDao.addCharacterToEncounter(encounter.getId(), aragornId);

            mockMvc.perform(get("/api/encounters/" + encounter.getId() + "/characters"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    @Nested
    class Add {

        @Test
        @WithMockUser(username = "user")
        void owner_canAddACharacterToTheirOwnEncounter() throws Exception {
            Encounter encounter = newEncounter(true, "user");

            mockMvc.perform(post("/api/encounters/" + encounter.getId() + "/characters/" + aragornId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.playerCharacterName").value("ZZTest Fixture Aragorn"));
        }

        @Test
        @WithMockUser(username = "user")
        void nonOwnerNonAdmin_cannotAddACharacter_evenToAPublicEncounter() throws Exception {
            Encounter encounter = newEncounter(true, "admin");

            mockMvc.perform(post("/api/encounters/" + encounter.getId() + "/characters/" + aragornId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class UpdateHealth {

        @Test
        @WithMockUser(username = "user")
        void owner_canUpdateACharactersHealth() throws Exception {
            Encounter encounter = newEncounter(true, "user");
            EncounterPlayerCharacter added = encounterPlayerCharacterDao.addCharacterToEncounter(encounter.getId(), aragornId);

            mockMvc.perform(patch("/api/encounters/" + encounter.getId() + "/characters/" + added.getId() + "/health")
                            .contentType("application/json")
                            .content("{\"currentHealth\": 3}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currentHealth").value(3));
        }

        @Test
        @WithMockUser(username = "user")
        void nonOwnerNonAdmin_cannotUpdateHealth() throws Exception {
            Encounter encounter = newEncounter(true, "admin");
            EncounterPlayerCharacter added = encounterPlayerCharacterDao.addCharacterToEncounter(encounter.getId(), aragornId);

            mockMvc.perform(patch("/api/encounters/" + encounter.getId() + "/characters/" + added.getId() + "/health")
                            .contentType("application/json")
                            .content("{\"currentHealth\": 3}"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class Remove {

        @Test
        @WithMockUser(username = "user")
        void owner_canRemoveACharacterFromTheirOwnEncounter() throws Exception {
            Encounter encounter = newEncounter(true, "user");
            EncounterPlayerCharacter added = encounterPlayerCharacterDao.addCharacterToEncounter(encounter.getId(), aragornId);

            mockMvc.perform(delete("/api/encounters/" + encounter.getId() + "/characters/" + added.getId()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(username = "user")
        void nonOwnerNonAdmin_cannotRemoveACharacter() throws Exception {
            Encounter encounter = newEncounter(true, "admin");
            EncounterPlayerCharacter added = encounterPlayerCharacterDao.addCharacterToEncounter(encounter.getId(), aragornId);

            mockMvc.perform(delete("/api/encounters/" + encounter.getId() + "/characters/" + added.getId()))
                    .andExpect(status().isForbidden());
        }
    }
}
