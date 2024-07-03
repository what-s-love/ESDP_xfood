package kg.attractor.xfood.service.impl;

import kg.attractor.xfood.dto.checklist_criteria.CheckListCriteriaDto;
import kg.attractor.xfood.dto.criteria.SaveCriteriaDto;
import kg.attractor.xfood.model.CheckList;
import kg.attractor.xfood.model.CheckListsCriteria;
import kg.attractor.xfood.model.Criteria;
import kg.attractor.xfood.repository.ChecklistCriteriaRepository;
import kg.attractor.xfood.repository.CriteriaRepository;
import kg.attractor.xfood.repository.SectionRepository;
import kg.attractor.xfood.repository.ZoneRepository;
import kg.attractor.xfood.service.CheckListCriteriaService;
import kg.attractor.xfood.service.CheckListService;
import kg.attractor.xfood.service.CriteriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckListCriteriaServiceImpl implements CheckListCriteriaService {
    private final ChecklistCriteriaRepository checkListCriteriaRepository;
    private final CriteriaService criteriaService;
    private  CheckListService checkListService;
    private final DtoBuilder dtoBuilder;
    private final CriteriaRepository criteriaRepository;
    private final SectionRepository sectionRepository;
    private final ZoneRepository zoneRepository;


    @Autowired
    public void setCheckListCriteriaService(CheckListService checkListService) {
        this.checkListService = checkListService;
    }


    @Override
    public void save(List<SaveCriteriaDto> saveCriteriaDto) {

        saveCriteriaDto.forEach(c -> {
            try {
                int maxValue = (c.getMaxValue() != null) ? c.getMaxValue() : 0;

                Long checkListId = checkListService.getModelCheckListById(c.getCheckListId()).getId();
                Long criteriaId = criteriaService.getCriteriaById(c.getCriteriaId()).getId();
                //CheckList checkList = checkListService.getModelCheckListById(checkListId);
                //checkList.setDuration();
                CheckListsCriteria optional = isPresentOptional(criteriaId, checkListId);

                if (optional != null) {
                    optional.setMaxValue(maxValue);
                    optional.setValue(c.getValue());

                    checkListCriteriaRepository.save(optional);
                } else {
                    CheckListsCriteria checkListsCriteria = CheckListsCriteria.builder()
                            .value(c.getValue())
                            .criteria(criteriaService.getCriteriaById(c.getCriteriaId()))
                            .checklist(checkListService.getModelCheckListById(c.getCheckListId()))
                            .maxValue(maxValue)
                            .build();

                    checkListCriteriaRepository.save(checkListsCriteria);
                }
            } catch (Exception e) {
                System.err.println("Error processing criteria: " + e.getMessage());
                e.printStackTrace();
            }
        });

    }

    @Override
    public void save(CheckListsCriteria checkListsCriteria) {
        checkListCriteriaRepository.save(checkListsCriteria);
        log.info("Saved checklist criteria: {}, {}, {}", checkListsCriteria.getCriteria(), checkListsCriteria.getMaxValue(), checkListsCriteria.getChecklist());
    }

    @Override
    public CheckListCriteriaDto createNewFactor(SaveCriteriaDto saveCriteriaDto) {
        CheckListsCriteria checkListsCriteria = isPresentOptional(saveCriteriaDto.getCriteriaId(), saveCriteriaDto.getCheckListId());
        if (checkListsCriteria == null) {
            CheckListsCriteria criteria = CheckListsCriteria.builder()
                    .maxValue(0)
                    .checklist(checkListService.getModelCheckListById(saveCriteriaDto.getCheckListId()))
                    .criteria(criteriaService.getCriteriaById(saveCriteriaDto.getCriteriaId()))
                    .value(saveCriteriaDto.getValue())
                    .build();
            CheckListsCriteria model =  checkListCriteriaRepository.save(criteria);
            return dtoBuilder.buildCheckListCriteriaDto(model);
        }

        throw new IllegalArgumentException("Такой критерий уже существует! Вы можете добавить только один раз!");
    }

    @Override
    public void deleteFactor(Long id, Long checkListId) {
        CheckListsCriteria checkListsCriteria = isPresentOptional(id, checkListId);
        if(checkListsCriteria != null) checkListCriteriaRepository.delete(checkListsCriteria);
    }

    @Override
    public CheckListCriteriaDto createCritFactor(SaveCriteriaDto saveCriteriaDto, String description) {
        if(description.isEmpty()) throw new IllegalArgumentException("Описание не может быть пустым!");

        Criteria criteria = Criteria.builder()
                .description(description)
                .section(sectionRepository.findById(1L).get())
                .zone(zoneRepository.findById(9L).get())
                .coefficient(1)
                .build();

        Criteria newCriteria = criteriaRepository.save(criteria);
        saveCriteriaDto.setCriteriaId(newCriteria.getId());
        saveCriteriaDto.setValue(-8);

        return createNewFactor(saveCriteriaDto);
    }

    @Override
    public Integer getPercentageById(Long id) {
        List<CheckListsCriteria> criteriaList = checkListCriteriaRepository.findCriteriaByCheckListId(id);
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

        Double percentage = (normalValue / normalMaxSum) * 100;

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
    public List<CheckListsCriteria> findAllByChecklistId(Long id) {
        return checkListCriteriaRepository.findAllByChecklistId(id);
    }

    private CheckListsCriteria isPresentOptional(Long criteriaId, Long checkListId) {
        Optional<CheckListsCriteria> optional = checkListCriteriaRepository
                .findByCheckListIdAndCriteriaId(checkListId, criteriaId);

        return optional.orElse(null);
    }
}
