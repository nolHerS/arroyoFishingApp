package com.example.fishingapp.controller;

import com.example.fishingapp.dto.UserDto;
import com.example.fishingapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDto> createUserDto(@RequestBody UserDto userDto){
        return new ResponseEntity<>(userService.createUser(userDto), HttpStatus.CREATED);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> findUserByUsername(@PathVariable String username){
        return new ResponseEntity<>(userService.findByUsername(username),HttpStatus.OK);
    }

    @GetMapping("/id/{idUser}")
    public ResponseEntity<UserDto> findUserById(@PathVariable Long idUser){
        return new ResponseEntity<>(userService.findById(idUser),HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> findAllUsers(){
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<UserDto> updateUser(UserDto userDto){
        return new ResponseEntity<>(userService.updateUserDto(userDto),HttpStatus.OK);
    }

    @DeleteMapping("/{usernameUser}")
    public ResponseEntity<String> deleteUser(@PathVariable String usernameUser){
        UserDto userDto = userService.findByUsername(usernameUser);
        userService.deleteUser(usernameUser);
        return new ResponseEntity<>("Usuario: "+userDto.toString()+" borrado.",HttpStatus.OK);
    }
}
