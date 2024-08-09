package kg.attractor.xfood.service.impl;

import kg.attractor.xfood.AuthParams;
import kg.attractor.xfood.dao.CheckListDao;
import kg.attractor.xfood.dto.LocationDto;
import kg.attractor.xfood.dto.checklist.*;
import kg.attractor.xfood.dto.comment.CommentDto;
import kg.attractor.xfood.dto.criteria.CriteriaExpertShowDto;
import kg.attractor.xfood.dto.criteria.CriteriaMaxValueDto;
import kg.attractor.xfood.dto.expert.ExpertShowDto;
import kg.attractor.xfood.dto.manager.ManagerDto;
import kg.attractor.xfood.dto.pizzeria.PizzeriaDto;
import kg.attractor.xfood.dto.statistics.*;
import kg.attractor.xfood.dto.work_schedule.WorkScheduleSupervisorEditDto;
import kg.attractor.xfood.enums.Role;
import kg.attractor.xfood.enums.Status;
import kg.attractor.xfood.exception.IncorrectDateException;
import kg.attractor.xfood.exception.NotFoundException;
import kg.attractor.xfood.model.*;
import kg.attractor.xfood.repository.CheckListRepository;
import kg.attractor.xfood.repository.ChecklistCriteriaRepository;
import kg.attractor.xfood.repository.UserRepository;
import kg.attractor.xfood.service.*;
import kg.attractor.xfood.specification.ChecklistSpecification;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckListServiceImpl implements CheckListService {
    @Lazy
    private final CommentService commentService;
    private final WorkScheduleService workScheduleService;
    private CheckListCriteriaServiceImpl checkListCriteriaService;
    private ManagerService managerService;
    private final CriteriaService criteriaService;
    private final CheckListRepository checkListRepository;
    private final ChecklistCriteriaRepository checklistCriteriaRepository;
    private final UserRepository userRepository;
    private final CriteriaTypeService criteriaTypeService;
    private final PizzeriaService pizzeriaService;
    private final CheckListDao checkListDao;
    private final CheckTypeFeeService checkTypeFeeService;
    private  CheckListCriteriaCommentService checkListCriteriaCommentService;

    private final DtoBuilder dtoBuilder;

    private static final String DEFAULT = "default";
    private final CheckTypeService checkTypeService;

    @Autowired
    public void setCheckListCriteriaService(@Lazy CheckListCriteriaServiceImpl checkListCriteriaService) {
        this.checkListCriteriaService = checkListCriteriaService;
    }

    @Autowired
    public void setManagerService(@Lazy ManagerService managerService) {
        this.managerService = managerService;
    }

    @Autowired
    public void setCheckListCriteriaCommentService(@Lazy CheckListCriteriaCommentService checkListCriteriaCommentService) {
        this.checkListCriteriaCommentService = checkListCriteriaCommentService;
    }

    @Override
    public List<ChecklistMiniExpertShowDto> getUsersChecklists(String username, Status status) {
//        return null;
        return checkListRepository.findCheckListByExpertEmailAndStatus(username, status)
                .stream()
                .map(dtoBuilder::buildChecklistDto)
                .toList();
    }

    @Override
    @Transactional
    public CheckListMiniSupervisorCreateDto create(CheckListSupervisorCreateDto createDto) {
        log.info(createDto.getCriteriaMaxValueDtoList().toString());
        if (createDto.getCriteriaMaxValueDtoList().isEmpty()) {
            throw new IncorrectDateException("Чек лист не содержит критериев");
        }
        if (createDto.getStartTime().isAfter(createDto.getEndTime())) {
            throw new IncorrectDateException("Время начала не может быть позже время конца смены");
        }
        WorkSchedule workSchedule = workScheduleService.findWorkScheduleByManagerAndDate(createDto.getManagerId(), createDto.getDate());
        if (checkListRepository.existsByWorkSchedule_IdAndExpert_Id(workSchedule.getId(), createDto.getExpertId())) {
            throw new IncorrectDateException("Проверка на " + createDto.getStartTime() + " - " + createDto.getEndTime() + " и эксперта c айди " + createDto.getExpertId() + "уже создана");
        }

        log.info(workSchedule.getStartTime().toString());
        log.info(createDto.getEndTime().toString());
        if (createDto.getEndTime().isBefore(workSchedule.getStartTime().toLocalTime())) {
            throw new IncorrectDateException("Время начала смены менеджера не может быть позже времени окончания работы эксперта");
        }

        createDto.getCriteriaMaxValueDtoList().removeIf(criteriaMaxValueDto -> criteriaMaxValueDto.getCriteriaId() == null);
        createDto.getCriteriaMaxValueDtoList().sort(Comparator.comparing(CriteriaMaxValueDto::getCriteriaId));

        String uuid = UUID.randomUUID().toString();
        checkListRepository.saveCheckList(workSchedule.getId(), Status.NEW.getStatus(), createDto.getExpertId(), uuid, createDto.getCheckTypeId());
        checkListRepository.flush();
        return CheckListMiniSupervisorCreateDto.builder().checkTypeId(createDto.getCheckTypeId()).expertId(createDto.getExpertId()).workScheduleId(workSchedule.getId()).criteriaMaxValueDtoList(createDto.getCriteriaMaxValueDtoList()).pizzeria(workSchedule.getPizzeria()).build();
    }

    @Override
    public ChecklistShowDto getCheckListById(String id) {
        for (var authority : AuthParams.getAuth().getAuthorities()) {
            if (!authority.getAuthority().equals("ROLE_EXPERT") && authority.getAuthority() != null) {
                Optional<CheckList> deletedCheckList = findDeletedCheckList(id);
                if (deletedCheckList.isPresent()) {
                    return dtoBuilder.buildChecklistShowDto(deletedCheckList.get(), Boolean.TRUE);
                }
            }
        }

        CheckList checkList = getModelCheckListById(id);
        return dtoBuilder.buildChecklistShowDto(checkList);
    }

    private Optional<CheckList> findDeletedCheckList(String uuid) {
        return checkListRepository.findDeleted(uuid);
    }

    @Override
    public CheckList getModelCheckListById(String id) {
        return checkListRepository.findByUuidLink(id).orElseThrow(() -> new NoSuchElementException("Can't find checklist by uuid " + id));
    }

    @Override
    public CheckList getModelCheckListById(Long id) {
        return checkListRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Can't find checklist by ID " + id));
    }

    @Override
    public void save(CheckList checkList) {
        checkListRepository.save(checkList);
    }

    @Override
    public List<ChecklistMiniExpertShowDto> getUsersChecklists(Status status) {
        return checkListRepository.findCheckListByStatus(status)
                .stream()
                .map(dtoBuilder::buildChecklistDto)
                .toList();
    }


    @Override
    public CheckListResultDto getResult(String checkListId) {
        CheckList checkList = getModelCheckListById(checkListId);

        if (checkList.getStatus().equals(Status.DONE) || checkList.getStatus().equals(Status.IN_PROGRESS)) {
            return dtoBuilder.buildCheckListResultDto(
                    checkListRepository.findByIdAndStatus(checkListId, checkList.getStatus())
                            .orElseThrow(() -> new NotFoundException("Check list not found"))
            );
        }

        throw new IllegalArgumentException("По данной проверке нет еще результатов");
    }

    @Override
    public CheckListResultDto getResult(Long checkListId) {
        CheckList checkList = getModelCheckListById(checkListId);

        if (checkList.getStatus().equals(Status.DONE) || checkList.getStatus().equals(Status.IN_PROGRESS)) {
            return dtoBuilder.buildCheckListResultDto(
                    checkListRepository.findByIdAndStatus(checkListId, checkList.getStatus())
                            .orElseThrow(() -> new NotFoundException("Check list not found"))
            );
        }

        throw new IllegalArgumentException("По данной проверке нет еще результатов");
    }


    @Override
    public List<CheckListAnalyticsDto> getAnalytics(String pizzeriaId, String managerId, String expertId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByEmail(AuthParams.getPrincipal().getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<CheckList> checkLists = getCheckListsByUserRole(user);

        checkLists = filterByPizzeriaId(checkLists, pizzeriaId);
        checkLists = filterByManagerId(checkLists, managerId);
        checkLists = filterByExpertId(checkLists, expertId);
        checkLists = filterByDateRange(checkLists, startDate, endDate);

        return checkLists.stream()
                .map(dtoBuilder::buildCheckListAnalyticsDto)
                .toList();
    }

    private List<CheckList> getCheckListsByUserRole(User user) {
        if (user.getRole().equals(Role.EXPERT)) {
            return checkListRepository.findCheckListByExpertEmailAndStatus(user.getEmail(), Status.DONE);
        } else {
            return checkListRepository.findByStatus(Status.DONE);
        }
    }

    private List<CheckList> filterByPizzeriaId(List<CheckList> checkLists, String pizzeriaId) {
        if (!DEFAULT.equals(pizzeriaId)) {
            return checkLists.stream()
                    .filter(checkList -> checkList.getWorkSchedule().getPizzeria().getId().equals(Long.parseLong(pizzeriaId)))
                    .toList();
        }
        return checkLists;
    }

    private List<CheckList> filterByManagerId(List<CheckList> checkLists, String managerId) {
        if (!DEFAULT.equals(managerId)) {
            return checkLists.stream()
                    .filter(checkList -> checkList.getWorkSchedule().getManager().getId().equals(Long.parseLong(managerId)))
                    .toList();
        }
        return checkLists;
    }

    private List<CheckList> filterByExpertId(List<CheckList> checkLists, String expertId) {
        if (!DEFAULT.equals(expertId)) {
            return checkLists.stream()
                    .filter(checkList -> checkList.getExpert().getId().equals(Long.parseLong(expertId)))
                    .toList();
        }
        return checkLists;
    }

    private List<CheckList> filterByDateRange(List<CheckList> checkLists, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return checkLists.stream()
                    .filter(checkList -> {
                        LocalDate startTime = checkList.getWorkSchedule().getStartTime().toLocalDate();
                        LocalDate endTime = checkList.getWorkSchedule().getEndTime().toLocalDate();
                        return (startTime.isEqual(startDate) || startTime.isAfter(startDate)) &&
                                (endTime.isEqual(endDate) || endTime.isBefore(endDate));
                    })
                    .toList();
        }
        return checkLists;
    }

    @Override
    public CheckListResultDto getResultByUuidLink(String uuidLink) {
        return dtoBuilder.buildCheckListResultDto(
                checkListRepository.findByUuidLinkAndStatus(uuidLink, Status.DONE)
                        .orElseThrow(() -> new NotFoundException("Check list not found"))
        );
    }

    @Override
    public CheckList updateCheckStatusCheckList(String id) {
        CheckList checkList = getModelCheckListById(id);
        if (checkList.getStatus().equals(Status.DONE)) {
            throw new IllegalArgumentException("Данный чеклист уже опубликован");
        }

        checkList.setStatus(Status.DONE);
        checkList.setEndTime(LocalDateTime.now());
        checkListDao.updateStatusToDone(Status.DONE, checkList);
        return checkList;
    }

    @Override
    public CheckListSupervisorEditDto getChecklistByUuid(String uuid) {
        CheckList checkList = checkListRepository.findByUuidLink(uuid).orElseThrow(() -> new NotFoundException("Check list not found by uuid: " + uuid));
        ExpertShowDto expert = dtoBuilder.buildExpertShowDto(checkList.getExpert());

        WorkScheduleSupervisorEditDto workScheduleDto = WorkScheduleSupervisorEditDto.builder()
                .id(checkList.getWorkSchedule().getId())
                .startTime(checkList.getWorkSchedule().getStartTime())
                .endTime(checkList.getWorkSchedule().getEndTime())
                .pizzeria(dtoBuilder.buildPizzeriaDto(checkList.getWorkSchedule().getPizzeria()))
                .manager(dtoBuilder.buildManagerShowDto(checkList.getWorkSchedule().getManager()))
                .build();

        List<CheckListsCriteria> checkListsCriteria = checkListCriteriaService.findAllByChecklistId(checkList.getId());
        List<CriteriaExpertShowDto> criterionWithMaxValue = new ArrayList<>();
        int sum = 0;
        for (CheckListsCriteria criteria : checkListsCriteria) {
            sum += criteria.getMaxValue();
            criterionWithMaxValue.add(CriteriaExpertShowDto.builder()
                    .id(criteria.getCriteria().getId())
                    .maxValue(criteria.getMaxValue())
                    .description(criteria.getCriteria().getDescription())
                    .zone(criteria.getCriteria().getZone().getName())
                    .section(criteria.getCriteria().getSection().getName())
                    .build());
        }
        criterionWithMaxValue.removeIf(criteria -> !criteria.getSection().equals(""));
        return CheckListSupervisorEditDto.builder()
                .id(checkList.getUuidLink())
                .workSchedule(workScheduleDto)
                .expert(expert)
                .totalValue(sum)
                .criterion(criterionWithMaxValue.stream()
                        .sorted(Comparator.comparing(CriteriaExpertShowDto::getSection)
                                .thenComparing(CriteriaExpertShowDto::getZone)).toList())
                .build();
    }

    @Transactional
    @Override
    public void edit(CheckListSupervisorEditDto checkListDto) {
        log.info(checkListDto.toString());
        CheckList checkList = checkListRepository.findByUuidLink(checkListDto.getId()).orElseThrow(() -> new NotFoundException("Check list not found by uuid: " + checkListDto.getId()));
        Manager manager = managerService.findByPhoneNumber(checkListDto.getWorkSchedule().getManager().getPhoneNumber());
        if (checkListDto.getWorkSchedule().getStartTime().isAfter(checkListDto.getWorkSchedule().getEndTime())) {
            throw new IncorrectDateException("Время начала смены менеджера не может быть позже времени конца смены");
        }
        WorkSchedule workSchedule = WorkSchedule.builder()
                .id(checkList.getWorkSchedule().getId())
                .manager(manager)
                .startTime(checkListDto.getWorkSchedule().getStartTime())
                .endTime(checkListDto.getWorkSchedule().getEndTime())
                .pizzeria(checkList.getWorkSchedule().getPizzeria())
                .build();
        workScheduleService.save(workSchedule);
        log.info("list {}", checkListDto.getCriterion().toString());
        checkListDto.getCriterion().removeIf(criteria -> criteria.getId() == null);
        checkListCriteriaService.deleteCriterionByChecklist(checkList.getId());
        for (CriteriaExpertShowDto criteriaMaxValueDto : checkListDto.getCriterion()) {
            CheckListsCriteria checkListsCriteria = CheckListsCriteria.builder()
                    .checklist(checkList)
                    .criteria(criteriaService.findById(criteriaMaxValueDto.getId()))
                    .maxValue(criteriaMaxValueDto.getMaxValue())
                    .value(0)
                    .build();
            checkListCriteriaService.save(checkListsCriteria);
        }
        checkList.setWorkSchedule(workSchedule);
        checkListRepository.save(checkList);
    }

    @Override
    public Integer getMaxPoints(Long id) {
        List<CheckListsCriteria> criteriaList = checklistCriteriaRepository.findCriteriaByCheckListId(id);
        return (int) criteriaList.stream()
                .mapToDouble(criteria -> criteria.getMaxValue() != null ? criteria.getMaxValue() : 0.0)
                .sum();

    }

    @Override
    public Integer getPercentageById(Long id) {
        List<CheckListsCriteria> criteriaList = checklistCriteriaRepository.findCriteriaByCheckListId(id);
        Double normalMaxSum = criteriaList.stream()
                .filter(criteria -> !criteria.getCriteria().getSection().getName().equalsIgnoreCase("WOW фактор"))
                .mapToDouble(criteria -> criteria.getMaxValue() != null ? criteria.getMaxValue() : 0.0)
                .sum();

        Double normalValue = criteriaList.stream()
                .filter(criteria -> !criteria.getCriteria().getSection().getName().equalsIgnoreCase("WOW фактор"))
                .mapToDouble(CheckListsCriteria::getValue)
                .sum();

        Double wowValue = criteriaList.stream()
                .filter(criteria -> criteria.getCriteria().getSection().getName().equalsIgnoreCase("WOW фактор"))
                .mapToDouble(CheckListsCriteria::getValue)
                .sum();

        double percentage = (normalValue / normalMaxSum) * 100;

        if (percentage < 100) {
            Double totalValue = normalValue + wowValue;
            percentage = (totalValue / normalMaxSum) * 100;

            if (percentage > 100) {
                percentage = 100.0;
            }
        }
        return (int) Math.ceil(percentage);
    }

    @Override
    @Transactional
    public void delete(String uuid) {
        for (var authority : AuthParams.getPrincipal().getAuthorities()) {
            if (Objects.equals(authority.getAuthority(), "ROLE_SUPERVISOR")) {
                  checkListRepository.deleteByUuidLinkAndStatusIsNot(uuid, Status.DONE);
                  break;
            }
            if (Objects.equals(authority.getAuthority(), "ROLE_ADMIN")) {
                checkListRepository.deleteByUuidLink(uuid);
                break;
            }
        }
    }

    @Override
    public List<ChecklistMiniExpertShowDto> getDeletedChecklists() {
        return checkListRepository.findDeletedChecklists()
                .stream()
                .map(dtoBuilder::buildChecklistDto)
                .toList();
    }

    @Override
    public List<CheckListRewardDto> getChecklistRewardsByExpert(String expertEmail, LocalDateTime startDate, LocalDateTime endDate, String pizzeriaName) {
        Specification<CheckList> spec = Specification
                .where(ChecklistSpecification.hasStatus(Status.DONE))
                .and(ChecklistSpecification.hasExpert(expertEmail))
                .and(ChecklistSpecification.betweenDate(startDate, endDate))
                .and(ChecklistSpecification.hasPizzeria(pizzeriaName))
                .and(ChecklistSpecification.isDeleted(false));

        List<CheckList> checkLists = checkListRepository.findAll(spec);
        return checkLists.stream().map(dtoBuilder::buildCheckListRewardDto).toList();
    }

    @Override
    public void bindChecklistWithCriterion(CheckListMiniSupervisorCreateDto checklistDto) {
        CheckList checkList = checkListRepository.findCheckListByWorkSchedule_IdAndExpert_Id(checklistDto.getWorkScheduleId(), checklistDto.getExpertId()).orElseThrow(() -> new NoSuchElementException("Чек-лист не найден "));
        log.info(checkList.toString());
        for (CriteriaMaxValueDto criteriaMaxValueDto : checklistDto.getCriteriaMaxValueDtoList()) {
            CheckListsCriteria checkListsCriteria = CheckListsCriteria.builder()
                    .checklist(checkList)
                    .criteria(criteriaService.findById(criteriaMaxValueDto.getCriteriaId()))
                    .maxValue(criteriaMaxValueDto.getMaxValue())
                    .value(0)
                    .build();
            if (Objects.equals(criteriaService.findById(criteriaMaxValueDto.getCriteriaId()).getSection().getName(), "Критический фактор")) {
                checkListsCriteria.setMaxValue(1);
            }
            log.info("чеклист {} связан с критерием {}", checkList, criteriaMaxValueDto.getCriteriaId());
            checkListCriteriaService.save(checkListsCriteria);
            log.info("Чек лист и все необходимые связи созданы");

        }
    }

    public Manager getManagerById(long id) {
        return managerService.findById(id);
    }

    @Override
    public StatisticsDto getStatistics(LocalDate from, LocalDate to) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StatisticsDto statisticsDto = new StatisticsDto();
        List<CheckList> checkLists = checkListRepository.findAll();
        List<PizzeriaDto> pizzerias = pizzeriaService.getAllPizzerias();
        log.info("all checklists {}", checkLists);

        checkLists.removeIf(checkList -> checkList.getEndTime() == null ||
                !Objects.equals(checkList.getStatus().getStatus(), "DONE"));

        checkLists.removeIf(checkList ->
                checkList.getEndTime().toLocalDate().isBefore(from) ||
                        checkList.getEndTime().toLocalDate().isAfter(to));
        List<RowDto> rowDtos = new ArrayList<>();
        log.info("after removing checklists {}", checkLists);


        for (CheckList checkList : checkLists) {
            Manager manager = checkList.getWorkSchedule().getManager();
            Pizzeria pizzeria = checkList.getWorkSchedule().getPizzeria();
            User expert = checkList.getExpert();
            Integer points = checkListCriteriaService.findAllByChecklistId(checkList.getId())
                    .stream().mapToInt(CheckListsCriteria::getValue).sum();
            CellDto cellDto = CellDto.builder()
                    .date(checkList.getEndTime().toLocalDate().format(formatter))
                    .points(points)
                    .build();

            boolean found = false;
            for (RowDto rowDto : rowDtos) {
                if (rowDto.getPizzeria().getId().equals(pizzeria.getId()) &&
                        rowDto.getExpert().getId().equals(expert.getId()) &&
                        rowDto.getManager().getId().equals(manager.getId())) {
                    rowDto.getCells().add(cellDto);
                    found = true;
                    break;
                }
            }
            if (!found) {
                rowDtos.add(RowDto.builder()
                        .type(checkList.getCheckType().getName())
                        .manager(ManagerDto.builder()
                                .name(manager.getName())
                                .surname(manager.getSurname())
                                .id(manager.getId())
                                .phoneNumber(manager.getPhoneNumber())
                                .build())
                        .expert(ExpertShowDto.builder()
                                .name(expert.getName())
                                .surname(expert.getSurname())
                                .id(expert.getId())
                                .build())
                        .pizzeria(PizzeriaDto.builder()
                                .id(pizzeria.getId())
                                .name(pizzeria.getName())
                                .location(LocationDto.builder().name(pizzeria.getLocation().getName()).build())
                                .uuid(pizzeria.getUuid())
                                .build())
                        .cells(new ArrayList<>(List.of(cellDto)))
                        .build());
            }
        }

        for (RowDto rowDto : rowDtos) {
            int sum = rowDto.getCells().stream().mapToInt(CellDto::getPoints).sum();
            int size = rowDto.getCells().size();
            rowDto.setAverageByRow(size > 0 ? sum / size : 0);
        }
        log.info("rows {}", rowDtos);

        List<String> allPizzeriaNames = new ArrayList<>();
        List<TableDto> tableDtos = new ArrayList<>();
        for (PizzeriaDto pizzeriaDto: pizzerias){
            allPizzeriaNames.add(pizzeriaDto.getName());
            List<RowDto> rowsByPizzeria = new ArrayList<>();
            for (RowDto rowDto : rowDtos) {
                if (pizzeriaDto.getName().equals(rowDto.getPizzeria().getName())) {
                    rowsByPizzeria.add(rowDto);
                }
            }
            if (!rowsByPizzeria.isEmpty()){
                tableDtos.add(TableDto.builder()

                        .pizzeria(pizzeriaDto.getName())
                        .rows(rowsByPizzeria)
                        .build());
            }
        }
        log.info("tables {}", tableDtos);
        List<String> pizzeriaNames = new ArrayList<>();

        for (String p : allPizzeriaNames) {
            for (RowDto rowDto : rowDtos) {
                if (p.equals(rowDto.getPizzeria().getName())) {
                    pizzeriaNames.add(rowDto.getPizzeria().getName());
                }
            }
        }

        for (TableDto tableDto : tableDtos) {
            int sum = tableDto.getRows().stream().mapToInt(RowDto::getAverageByRow).sum();
            int size = tableDto.getRows().size();
            tableDto.setAverageByTable(size > 0 ? sum / size : 0);
        }
        int totalSum = tableDtos.stream().mapToInt(TableDto::getAverageByTable).sum();
        int tableCount = tableDtos.size();
        statisticsDto.setAverage(tableCount > 0 ? totalSum / tableCount : 0);
        statisticsDto.setPizzerias(pizzeriaNames.stream().distinct().toList());
        statisticsDto.setDays(getDays(from, to));
        log.info("statistics average {}", statisticsDto.getAverage());
        statisticsDto.setTables(tableDtos);
        log.info("statistics {}", statisticsDto);
        return statisticsDto;
    }


    @Override
    public List<DayDto> getDays(LocalDate from, LocalDate to) {

        List<LocalDate> localDates = new ArrayList<>();
        LocalDate currentDate = from;

        while (!currentDate.isAfter(to)) {
            localDates.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<DayDto> dayDtos = new ArrayList<>();
        for (LocalDate localDate : localDates) {
            dayDtos.add(DayDto.builder()
                    .day(localDate.format(formatter))
                    .dayOfWeek(localDate.getDayOfWeek().name().toLowerCase())
                    .build());
        }
        return dayDtos;
    }

    @Override
    public void comment(String uuid, Long criteriaId, CommentDto commentDto) {
        log.info(uuid);
        log.info(criteriaId.toString());
        if (commentDto.getComment() != null) {
            log.info("comment {}", commentDto);
        }
        CheckList checkList = checkListRepository.findByUuidLink(uuid).orElseThrow(()-> new NoSuchElementException("Чеклист с uuid " + uuid + " не найден"));
        CheckListsCriteria checkListsCriteria = checkListCriteriaService.findByCriteriaIdAndChecklistId(criteriaId, checkList.getId());
        Comment comment = new Comment();
        if (commentDto.getCommentId()!= null){
            comment = commentService.findById(commentDto.getCommentId());
        }else {
            comment.setComment(commentDto.getComment());
            commentService.save(comment);
        }

        CheckListsCriteriaComment commentCriteria = CheckListsCriteriaComment.builder()
                .comment(comment)
                .checklistCriteria(checkListsCriteria)
                .build();
        checkListCriteriaCommentService.save(commentCriteria);
        log.info("commentCriteria {}",commentCriteria);
    }
    @Override
    @Transactional
    public void restore (String uuid) {
        checkListRepository.restore (uuid);
    }

    @Override
    public ChecklistShowDto getCheckListByIdIncludeDeleted(String checkListId) {
        return dtoBuilder.buildChecklistShowDto(checkListRepository.findDeleted(checkListId)
                .orElseThrow(() -> new NotFoundException("CheckList not found")));
    }

}
