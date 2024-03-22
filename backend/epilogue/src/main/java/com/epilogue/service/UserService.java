package com.epilogue.service;

import com.epilogue.domain.user.User;
import com.epilogue.dto.request.user.JoinRequestDto;
import com.epilogue.dto.request.user.UpdateInfoRequestDto;
import com.epilogue.dto.request.user.UserStatusRepository;
import com.epilogue.dto.response.user.UserDTO;
import com.epilogue.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public void join(JoinRequestDto joinRequestDto) {
        User user = User.builder()
                .userId(joinRequestDto.getUserId())
                .password(bCryptPasswordEncoder.encode(joinRequestDto.getPassword()))
                .name(joinRequestDto.getName())
                .mobile(joinRequestDto.getMobile())
                .birth(joinRequestDto.getBirth())
                .build();

        userRepository.save(user);
    }

    public Boolean check(String userId) {
        return userRepository.existsByUserId(userId);
    }

    public void updatePassword(String loginUserId, String password) {
        userRepository.updatePassword(loginUserId, password);
    }

    public UserDTO userInfo(String loginUserId) {
        User findUser = userRepository.findByUserId(loginUserId);
        return new UserDTO(findUser);
    }

    public void updateUserInfo(String loginUserId, UpdateInfoRequestDto updateInfoRequestDto) {
        User findUser = userRepository.findByUserId(loginUserId);
        findUser.updateUserInfo(updateInfoRequestDto.getName(), updateInfoRequestDto.getMobile());
    }

    public void deleteMember(String loginUserId) {
        userRepository.delete(userRepository.findByUserId(loginUserId));
    }

}
