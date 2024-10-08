//package kg.attractor.xfood.controller.rest;
//
//import kg.attractor.xfood.AuthParams;
//import kg.attractor.xfood.dto.workSchedule.WeeklyScheduleShowDto;
//import kg.attractor.xfood.service.OkHttpService;
//import kg.attractor.xfood.service.PizzeriaService;
//import kg.attractor.xfood.service.impl.WorkScheduleServiceImpl;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/managers-work-schedule")
//@PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
//public class WorkScheduleController {
//    private final WorkScheduleServiceImpl workScheduleService;
//    private final OkHttpService okHttpService;
//    private final PizzeriaService pizzeriaService;
//
//    @GetMapping("pizzeria/{id}")
//    @PreAuthorize("hasAnyAuthority('admin:read','supervisor:read')")
//    public ResponseEntity<List<WeeklyScheduleShowDto>> getManagersSchedules (
//            @PathVariable (name = "id") Long pizzeriaId
//    ) {
//        List<WeeklyScheduleShowDto> dtos = workScheduleService.getWeeklySchedulesByPizzeriaId(pizzeriaId, 0);
//
//        okHttpService.getWorksheetOfPizzeriaManagers(pizzeriaId, AuthParams.getPrincipal().getUsername());
//
//        return ResponseEntity.ok(dtos);
//    }
//
//}
