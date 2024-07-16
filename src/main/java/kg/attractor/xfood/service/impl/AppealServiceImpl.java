package kg.attractor.xfood.service.impl;

import kg.attractor.xfood.dto.appeal.AppealDto;
import kg.attractor.xfood.dto.appeal.AppealListDto;
import kg.attractor.xfood.dto.appeal.CreateAppealDto;
import kg.attractor.xfood.dto.appeal.DataAppealDto;
import kg.attractor.xfood.model.Appeal;
import kg.attractor.xfood.model.CheckListsCriteria;
import kg.attractor.xfood.repository.AppealRepository;
import kg.attractor.xfood.service.AppealService;
import kg.attractor.xfood.service.CheckListCriteriaService;
import kg.attractor.xfood.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AppealServiceImpl implements AppealService {

    private final CheckListCriteriaService checkListCriteriaService;
    private final FileService fileService;
    private final AppealRepository appealRepository;
    private final DtoBuilder dtoBuilder;

    @Override
    public Long create(DataAppealDto dto) {
        CheckListsCriteria checkListsCriteria = checkListCriteriaService
                .findByCriteriaIdAndChecklistId(dto.getCriteriaId(), dto.getCheckListId());

        Appeal appeal = Appeal.builder()
                .checkListsCriteria(checkListsCriteria)
                .comment("")
                .email("")
                .fullName("")
                .build();

        return appealRepository.save(appeal).getId();
    }

    @Override
    public AppealDto findById(Long id) {
        Appeal appeal = appealRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Аппеляция не найдена"));
        return dtoBuilder.buildAppealDto(appeal);
    }

    @Override
    public void update(CreateAppealDto createAppealDto, Long id) {
        Appeal appeal = appealRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Аппеляция не найдена"));

        appeal.setEmail(createAppealDto.getEmail());
        appeal.setComment(createAppealDto.getComment());
        appeal.setFullName(createAppealDto.getFullName());
        appeal.setIsAccepted(false);
        appeal.setLinkToExternalSrc(createAppealDto.getLinkToExternalSrc());
        appeal.setTgLinkMessage(createAppealDto.getTgLinkMessage());
        appealRepository.save(appeal);

        List<MultipartFile> files = Arrays.asList(createAppealDto.getFiles());
        fileService.saveFiles(files, id);
    }

    @Override
    public Integer getAmountOfNewAppeals() {
        return appealRepository.countAllByIsAcceptedNull();
    }

    @Override
    public Page<AppealListDto> getAllByStatus(Boolean isAccepted, int page) {
        Page<Appeal> appeals = appealRepository.findAllByIsAccepted(isAccepted, PageRequest.of(page - 1, 6));
        return appeals.map(dtoBuilder::buildAppealsListDto);
    }
}
