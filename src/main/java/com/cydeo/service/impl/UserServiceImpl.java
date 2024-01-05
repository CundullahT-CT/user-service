package com.cydeo.service.impl;

import com.cydeo.client.ProjectClient;
import com.cydeo.client.TaskClient;
import com.cydeo.dto.ProjectResponseDTO;
import com.cydeo.dto.TaskResponseDTO;
import com.cydeo.dto.UserDTO;
import com.cydeo.entity.User;
import com.cydeo.exception.*;
import com.cydeo.repository.UserRepository;
import com.cydeo.service.KeycloakService;
import com.cydeo.service.UserService;
import com.cydeo.util.MapperUtil;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ProjectClient projectClient;
    private final TaskClient taskClient;
    private final MapperUtil mapperUtil;
    private final KeycloakService keycloakService;

    public UserServiceImpl(UserRepository userRepository, ProjectClient projectClient, TaskClient taskClient,
                           MapperUtil mapperUtil, KeycloakService keycloakService) {
        this.userRepository = userRepository;
        this.projectClient = projectClient;
        this.taskClient = taskClient;
        this.mapperUtil = mapperUtil;
        this.keycloakService = keycloakService;
    }

    @Override
    public UserDTO create(UserDTO userDTO) {

        Optional<User> foundUser = userRepository.findByUserNameAndIsDeleted(userDTO.getUserName(), false);

        if (foundUser.isPresent()) {
            throw new UserAlreadyExistsException("User already exists.");
        }

        userDTO.setEnabled(true);

        User userToSave = mapperUtil.convert(userDTO, new User());

        keycloakService.userCreate(userDTO);
        User savedUser = userRepository.save(userToSave);

        return mapperUtil.convert(savedUser, new UserDTO());

    }

    @Override
    public UserDTO readByUserName(String username) {
        User foundUser = userRepository.findByUserNameAndIsDeleted(username, false)
                .orElseThrow(() -> new UserNotFoundException("User does not exist."));
        return mapperUtil.convert(foundUser, new UserDTO());
    }

    @Override
    public List<UserDTO> readAllUsers() {
        List<User> foundUsers = userRepository.findAllByIsDeleted(false, Sort.by("firstName"));
        return foundUsers.stream().map(user -> mapperUtil.convert(user, new UserDTO()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean checkByUserName(String username) {
        userRepository.findByUserNameAndIsDeleted(username, false)
                .orElseThrow(() -> new UserNotFoundException("User does not exist."));
        return true;
    }

    @Override
    public UserDTO update(String username, UserDTO userDTO) {

        User foundUser = userRepository.findByUserNameAndIsDeleted(username, false)
                .orElseThrow(() -> new UserNotFoundException("User does not exist."));

        userDTO.setUserName(username);
        userDTO.setEnabled(true);
        userDTO.setId(foundUser.getId());

        User userToUpdate = mapperUtil.convert(userDTO, new User());

        keycloakService.userUpdate(userDTO);
        User updatedUser = userRepository.save(userToUpdate);

        return mapperUtil.convert(updatedUser, new UserDTO());

    }

    @Override
    public void delete(String username) {

        User userToDelete = checkIfUserCanBeDeleted(username);

        userToDelete.setUserName(username + "-" + userToDelete.getId());
        userToDelete.setIsDeleted(true);

        keycloakService.delete(username);
        userRepository.save(userToDelete);

    }

    private User checkIfUserCanBeDeleted(String username) {

        User userToDelete = userRepository.findByUserNameAndIsDeleted(username, false)
                .orElseThrow(() -> new UserNotFoundException("User does not exist."));

        checkUserConnections(userToDelete);

        return userToDelete;

    }

    private void checkUserConnections(User userToDelete) {

        String role = userToDelete.getRole().getDescription();
        String username = userToDelete.getUserName();

        switch (role) {
            case "Manager":
                checkManagerConnections(username);
                break;
            case "Employee":
                checkEmployeeConnections(username);
                break;
        }

    }

    private void checkManagerConnections(String username) {

        Integer projectCount = 0;

        ResponseEntity<ProjectResponseDTO> projectResponse = projectClient.getNonCompletedCountByAssignedManager(getAccessToken(), username);

        if (Objects.requireNonNull(projectResponse.getBody()).isSuccess()) {
            projectCount = projectResponse.getBody().getData();
        } else {
            throw new ProjectCountNotRetrievedException("Project count cannot be retrieved.");
        }

        if (projectCount > 0) {
            throw new UserCanNotBeDeletedException("User can not be deleted. User is linked to a project(s)");
        }

    }

    private void checkEmployeeConnections(String username) {

        Integer taskCount = 0;

        ResponseEntity<TaskResponseDTO> taskResponse = taskClient.getNonCompletedCountByAssignedEmployee(getAccessToken(), username);

        if (Objects.requireNonNull(taskResponse.getBody()).isSuccess()) {
            taskCount = taskResponse.getBody().getData();
        } else {
            throw new TaskCountNotRetrievedException("Task count cannot be retrieved.");
        }

        if (taskCount > 0) {
            throw new UserCanNotBeDeletedException("User can not be deleted. User is linked to a task(s)");
        }

    }

    private String getAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        KeycloakAuthenticationToken keycloakAuthenticationToken = (KeycloakAuthenticationToken) authentication;
        return "Bearer " + keycloakAuthenticationToken.getAccount().getKeycloakSecurityContext().getTokenString();
    }

}
