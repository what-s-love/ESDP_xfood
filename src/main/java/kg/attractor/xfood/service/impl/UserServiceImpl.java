package kg.attractor.xfood.service.impl;

import kg.attractor.xfood.AuthParams;
import kg.attractor.xfood.dto.auth.RegisterUserDto;
import kg.attractor.xfood.dto.expert.ExpertShowDto;
import kg.attractor.xfood.dto.user.UserDto;
import kg.attractor.xfood.enums.Role;
import kg.attractor.xfood.exception.NotFoundException;
import kg.attractor.xfood.model.User;
import kg.attractor.xfood.repository.UserRepository;
import kg.attractor.xfood.service.UserService;
import kg.attractor.xfood.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final DtoBuilder dtoBuilder;


    public void register(RegisterUserDto dto) {
        if (userRepository.getByEmail(dto.getEmail()).isPresent()) throw new IllegalArgumentException("User already exists");
        if (dto.getRole().isEmpty()) throw new NotFoundException("Role not found");
        try {
            Role role = Role.valueOf(dto.getRole().toUpperCase());
            String encodedPassword = encoder.encode(dto.getPassword());
            
            userRepository.saveUser(
                    dto.getName(),
                    dto.getSurname(),
                    dto.getEmail(),
                    encodedPassword,
                    dto.getTgLink(),
                    role.toString().toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            log.error("Invalid role: {}", dto.getRole());
        }
    }

    @Override
    public UserDto getUserDto() {
        return dtoBuilder.buildUserDto(
                userRepository.findByEmail(AuthParams.getPrincipal().getUsername())
                        .orElseThrow(() -> new NotFoundException("User not found"))
        );
    }

    public User getByEmail(String name) {
        return userRepository.getByEmail(name)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }


    @Override
    public List<UserDto> getAllExpertsDtos() {
        return userRepository.findByRole(Role.EXPERT.toString())
                .stream()
                .filter(e -> e.getEnabled().equals(true))
                .map(dtoBuilder::buildUserDto)
                .toList();
    }

    public List<User> getAllExperts() {
        return userRepository.findByRole(Role.EXPERT.toString());
    }

    @Override
    public List<ExpertShowDto> fetchAllExperts() {
        List<UserDto> users = getAllExpertsDtos();
        List<ExpertShowDto> expertDtos = users.stream()
                .map(user -> dtoBuilder.buildExpertShowDto(user))
                .collect(Collectors.toList());
        return expertDtos;
    }

    @Override
    public User findById(Long expertId) {
        return userRepository.findById(expertId).orElseThrow(() -> new NotFoundException("Expert not found"));
    }

    @Override
    public Page<UserDto> getAllUsers(String role, Pageable pageable, String word) {
        Page<User> users;
        if(!role.equals("default")) {
            Specification<User> spec = UserSpecification
                    .hasRole(Role.valueOf(role))
                    .and(UserSpecification.filterByQuery(word));
            users = userRepository.findAll(spec, pageable);
        } else {
            Specification<User> spec = UserSpecification.filterByQuery(word);
            users = userRepository.findAll(spec, pageable);
        }
        return new PageImpl<>(users.getContent().stream()
                .map(dtoBuilder::buildUserDto)
                .toList(), pageable, users.getTotalElements());
    }
    
    @Override
    public Boolean isUserExist(String email) {
        return userRepository.getByEmail(email).isPresent();
    }
    
    public List<User> findSupervisors() {
        return userRepository.findByRole(Role.SUPERVISOR.toString());
    }
}