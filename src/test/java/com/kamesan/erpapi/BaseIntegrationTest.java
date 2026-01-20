package com.kamesan.erpapi;

import com.kamesan.erpapi.accounts.dto.LoginRequest;
import com.kamesan.erpapi.accounts.entity.Role;
import com.kamesan.erpapi.accounts.entity.Store;
import com.kamesan.erpapi.accounts.entity.User;
import com.kamesan.erpapi.accounts.repository.RoleRepository;
import com.kamesan.erpapi.accounts.repository.StoreRepository;
import com.kamesan.erpapi.accounts.repository.UserRepository;
import com.kamesan.erpapi.security.JwtTokenProvider;
import com.kamesan.erpapi.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * 整合測試基礎類別
 *
 * <p>提供所有整合測試的共用配置和工具方法：</p>
 * <ul>
 *   <li>MockMvc - 模擬 HTTP 請求</li>
 *   <li>ObjectMapper - JSON 序列化/反序列化</li>
 *   <li>測試資料初始化方法</li>
 *   <li>JWT Token 生成方法</li>
 * </ul>
 *
 * @author ERP System Team
 * @version 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    /**
     * MockMvc - 用於模擬 HTTP 請求
     */
    @Autowired
    protected MockMvc mockMvc;

    /**
     * ObjectMapper - 用於 JSON 處理
     */
    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * JWT Token 提供者
     */
    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    /**
     * 密碼編碼器
     */
    @Autowired
    protected PasswordEncoder passwordEncoder;

    /**
     * 使用者 Repository
     */
    @Autowired
    protected UserRepository userRepository;

    /**
     * 角色 Repository
     */
    @Autowired
    protected RoleRepository roleRepository;

    /**
     * 門市 Repository
     */
    @Autowired
    protected StoreRepository storeRepository;

    /**
     * 測試用管理員使用者
     */
    protected User testAdminUser;

    /**
     * 測試用一般使用者
     */
    protected User testNormalUser;

    /**
     * 測試用角色
     */
    protected Role testAdminRole;
    protected Role testUserRole;

    /**
     * 測試用門市
     */
    protected Store testStore;

    /**
     * 測試用密碼（明文）
     */
    protected static final String TEST_PASSWORD = "password123";

    /**
     * 在每個測試前初始化測試資料
     */
    @BeforeEach
    void setUpTestData() {
        // 初始化基礎測試資料
        initTestRoles();
        initTestStores();
        initTestUsers();
    }

    /**
     * 初始化測試角色
     */
    protected void initTestRoles() {
        // 建立管理員角色
        testAdminRole = roleRepository.findByCode("ADMIN").orElseGet(() -> {
            Role role = new Role();
            role.setCode("ADMIN");
            role.setName("系統管理員");
            role.setDescription("擁有所有權限");
            role.setActive(true);
            return roleRepository.save(role);
        });

        // 建立一般使用者角色
        testUserRole = roleRepository.findByCode("CASHIER").orElseGet(() -> {
            Role role = new Role();
            role.setCode("CASHIER");
            role.setName("收銀員");
            role.setDescription("收銀台操作權限");
            role.setActive(true);
            return roleRepository.save(role);
        });
    }

    /**
     * 初始化測試門市
     */
    protected void initTestStores() {
        testStore = storeRepository.findByCode("TEST001").orElseGet(() -> {
            Store store = new Store();
            store.setCode("TEST001");
            store.setName("測試門市");
            store.setAddress("台北市測試區測試路1號");
            store.setPhone("02-1234-5678");
            store.setType(Store.StoreType.STORE);
            store.setActive(true);
            return storeRepository.save(store);
        });
    }

    /**
     * 初始化測試使用者
     */
    protected void initTestUsers() {
        // 建立管理員使用者
        testAdminUser = userRepository.findByUsername("testadmin").orElseGet(() -> {
            User user = new User();
            user.setUsername("testadmin");
            user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
            user.setName("測試管理員");
            user.setEmail("testadmin@test.com");
            user.setPhone("0912-111-111");
            user.setRole(testAdminRole);
            user.setActive(true);

            Set<Store> stores = new HashSet<>();
            stores.add(testStore);
            user.setStores(stores);

            return userRepository.save(user);
        });

        // 建立一般使用者
        testNormalUser = userRepository.findByUsername("testuser").orElseGet(() -> {
            User user = new User();
            user.setUsername("testuser");
            user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
            user.setName("測試使用者");
            user.setEmail("testuser@test.com");
            user.setPhone("0923-222-222");
            user.setRole(testUserRole);
            user.setActive(true);

            Set<Store> stores = new HashSet<>();
            stores.add(testStore);
            user.setStores(stores);

            return userRepository.save(user);
        });
    }

    /**
     * 生成管理員 JWT Token
     *
     * @return JWT Token 字串
     */
    protected String generateAdminToken() {
        UserPrincipal userPrincipal = UserPrincipal.create(
                testAdminUser,
                java.util.List.of(testAdminRole.getCode())
        );
        return jwtTokenProvider.generateToken(userPrincipal);
    }

    /**
     * 生成一般使用者 JWT Token
     *
     * @return JWT Token 字串
     */
    protected String generateUserToken() {
        UserPrincipal userPrincipal = UserPrincipal.create(
                testNormalUser,
                java.util.List.of(testUserRole.getCode())
        );
        return jwtTokenProvider.generateToken(userPrincipal);
    }

    /**
     * 生成 Authorization Header 值
     *
     * @param token JWT Token
     * @return Bearer Token 字串
     */
    protected String bearerToken(String token) {
        return "Bearer " + token;
    }

    /**
     * 建立登入請求物件
     *
     * @param username 使用者名稱
     * @param password 密碼
     * @return 登入請求物件
     */
    protected LoginRequest createLoginRequest(String username, String password) {
        return LoginRequest.builder()
                .username(username)
                .password(password)
                .build();
    }

    /**
     * 將物件轉換為 JSON 字串
     *
     * @param obj 要轉換的物件
     * @return JSON 字串
     * @throws Exception 轉換失敗時拋出
     */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
