package com.kamesan.erpapi.accounts.service;

import com.kamesan.erpapi.accounts.entity.User;
import com.kamesan.erpapi.accounts.repository.UserRepository;
import com.kamesan.erpapi.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * UserDetailsService 實作
 *
 * <p>實作 Spring Security 的 UserDetailsService 介面，
 * 用於載入使用者資訊進行認證。</p>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     * 使用者 Repository
     */
    private final UserRepository userRepository;

    /**
     * 根據使用者名稱載入使用者資訊
     *
     * @param username 使用者名稱
     * @return UserDetails
     * @throws UsernameNotFoundException 使用者不存在時拋出
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("使用者不存在: " + username));

        // 檢查使用者是否啟用
        if (!user.isActive()) {
            throw new UsernameNotFoundException("使用者已停用: " + username);
        }

        // 檢查使用者是否被鎖定
        if (user.isLocked()) {
            throw new UsernameNotFoundException("使用者已被鎖定: " + username);
        }

        // 取得角色
        String roleCode = user.getRole().getCode();

        return UserPrincipal.create(user, Collections.singletonList(roleCode));
    }

    /**
     * 根據使用者 ID 載入使用者資訊
     *
     * @param id 使用者 ID
     * @return UserDetails
     * @throws UsernameNotFoundException 使用者不存在時拋出
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("使用者不存在: ID=" + id));

        String roleCode = user.getRole().getCode();

        return UserPrincipal.create(user, Collections.singletonList(roleCode));
    }
}
