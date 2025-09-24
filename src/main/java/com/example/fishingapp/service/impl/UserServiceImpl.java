package com.example.fishingapp.service.impl;

import com.example.fishingapp.dto.UserCreateDto;
import com.example.fishingapp.dto.UserDto;
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
    public UserDto findById(String userName) {
        return null;
    }

    @Override
    public List<UserCreateDto> getAllUsers() {
        return List.of();
    }

    @Override
    public UserCreateDto updateUserCreateDto(UserCreateDto userCreateDto) {
        return null;
    }

    @Override
    public void deleteUser(String username) {

    }
}
