package com.site.webapp.controllers;

import com.site.webapp.dto.TeamCreateDto;
import com.site.webapp.exception.team.TeamAccessDeniedException;
import com.site.webapp.models.Team;
import com.site.webapp.models.User;
import com.site.webapp.repo.UserRepository;
import com.site.webapp.security.CustomUserDetails;
import com.site.webapp.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/teams")
public class TeamController extends LoggingController {

    private final TeamService teamService;
    private final UserRepository userRepository;

    public TeamController(TeamService teamService, UserRepository userRepository) {
        this.teamService = teamService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listTeams(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        model.addAttribute("teams", teamService.getUserTeams(currentUser.getId()));
        return "teams";
    }

    @GetMapping("/new")
    public String newTeamForm(Model model) {
        model.addAttribute("teamForm", new TeamCreateDto());
        return "team-create";
    }

    @PostMapping
    public String createTeam(@Valid @ModelAttribute("teamForm") TeamCreateDto teamForm,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal CustomUserDetails currentUser,
                             Model model) {
        if (bindingResult.hasErrors()) {
            return "team-create";
        }

        String name = teamForm.getName();
        if (name != null && (name.length() < 2 || name.length() > 100)) {
            bindingResult.rejectValue("name", "Size",
                    "Название должно содержать от 2 до 100 символов");
            return "team-create";
        }

        Team team = new Team(teamForm.getName(), null);
        Team createdTeam = teamService.createTeam(team, currentUser);
        return "redirect:/teams/" + createdTeam.getId();
    }

    @GetMapping("/{id}")
    public String teamDetails(@PathVariable Long id,
                              @AuthenticationPrincipal CustomUserDetails currentUser,
                              Model model) {
        Team team = teamService.getTeamById(id);
        if (!team.isMember(currentUser.getId())) {
            throw new TeamAccessDeniedException(id);
        }
        User owner = userRepository.findById(team.getOwnerId()).orElse(null);
        model.addAttribute("team", team);
        model.addAttribute("owner", owner);
        return "team-details";
    }
}
