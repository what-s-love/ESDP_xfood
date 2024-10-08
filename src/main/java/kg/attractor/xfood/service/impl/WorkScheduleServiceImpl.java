package kg.attractor.xfood.service.impl;

import jakarta.validation.Valid;
import kg.attractor.xfood.dto.WorkScheduleSupervisorShowDto;
import kg.attractor.xfood.dto.workSchedule.DailyWorkScheduleShowDto;
import kg.attractor.xfood.dto.workSchedule.WeekDto;
import kg.attractor.xfood.dto.workSchedule.WeeklyScheduleShowDto;
import kg.attractor.xfood.dto.workSchedule.WorkScheduleCreateDto;
import kg.attractor.xfood.model.Manager;
import kg.attractor.xfood.model.Pizzeria;
import kg.attractor.xfood.model.WorkSchedule;
import kg.attractor.xfood.repository.ManagerRepository;
import kg.attractor.xfood.repository.WorkScheduleRepository;
import kg.attractor.xfood.service.WorkScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkScheduleServiceImpl implements WorkScheduleService {
    private final ManagerRepository managerRepository;

    private final WorkScheduleRepository workScheduleRepository;
    private final DtoBuilder dtoBuilder;
    private final PizzeriaServiceImpl pizzeriaService;

    public WeekDto getWeekInfo(long week) {
        LocalDateTime monday = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime chosenMonday = monday.plusDays(7 * week);
        LocalDateTime sunday = chosenMonday.plusDays(6);
        return dtoBuilder.buildWeekDto(week, chosenMonday.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), sunday.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
    }
    @Override
    public List<WeeklyScheduleShowDto> getWeeklySchedulesByPizzeriaId(long pizzeriaId, long week) {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime chosenMonday = monday.plusDays(7 * week);

        List<WorkSchedule> workSchedulesOfWeek =
                workScheduleRepository.findByPizzeria_IdAndStartTimeBetweenOrderByManager_SurnameAsc(pizzeriaId, chosenMonday.toLocalDate(), chosenMonday.plusDays(6).toLocalDate());

        Set<Manager> managersOfPizzeria = new HashSet<>();
        workSchedulesOfWeek.forEach(e -> managersOfPizzeria.add(e.getManager()));

        List<WeeklyScheduleShowDto> weeklyDtos = new ArrayList<>();

        managersOfPizzeria.forEach(e -> weeklyDtos.add(createWeeklySchedule(e, pizzeriaId, chosenMonday)));
        
        weeklyDtos.forEach(e->e.setPizzeriaDto(
                dtoBuilder.buildPizzeriaDto(pizzeriaService.getPizzeriaById(pizzeriaId))
        ));

        weeklyDtos.sort(Comparator.comparing(e -> e.getManager().getSurname()));

        return weeklyDtos;
    }

    @Override
    public WorkSchedule findWorkScheduleByManagerAndDate(Long managerId, LocalDate date) {
        WorkSchedule workSchedule =  workScheduleRepository.findByManager_IdAndStartTimeDate(
                        managerId, date)
                .orElseThrow(() -> new NoSuchElementException("No such work_schedule"));
        log.error("work schedule: {}", workSchedule.getPizzeria().getId());
        return workSchedule;
    }

    @Override
    public WorkScheduleSupervisorShowDto getWorkSchedule(Long managerId, LocalDate date) {
        return dtoBuilder.buildWorkScheduleShowDto(findWorkScheduleByManagerAndDate(managerId, date));
    }

    @Override
    public void save(WorkSchedule workSchedule) {
        workScheduleRepository.save(workSchedule);
    }

    @Override
    public void prepareScheduleForCheck(@Valid WorkScheduleCreateDto schedule){
        Manager manager = managerRepository.findById(schedule.getManagerId()).orElseThrow(() -> new NoSuchElementException("Manager not found by id"));
        Pizzeria pizzeria = pizzeriaService.getPizzeriaById(schedule.getPizzeriaId());
        LocalDateTime startTime = LocalDateTime.of(schedule.getCurrentDate(), LocalTime.of(schedule.getStartTimeHour(), schedule.getStartTimeMinute()));
        LocalDateTime endTime = LocalDateTime.of(schedule.getCurrentDate(), LocalTime.of(schedule.getEndTimeHour(), schedule.getEndTimeMinute()));
        WorkSchedule workSchedule = WorkSchedule.builder()
                .manager(manager)
                .pizzeria(pizzeria)
                .startTime(startTime)
                .endTime(endTime).build();
        save(workSchedule);
    }

    public WeeklyScheduleShowDto createWeeklySchedule(Manager manager, Long pizzeriaId, LocalDateTime monday) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        WeeklyScheduleShowDto dto = new WeeklyScheduleShowDto();
        List<DailyWorkScheduleShowDto> managerSchedules = new ArrayList<>();

        for (LocalDateTime dayOfWeek = monday; dayOfWeek.isBefore(monday.plusDays(7)); dayOfWeek = dayOfWeek.plusDays(1)) {
            DailyWorkScheduleShowDto shift = new DailyWorkScheduleShowDto();
            Optional<WorkSchedule> schedule = workScheduleRepository.findByManager_IdAndPizzeria_IdAndStartTime(manager.getId(), pizzeriaId, dayOfWeek.toLocalDate());
            log.info("Optional schedule: " + schedule);
            if (schedule.isPresent()){
                shift.setId(schedule.get().getId());
                shift.setWorkDay(true);
                shift.setDate(schedule.get().getStartTime().toLocalDate().format(dateFormatter));
                shift.setStartTime(schedule.get().getStartTime().toLocalTime().format(timeFormatter));
                shift.setEndTime(schedule.get().getEndTime().toLocalTime().format(timeFormatter));
                log.info("Shift: " + shift);
            } else {
                shift.setDate(dayOfWeek.toLocalDate().format(dateFormatter));
                shift.setWorkDay(false);
                log.info("Shift: " + shift);
            }
            managerSchedules.add(shift);
        }
        dto.setManager(dtoBuilder.buildManagerShowDto(manager));
        dto.setWeeklySchedule(managerSchedules);
        return dto;
    }
    
    @Modifying
    public void add(WorkSchedule e) {
        workScheduleRepository.save(e);
    }
    
    public boolean exists(WorkSchedule workSchedule) {
        return workScheduleRepository.existsByManagerIdAndPizzeriaIdAndStartTimeAndEndTime(
                workSchedule.getManager().getId(),
                workSchedule.getPizzeria().getId(),
                workSchedule.getStartTime(),
                workSchedule.getEndTime());
    }
//
//    private LocalDate getMonday() {
//        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
//    }
//
//    private LocalDate getSunday() {
//        return LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
//    }
}
