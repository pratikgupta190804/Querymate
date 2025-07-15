package com.querymate.QueryMate.service;

import com.querymate.QueryMate.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto getUserById(Long userId);

    List<UserDto> getAllUsers();

    UserDto updateUser(Long userId, UserDto userDto);

    void deleteUser(Long userId);

    UserDto getUserByEmail(String email);
}
