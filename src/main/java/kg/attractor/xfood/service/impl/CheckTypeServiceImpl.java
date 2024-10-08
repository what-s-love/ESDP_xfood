package kg.attractor.xfood.service.impl;

import org.springframework.transaction.annotation.Transactional;
import kg.attractor.xfood.dto.CheckTypeShowDto;
import kg.attractor.xfood.dto.checktype.CheckTypeSupervisorViewDto;
import kg.attractor.xfood.model.CheckType;
import kg.attractor.xfood.model.CriteriaType;
import kg.attractor.xfood.repository.CheckTypeRepository;
import kg.attractor.xfood.repository.CriteriaTypeRepository;
import kg.attractor.xfood.service.CheckTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckTypeServiceImpl implements CheckTypeService {
    private final CheckTypeRepository checkTypeRepository;
    private final CriteriaTypeRepository criteriaTypeRepository;
    private final CheckTypeFeeServiceImpl checkTypeFeeService;
    private final DtoBuilder dtoBuilder;

    @Override
    public List<CheckTypeSupervisorViewDto> getTypes() {
        return checkTypeRepository.findAll().stream().map(dtoBuilder::buildCheckTypeShowDto).toList();
    }

    @Override
    public CheckType getById(Long checkTypeId) {
       return checkTypeRepository.findById(checkTypeId).orElseThrow(() -> new NoSuchElementException("No such checkTypeId: " + checkTypeId));
    }

    @Override
    public List<CheckTypeShowDto> getCheckTypes() {
        List<CheckType> types = checkTypeRepository.findByOrderByNameAsc();
        List<CheckTypeShowDto> dtos = new ArrayList<>();
        types.forEach(e -> dtos.add(CheckTypeShowDto.builder()
                .id(e.getId())
                .name(e.getName())
                .numsOfCriteria(e.getCriteriaTypes().stream()
                                .filter(criteriaType -> criteriaType.getCriteria().getSection() != null)
                                .filter(criteriaType -> criteriaType.getCriteria().getSection().getId().equals(3L))
                                .count())
                .totalValue(getTotalMaxValueByTypeId(e.getId()))
                .fee(checkTypeFeeService.getEnabledFeeByCheckTypeId(e.getId()))
                .build()));
        return dtos;
    }

    @Override
    public CheckType findByName(String type) {
       return   checkTypeRepository.findByName(type).orElseThrow(() -> new NoSuchElementException("No such checkType: " + type));
    }

    @Override
    public void save(CheckType checkType) {
        checkTypeRepository.save(checkType);
    }

    @Override
    public boolean existsByName(String value) {
        boolean b = checkTypeRepository.existsByName(value);
        log.info("existsByName: " + b);
        return b;
    }

    @Override
    public void deleteCriteriaType(CriteriaType criteriaType) {
        criteriaTypeRepository.delete(criteriaType);
        log.info("Deleted checkType: " + criteriaType);
    }

    @Override
    @Transactional
    public void deleteCheckType(Long id) {
        checkTypeRepository.deleteById(id);
    }

    public int getTotalMaxValueByTypeId(Long typeId) {
        List<CriteriaType> criterias = criteriaTypeRepository.findByType_IdAndCriteria_Section_IdOrderByType_NameAsc(typeId, 3L);
        return criterias.stream()
                .mapToInt(CriteriaType::getMaxValue)
                .sum();
    }

//    @Override
//    public Integer getTypeById(Long checkTypeId) {
////        return getById(checkTypeId).getTotalMaxValue();
////    }
}
