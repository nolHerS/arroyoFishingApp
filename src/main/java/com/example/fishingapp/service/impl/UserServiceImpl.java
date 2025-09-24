package com.example.fishingapp.service.impl;

import com.example.fishingapp.dto.UserCreateDto;
import com.example.fishingapp.dto.UserDto;
import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.exception.UsernameAlreadyExistsException;
import com.example.fishingapp.mapper.UserMapper;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.UserRepository;
import com.example.fishingapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    private UserRepository userRepository;

    public UserServiceImpl(UserMapper userMapper, UserRepository userRepository) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    @Override
    public UserCreateDto createUser(UserCreateDto userCreateDto) {

        Optional<User> optionalUser = userRepository.findByUsername(userCreateDto.username());

        if(optionalUser.isPresent()){
            throw new UsernameAlreadyExistsException("Username Already Exists For User "+userCreateDto.username());
        }

        User user = userMapper.mapUserCreateDto(userCreateDto);

        User savedUser = userRepository.save(user);

        return userMapper.mapUserCreateDto(savedUser);
    }

    @Override
    public UserDto findByUsername(String userName) {

        return userRepository.findByUsername(userName).map(UserMapper::mapUserDto).orElseThrow(
                () -> new ResourceNotFoundException("User", "username", userName)
        );
    }

    @Override
    public List<UserCreateDto> getAllUsers() {

        return userRepository.findAll().stream().map(UserMapper::mapUserCreateDto).toList();
    }

    @Override
    public UserCreateDto updateUserCreateDto(UserCreateDto userCreateDto) {

        User existingUser = userRepository.findById(userCreateDto.id()).orElseThrow(() -> new ResourceNotFoundException(
                "user","id",userCreateDto.id().toString()
        ));

        Optional<User> existingUsername = userRepository.findByUsername(userCreateDto.username());

        if(existingUsername.isPresent()){
            throw new UsernameAlreadyExistsException("Username Already Exists For User "+userCreateDto.username());
        }

        existingUser.setFullName(userCreateDto.fullName());
        existingUser.setEmail(userCreateDto.email());
        existingUser.setUsername(userCreateDto.username());

        User updateUser = userRepository.save(existingUser);

        return UserMapper.mapUserCreateDto(updateUser);
    }

    @Override
    public void deleteUser(String username) {

        Optional<User> existingUsername = userRepository.findByUsername(username);

        userRepository.delete(existingUsername.orElseThrow(() -> new ResourceNotFoundException(
                "user","id",username
        )));


    }
}
