package com.codewithmosh.store.users;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> getAllUser(String sortBy){
        if (!Set.of("name", "email").contains(sortBy))
            sortBy = "name";

        return userRepository.findAll(Sort.by(sortBy))
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto getUser(Long id){
        var user = findUser(id);

        return userMapper.toDto(user);
    }

    public UserDto registerUser(RegisterUserRequest request){
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException();
        }

        var user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);

        return userMapper.toDto(user);
    }

    public UserDto updateUser(Long id, UpdateUserRequest request){
        var user = findUser(id);
        userMapper.update(request, user);
        userRepository.save(user);

        return userMapper.toDto(user);
    }

    public void deleteUser(Long id){
        var user = findUser(id);
        userRepository.delete(user);
    }

    public void changePassword(Long id, ChangePasswordRequest request){
        var user = findUser(id);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AccessDeniedException("Password does not match");
        }

        user.setPassword(request.getNewPassword());
        userRepository.save(user);
    }

    private User findUser(Long id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }
}
