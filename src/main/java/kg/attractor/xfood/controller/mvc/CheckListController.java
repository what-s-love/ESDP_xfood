package kg.attractor.xfood.controller.mvc;

import kg.attractor.xfood.dto.checklist.CheckListSupervisorCreateDto;
import kg.attractor.xfood.dto.criteria.CriteriaSupervisorCreateDto;
import kg.attractor.xfood.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/checks")
public class CheckListController {
    private final CheckListService checkListService;
    private final CheckTypeService checkTypeService;
    private final CriteriaService criteriaService;
    private final ZoneService zoneService;
    private final SectionService sectionService;
    private final WorkScheduleService workScheduleService;


//    // ROLE: SUPERVISOR
@GetMapping("/create")
public String create (@RequestParam(name = "date", required = true) LocalDateTime date, @RequestParam(name ="managerId", required = true) Long managerId, @RequestParam(name = "expertId", required = true)Long expertId, Model model) {
    model.addAttribute("zones",zoneService.getZones() );
    model.addAttribute("sections", sectionService.getSections());
    model.addAttribute("pizzeriaId", workScheduleService.getPizzeriaId(managerId, date));
    model.addAttribute("types",checkTypeService.getTypes()); workScheduleService.getPizzeriaId(managerId, date);
    model.addAttribute("criteriaSupervisorCreateDto", new CriteriaSupervisorCreateDto());
    model.addAttribute("date", date);
    model.addAttribute("managerId", managerId);
    model.addAttribute("expertId", expertId);
    return "checklist/create";
}


    // ROLE: SUPERVISOR
    @PostMapping("/create")
    public String create (CheckListSupervisorCreateDto createDto, BindingResult result, Model model) {
        checkListService.create(createDto);
        return "redirect:/supervisor/weekly";
        // TODO назначение проверки
    }

//    // ROLE: ADMIN
//    @GetMapping ("/{id}/edit")
//    public String edit (@PathVariable (name="id") Long checkListId, Model model) {
//        return null;
//    }
//
//    // ROLE: ADMIN
//    @PostMapping ("/{id}/edit")
//    public String edit (@PathVariable (name="id") Long checkListId, BindingResult result, Model model) {
//        return null;
//    }

    // ROLE: EXPERT
    @GetMapping ("/{id}/check")
    public String check (@PathVariable (name="id") Long checkListId, Model model) {
         /* TODO:
             Проверка
        */

        return null;
    }

    // ROLE: EXPERT
    @PostMapping ("/{id}/check")
    public String check (@PathVariable (name="id") Long checkListId, BindingResult result, Model model) {
        /* TODO:
            Подтверждение проверки экспертом
        */

        return null;
    }

    // ROLE: SUPERVISOR, ADMIN
    @GetMapping ("/{id}/change")
    public String changeResult (@PathVariable (name="id") Long checkListId, Model model) {
        /* TODO:
            Изменение результата проверки *после
            подтверждения экспертом
        */

        return null;
    }

    // ROLE: SUPERVISOR, ADMIN
    @PostMapping ("/{id}/change")
    public String changeResult (@PathVariable (name="id") Long checkListId, BindingResult result, Model model) {
        /* TODO:
            Изменение результата проверки *после
            подтверждения экспертом
        */

        return null;
    }


    @GetMapping ("/{id}/result")
    public String getResult (@PathVariable (name = "id") Long checkListId, Model model) {
        model.addAttribute("checkList", checkListService.getResult(checkListId));

        return "checklist/result";
    }

}
