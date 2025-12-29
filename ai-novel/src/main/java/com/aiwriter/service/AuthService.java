package com.aiwriter.service;

import com.aiwriter.dto.AuthResponse;
import com.aiwriter.dto.LoginRequest;
import com.aiwriter.dto.RegisterRequest;
import com.aiwriter.dto.UserDTO;
import com.aiwriter.entity.User;
import com.aiwriter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        // TODO: 实际项目中应该使用BCryptPasswordEncoder加密密码
        user.setPassword(request.getPassword());
        
        user = userRepository.save(user);
        log.info("新用户注册成功: {}", user.getUsername());
        
        // 生成token（简化版本，实际应使用JWT）
        String token = generateToken(user);
        
        UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), user.getEmail());
        return new AuthResponse(token, userDTO);
    }
    
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        
        // 验证密码（简化版本，实际应使用BCryptPasswordEncoder）
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        log.info("用户登录成功: {}", user.getUsername());
        
        // 生成token
        String token = generateToken(user);
        
        UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), user.getEmail());
        return new AuthResponse(token, userDTO);
    }
    
    private String generateToken(User user) {
        // 简化版token生成，实际应使用JWT
        return "token_" + user.getId() + "_" + UUID.randomUUID().toString();
    }
}
