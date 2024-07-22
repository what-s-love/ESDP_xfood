package kg.attractor.xfood.controller.mvc;

import kg.attractor.xfood.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/profile")
public class ProfileController {
    private final UserService userService;
    @GetMapping
    public String getProfile(Model model) {
        model.addAttribute("user", userService.getUserDto());
        return "profile/profile";
    }
}
