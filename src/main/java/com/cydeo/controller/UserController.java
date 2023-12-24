package com.cydeo.controller;

import com.cydeo.dto.wrapper.ResponseWrapper;
import com.cydeo.dto.UserDTO;
import com.cydeo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RolesAllowed("Admin")
    @PostMapping("/create")
    public ResponseEntity<ResponseWrapper> createUser(@Valid @RequestBody UserDTO userDTO) {

        UserDTO createdUser = userService.create(userDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseWrapper.builder()
                        .success(true)
                        .statusCode(HttpStatus.CREATED)
                        .message("User is successfully created.")
                        .data(createdUser)
                        .build());

    }

    @RolesAllowed("Admin")
    @GetMapping("/read/{userName}")
    public ResponseEntity<ResponseWrapper> getByUserName(@PathVariable("userName") String userName) {

        UserDTO foundUser = userService.readByUserName(userName);

        return ResponseEntity
                .ok(ResponseWrapper.builder()
                        .success(true)
                        .statusCode(HttpStatus.OK)
                        .message("User is successfully retrieved")
                        .data(foundUser)
                        .build());

    }

    @RolesAllowed("Admin")
    @GetMapping("/read/all")
    public ResponseEntity<ResponseWrapper> getUsers() {

        List<UserDTO> foundUsers = userService.readAllUsers();

        return ResponseEntity
                .ok(ResponseWrapper.builder()
                        .success(true)
                        .statusCode(HttpStatus.OK)
                        .message("Users are successfully retrieved.")
                        .data(foundUsers)
                        .build());

    }

    @RolesAllowed({"Admin", "Manager"})
    @GetMapping("/check/{userName}")
    public ResponseEntity<ResponseWrapper> checkByUserName(@PathVariable("userName") String userName) {

        boolean result = userService.checkByUserName(userName);

        return ResponseEntity
                .ok(ResponseWrapper.builder()
                        .success(true)
                        .statusCode(HttpStatus.OK)
                        .message("User exists.")
                        .data(result)
                        .build());

    }

    @RolesAllowed("Admin")
    @PutMapping("/update/{username}")
    public ResponseEntity<ResponseWrapper> updateUser(@PathVariable("username") String username, @Valid @RequestBody UserDTO userDTO) {

        UserDTO updatedUser = userService.update(username, userDTO);

        return ResponseEntity
                .ok(ResponseWrapper.builder()
                        .success(true)
                        .statusCode(HttpStatus.OK)
                        .message("User is successfully updated.")
                        .data(updatedUser)
                        .build());

    }

    @RolesAllowed("Admin")
    @DeleteMapping("/delete/{userName}")
    public ResponseEntity<Void> deleteUser(@PathVariable("userName") String userName) {
        userService.delete(userName);
        return ResponseEntity.noContent().build();
    }

}
