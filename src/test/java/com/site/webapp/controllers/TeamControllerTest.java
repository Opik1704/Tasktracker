package com.site.webapp.controllers;

import com.site.webapp.models.Team;
import com.site.webapp.models.User;
import com.site.webapp.repo.TeamRepository;
import com.site.webapp.repo.UserRepository;
import com.site.webapp.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class TeamControllerTest {

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mockMvc;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private CustomUserDetails ownerDetails;
    private User otherUser;
    private CustomUserDetails otherUserDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teamRepository.deleteAll();
        userRepository.deleteAll();

        owner = new User();
        owner.setFirstName("Owner");
        owner.setLastName("Ownerov");
        owner.setEmail("owner@test.com");
        owner.setPassword("password");
        owner = userRepository.save(owner);
        ownerDetails = new CustomUserDetails(owner);

        otherUser = new User();
        otherUser.setFirstName("Other");
        otherUser.setLastName("Otherov");
        otherUser.setEmail("other@test.com");
        otherUser.setPassword("password");
        otherUser = userRepository.save(otherUser);
        otherUserDetails = new CustomUserDetails(otherUser);
    }

    // AC-1: Создание команды авторизованным пользователем — редирект 302 на /teams/{id}
    @Test
    void createTeam_validName_redirectsToTeamDetails() throws Exception {
        mockMvc.perform(post("/teams")
                .with(csrf())
                .with(user(ownerDetails))
                .param("name", "My Team"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("/teams/**"));
    }

    // AC-1: После редиректа команда доступна на странице /teams/{id}
    @Test
    void createTeam_thenTeamDetailsAccessible() throws Exception {
        Team team = new Team("My Team", owner.getId());
        team.addMember(owner);
        team = teamRepository.save(team);

        mockMvc.perform(get("/teams/{id}", team.getId())
                .with(user(ownerDetails)))
            .andExpect(status().isOk())
            .andExpect(view().name("team-details"))
            .andExpect(model().attributeExists("team"));
    }

    // AC-2: Владелец автоматически становится участника — проверяется на уровне сервиса (TeamServiceTest)

    // AC-3: Валидация — пустое название
    @Test
    void createTeam_emptyName_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/teams")
                .with(csrf())
                .with(user(ownerDetails))
                .param("name", ""))
            .andExpect(status().isOk())
            .andExpect(view().name("team-create"))
            .andExpect(model().attributeHasFieldErrors("teamForm", "name"))
            .andExpect(model().attributeHasFieldErrorCode("teamForm", "name", "NotBlank"));
    }

    // AC-3: Валидация — название из 1 символа
    @Test
    void createTeam_oneCharName_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/teams")
                .with(csrf())
                .with(user(ownerDetails))
                .param("name", "A"))
            .andExpect(status().isOk())
            .andExpect(view().name("team-create"))
            .andExpect(model().attributeHasFieldErrors("teamForm", "name"))
            .andExpect(model().attributeHasFieldErrorCode("teamForm", "name", "Size"));
    }

    // AC-3: Валидация — название более 100 символов
    @Test
    void createTeam_nameOver100Chars_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/teams")
                .with(csrf())
                .with(user(ownerDetails))
                .param("name", "A".repeat(101)))
            .andExpect(status().isOk())
            .andExpect(view().name("team-create"))
            .andExpect(model().attributeHasFieldErrors("teamForm", "name"))
            .andExpect(model().attributeHasFieldErrorCode("teamForm", "name", "Size"));
    }

    // AC-3: Граничное значение — ровно 2 символа (должно пройти валидацию)
    @Test
    void createTeam_nameExactly2Chars_redirects() throws Exception {
        mockMvc.perform(post("/teams")
                .with(csrf())
                .with(user(ownerDetails))
                .param("name", "AB"))
            .andExpect(status().is3xxRedirection());
    }

    // AC-3: Граничное значение — ровно 100 символов (должно пройти валидацию)
    @Test
    void createTeam_nameExactly100Chars_redirects() throws Exception {
        mockMvc.perform(post("/teams")
                .with(csrf())
                .with(user(ownerDetails))
                .param("name", "A".repeat(100)))
            .andExpect(status().is3xxRedirection());
    }

    // AC-3: Команда не создаётся при ошибках валидации
    @Test
    void createTeam_validationErrors_teamNotCreated() throws Exception {
        int teamsBefore = teamRepository.findAll().size();

        mockMvc.perform(post("/teams")
                .with(csrf())
                .with(user(ownerDetails))
                .param("name", ""))
            .andExpect(status().isOk())
            .andExpect(view().name("team-create"));

        assertEquals(teamsBefore, teamRepository.findAll().size());
    }

    // AC-4: Просмотр списка своих команд — HTTP 200
    @Test
    void listTeams_returns200() throws Exception {
        mockMvc.perform(get("/teams")
                .with(user(ownerDetails)))
            .andExpect(status().isOk())
            .andExpect(view().name("teams"))
            .andExpect(model().attributeExists("teams"));
    }

    // AC-4: GET /teams/new — HTTP 200 с формой
    @Test
    void newTeamForm_returns200() throws Exception {
        mockMvc.perform(get("/teams/new")
                .with(user(ownerDetails)))
            .andExpect(status().isOk())
            .andExpect(view().name("team-create"))
            .andExpect(model().attributeExists("teamForm"));
    }

    // AC-5: Просмотр деталей команды участником — HTTP 200
    @Test
    void teamDetails_asMember_returns200() throws Exception {
        Team team = new Team("Test Team", owner.getId());
        team.addMember(owner);
        team = teamRepository.save(team);

        mockMvc.perform(get("/teams/{id}", team.getId())
                .with(user(ownerDetails)))
            .andExpect(status().isOk())
            .andExpect(view().name("team-details"))
            .andExpect(model().attributeExists("team"));
    }

    // AC-6: Запрет доступа не-участнику — HTTP 403
    @Test
    void teamDetails_asNonMember_returns403() throws Exception {
        Team team = new Team("Test Team", owner.getId());
        team.addMember(owner);
        team = teamRepository.save(team);

        mockMvc.perform(get("/teams/{id}", team.getId())
                .with(user(otherUserDetails)))
            .andExpect(status().isForbidden());
    }

    // AC-7: Команда не найдена — HTTP 404
    @Test
    void teamDetails_nonexistentTeam_returns404() throws Exception {
        mockMvc.perform(get("/teams/{id}", 999L)
                .with(user(ownerDetails)))
            .andExpect(status().isNotFound());
    }
}