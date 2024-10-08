package kg.attractor.xfood.controller.mvc;

import kg.attractor.xfood.AuthParams;
import kg.attractor.xfood.dto.checklist.CheckListMiniSupervisorCreateDto;
import kg.attractor.xfood.dto.checklist.CheckListSupervisorCreateDto;
import kg.attractor.xfood.dto.checklist.CheckListSupervisorEditDto;
import kg.attractor.xfood.dto.checklist.ChecklistShowDto;
import kg.attractor.xfood.dto.comment.CommentDto;
import kg.attractor.xfood.dto.criteria.CriteriaSupervisorCreateDto;
import kg.attractor.xfood.enums.Role;
import kg.attractor.xfood.enums.Status;
import kg.attractor.xfood.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collection;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/checks")
public class ChecksController {
    private final CheckListService checkListService;
    private final CheckTypeService checkTypeService;
    private final ZoneService zoneService;
    private final SectionService sectionService;
    private final WorkScheduleService workScheduleService;
    private final SettingService settingService;

    @GetMapping
    public String getChecks (Model model) {
        if (AuthParams.getPrincipal().getAuthorities().contains(Role.EXPERT)) {
            model.addAttribute("checksCount", checkListService.getAmountOfNewChecks());
        }
        return "checklist/checks";
    }

    // ROLE: SUPERVISOR
    @GetMapping("/create")
    public String create(@RequestParam(name = "date") LocalDate date, @RequestParam(name = "managerId") Long managerId, @RequestParam(name = "expertId") Long expertId, Model model) {
        model.addAttribute("zones", zoneService.getZones());
        model.addAttribute("sections", sectionService.getSectionsWithoutCritAndWow());
        model.addAttribute("workSchedule", workScheduleService.getWorkSchedule(managerId, date));
        model.addAttribute("types", checkTypeService.getTypes());
        model.addAttribute("criteriaSupervisorCreateDto", new CriteriaSupervisorCreateDto());
        model.addAttribute("date", date);
        model.addAttribute("managerId", managerId);
        model.addAttribute("expertId", expertId);
        return "checklist/create";
    }

    // ROLE: SUPERVISOR
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @PostMapping("/create")
    public String create(CheckListSupervisorCreateDto createDto) {
        CheckListMiniSupervisorCreateDto checklistDto = checkListService.create(createDto);
        checkListService.bindChecklistWithCriterion(checklistDto);
        return "redirect:/checks";
    }

    @GetMapping("{uuid}")
    public String getCheck(@PathVariable String uuid, Model model, Authentication auth) {
        if(auth == null) {
            model.addAttribute("guest", true);
        }
        ChecklistShowDto checkList = checkListService.getCheckListByUuid(uuid);
        model.addAttribute("checkList", checkList);
        boolean isRecent = settingService.isCheckRecent(checkList);
        model.addAttribute("isRecent", settingService.isCheckRecent(checkList));
        return "checklist/result";
    }

    @PreAuthorize("hasRole('EXPERT')")
    @GetMapping("{uuid}/fill")
    public String getCheckForFill(@PathVariable String uuid, Model model) {
        Collection<? extends GrantedAuthority> authorities = AuthParams.getAuth().getAuthorities();
        ChecklistShowDto checkListDto = checkListService.getCheckListByUuid(uuid);
        String role = authorities.stream().toList().get(0).getAuthority();
        if (role.equalsIgnoreCase("role_expert")) {
            String authExpertEmail = AuthParams.getPrincipal().getUsername();
            if (authExpertEmail.equals(checkListDto.getExpertEmail())) {
                model.addAttribute("checkList", checkListDto);
            } else {
                model.addAttribute("error", "Эта проверка не назначена на вас!");
            }
        } else {
            model.addAttribute("checkList", checkListDto);
        }
        return "checklist/check_list";
    }

    // ROLE: SUPERVISOR
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @GetMapping ("/{uuid}/edit")
    public String edit (@PathVariable (name="uuid") String uuid,@RequestParam(name = "type", required = false) String type,  Model model) {
            model.addAttribute("zones",zoneService.getZones() );
            model.addAttribute("sections", sectionService.getSections());
            model.addAttribute("checklist", checkListService.getChecklistByUuid(uuid, type));
            model.addAttribute("types",checkTypeService.getTypes());
            model.addAttribute("type", type);
        return "checklist/edit";
    }

    // ROLE: SUPERVISOR
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @PostMapping("/{uuid}/edit")
    public String edit(@PathVariable(name = "uuid") String uuid, CheckListSupervisorEditDto checkList) {
        checkListService.edit(checkList);
        return "redirect:/checks/" + uuid;
    }

    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    @PostMapping("{uuid}/delete")
    public String delete(@PathVariable String uuid) {
        checkListService.delete(uuid);
        return "redirect:/checks";
    }

    @PostMapping("/{uuid}/{criteriaId}")
    public String comment(@PathVariable(name = "uuid") String uuid, @PathVariable(name = "criteriaId") Long
            criteriaId, CommentDto commentDto) {
        checkListService.comment(uuid, criteriaId, commentDto);
        return "redirect:/checks/" + uuid + "/fill";

    }

    @PostMapping("{uuid}/restore")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String restore(@PathVariable String uuid) {
        checkListService.restore(uuid);
        return "redirect:/checks";
    }

}
