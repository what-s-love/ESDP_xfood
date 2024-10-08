package kg.attractor.xfood.service.impl;

import kg.attractor.xfood.exception.AppealNotFoundException;
import kg.attractor.xfood.model.Appeal;
import kg.attractor.xfood.model.CheckList;
import kg.attractor.xfood.model.File;
import kg.attractor.xfood.repository.AppealRepository;
import kg.attractor.xfood.repository.FileRepository;
import kg.attractor.xfood.service.FileService;
import kg.attractor.xfood.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
	
	private final AppealRepository appealRepository;
	private final FileRepository fileRepository;
	private final FileUtil fileUtil;
	
	@Override
	public void saveFiles(List<MultipartFile> files, Long appealId) {
		Appeal appeal = appealRepository.findById(appealId).orElseThrow(AppealNotFoundException :: new);
		List<MultipartFile> validFiles = validateAppealFiles(files);
		
		if (! validFiles.isEmpty()) {
			for (int i = 0; i < validFiles.size(); i++) {
				String extension = "." + getFileExtension(files.get(i).getOriginalFilename());
				String fileName = generateFileName(appeal, String.valueOf(i + 1));
				
				String path = fileUtil.saveUploadedFile(files.get(i), "appealFiles", fileName + extension);
				
				fileRepository.saveAndFlush(File.builder()
						.appeal(appeal)
						.path(path)
						.build());
				log.info("Saved file: {}", fileName);
			}
		} else log.warn("No valid files found");
	}
	
	@Override
	public List<String> getPathsForAppealFiles(Long appealId) {
		List<File> files = fileRepository.findByAppealId(appealId);
		List<String> paths = new ArrayList<>();
		files.forEach(e -> paths.add(e.getPath()));
		return paths;
	}
	
	@Override
	public ResponseEntity<InputStreamResource> getFile(String path) {
		return fileUtil.getFIle(path);
	}
	
	public List<MultipartFile> validateAppealFiles(List<MultipartFile> files) {
		final int MAX_IMAGE_SIZE = 3 * 1024 * 1024;
		final int MAX_VIDEO_SIZE = 25 * 1024 * 1024;
		final int MAX_PHOTO_COUNT = 5;
		final int MAX_VIDEO_COUNT = 4;
		
		int photoCount = 0, videoCount = 0;
	
		List<MultipartFile> validFiles = new ArrayList<>();
		
		for (MultipartFile file : files) {
			String contentType = file.getContentType();
			long fileSize = file.getSize();
			
			if (contentType == null) throw new IllegalArgumentException("Не удалось определить тип файла");
			
			if (contentType.startsWith("image/")) {
				if (fileSize > MAX_IMAGE_SIZE)
					throw new IllegalArgumentException("Фото превышает максимальный размер 3 МБ");
				if (photoCount >= MAX_PHOTO_COUNT)
					throw new IllegalArgumentException("Превышено максимальное количество фото (5)");
				
				photoCount++;
				
			} else if (contentType.startsWith("video/")) {
				
				if (fileSize > MAX_VIDEO_SIZE)
					throw new IllegalArgumentException("Видео превышает максимальный размер 25 МБ");
				if (videoCount >= MAX_VIDEO_COUNT)
					throw new IllegalArgumentException("Превышено максимальное количество видео (4)");
				
				videoCount++;
			}
			validFiles.add(file);
		}
		return validFiles;
	}
	
	public String generateFileName(Appeal appeal, String iterator) {
		/* (fileName) -> CheckListId_PizzeriaName_CriteriaId_SenderEmail_iterator */
		CheckList checkList = appeal.getCheckListsCriteria().getChecklist();
		String checkListId = checkList.getId().toString();
		String pizzeriaName = checkList.getWorkSchedule().getPizzeria().getName();
		String criteriaId = appeal.getCheckListsCriteria().getCriteria().getId().toString();
		String senderEmail = appeal.getEmail();
		return String.format("%s_%s_%s_%s_%s", checkListId, pizzeriaName, criteriaId, senderEmail, iterator);
	}
	
	private String getFileExtension(String fileName) {
		if (fileName == null || fileName.lastIndexOf(".") == - 1) {
			return "";
		}
		return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
	}
}
