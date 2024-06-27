package kg.attractor.xfood.controller.rest;

import kg.attractor.xfood.dao.CheckListDao;
import kg.attractor.xfood.enums.Status;
import kg.attractor.xfood.model.CheckList;
import kg.attractor.xfood.repository.CheckListRepository;
import kg.attractor.xfood.service.CheckListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("checkListControllerRest")
@RequiredArgsConstructor
@RequestMapping("api/checks")
public class CheckListController {
    private final CheckListService checkListService;
    private final CheckListDao checkListDao;

    @GetMapping("{id}")
    public ResponseEntity<String> getUuidLinkById(@PathVariable(name = "id") CheckList checkList) {
        try {
            String uuidLink = checkList.getUuidLink();
            return ResponseEntity.ok(uuidLink);
        } catch (Exception e) {
            System.err.println("Error retrieving UUID link: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving UUID link");
        }
    }


    @PostMapping("post/{id}")
    public HttpStatus postCheck(@PathVariable(name = "id") String id) {
        try{
            checkListService.updateCheckStatusCheckList(id);
            return HttpStatus.OK;
        } catch (IllegalArgumentException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @PostMapping("change/status/{id}")
    public HttpStatus changeStatusToInProgress(@PathVariable(name = "id") CheckList checkList) {
        checkListDao.updateStatus(Status.IN_PROGRESS, checkList);
        return HttpStatus.OK;
    }

}
