package vn.fptu.reasbe.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.fptu.reasbe.model.constant.AppConstants;
import vn.fptu.reasbe.model.dto.auth.GoogleSignUpDto;
import vn.fptu.reasbe.model.dto.auth.JWTAuthResponse;
import vn.fptu.reasbe.model.dto.auth.LoginDto;
import vn.fptu.reasbe.model.dto.auth.SignupDto;
import vn.fptu.reasbe.model.entity.Role;
import vn.fptu.reasbe.model.entity.Token;
import vn.fptu.reasbe.model.entity.User;
import vn.fptu.reasbe.model.enums.core.StatusEntity;
import vn.fptu.reasbe.model.enums.user.RoleName;
import vn.fptu.reasbe.model.enums.user.StatusOnline;
import vn.fptu.reasbe.model.exception.ReasApiException;
import vn.fptu.reasbe.model.exception.ResourceNotFoundException;
import vn.fptu.reasbe.repository.RoleRepository;
import vn.fptu.reasbe.repository.TokenRepository;
import vn.fptu.reasbe.repository.UserRepository;
import vn.fptu.reasbe.security.JwtTokenProvider;
import vn.fptu.reasbe.service.AuthService;
import vn.fptu.reasbe.service.EmailService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import vn.fptu.reasbe.service.OtpService;
import vn.fptu.reasbe.service.mongodb.UserMService;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserMService userMService;
    private final OtpService otpService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public JWTAuthResponse authenticateUser(LoginDto dto) {
        User user = userRepository.findByUserNameOrEmailOrPhone(
                dto.getUserNameOrEmailOrPhone(),
                dto.getUserNameOrEmailOrPhone(),
                dto.getUserNameOrEmailOrPhone()
        ).orElseThrow(() -> new BadCredentialsException("error.emailNotExist"));

        if (user.getStatusEntity().equals(StatusEntity.INACTIVE)) {
            throw new ReasApiException(HttpStatus.FORBIDDEN, "error.userInactive");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUserNameOrEmailOrPhone(), dto.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtTokenProvider.generateAccessToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);

            revokeAllTokenByUser(user);
            saveUserToken(accessToken, refreshToken, user);

//            if (dto.getRegistrationTokens() != null && !dto.getRegistrationTokens().isEmpty()) {
            // save user registration tokens to mongodb
            vn.fptu.reasbe.model.mongodb.User userM = userMService.findByRefId(user.getId());
            if (userM != null) {
                userM.setRegistrationTokens(dto.getRegistrationTokens());
                userMService.saveUser(userM);
            } else {
                saveNewUserToMongoDb(dto.getRegistrationTokens(), user);
            }
//            }

            return new JWTAuthResponse(accessToken, refreshToken, user.getRole().getName());

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("error.incorrectPassword");
        }
    }

    @Override
    public String validateAndSendOtp(SignupDto dto) {
        validateUser(dto);
        return otpService.generateAndSendOtp(dto.getEmail(), dto.getFullName());
    }

    @Override
    public JWTAuthResponse signupVerifiedUser(SignupDto request) {
        Role userRole = roleRepository.findByName(RoleName.ROLE_RESIDENT)
                .orElseThrow(() -> new ReasApiException(HttpStatus.BAD_REQUEST, "error.roleNotExist"));
        validateUser(request);
        User user = prepareUserData(request);
        user.setRole(userRole);
        user = userRepository.save(user);

        // save user to MongoDB
        saveNewUserToMongoDb(request.getRegistrationTokens(), user);

        // save user token
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        saveUserToken(accessToken, refreshToken, user);

        // send email to user
        sendMailToUser(user);
        return new JWTAuthResponse(accessToken, refreshToken, user.getRole().getName());
    }

    private void saveNewUserToMongoDb(List<String> registrationTokens, User user) {
        vn.fptu.reasbe.model.mongodb.User userM = vn.fptu.reasbe.model.mongodb.User.builder()
                .refId(user.getId())
                .userName(user.getUserName())
                .fullName(user.getFullName())
                .statusOnline(StatusOnline.OFFLINE)
                .registrationTokens(registrationTokens)
                .build();
        userMService.saveUser(userM);
    }

    @Override
    public JWTAuthResponse authenticateGoogleUser(GoogleSignUpDto googleSignUpDto) {
        String password = "Google:" + googleSignUpDto.getGoogleId();
        String username = googleSignUpDto.getEmail().substring(0, googleSignUpDto.getEmail().indexOf("@"));

        Optional<User> existingUser = userRepository.findByEmail(googleSignUpDto.getEmail());

        Role userRole = roleRepository.findByName(RoleName.ROLE_RESIDENT)
                .orElseThrow(() -> new ReasApiException(HttpStatus.BAD_REQUEST, "error.roleNotExist"));

        User ggUser;

        if (existingUser.isPresent()) {
            ggUser = existingUser.get();
        } else {
            User newUser = User.builder()
                    .email(googleSignUpDto.getEmail())
                    .fullName(googleSignUpDto.getFullName())
                    .userName(username)
                    .password(passwordEncoder.encode(password))
                    .image(googleSignUpDto.getPhotoUrl())
                    .googleAccountId(googleSignUpDto.getGoogleId())
                    .role(userRole)
                    .isFirstLogin(true)
                    .build();

            ggUser = userRepository.save(newUser);
        }

        return authenticateUser(new LoginDto(ggUser.getEmail(), password, googleSignUpDto.getRegistrationTokens()));
    }

    private void sendMailToUser(User user) {
        String content = "<html>" +
                "<head>" +
                "<style>" +
                "table { width: 100%; border-collapse: collapse; }" +
                "th, td { padding: 10px; border: 1px solid #ddd; text-align: left; }" +
                "th { background-color: #f2f2f2; }" +
                "body { font-family: Arial, sans-serif; }" +
                ".highlight { font-weight: bold; color: red; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<p class='greeting'>Hi, " + user.getFullName() + ",</p>" +
                "<p>Your REAS account has been successfully created.</p>" +
                "<p>Please log into the system with the following information:</p>" +
                "<table>" + "<tr><th>Username</th><td>" + user.getEmail() + "</td></tr>" +
                "</table>" +
                "</body>" +
                "</html>";
        emailService.sendEmail(user.getEmail(), "[REAS] - Account Successfully Created", content);
    }

    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return null;
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    @Override
    public ResponseEntity<JWTAuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(AppConstants.AUTH_VALUE_PREFIX)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // get user from token
        String token = authHeader.substring(7);
        String username = jwtTokenProvider.getUsernameFromJwt(token);
        User user = userRepository.findByUserNameOrEmailOrPhone(username, username, username).orElseThrow(() -> new RuntimeException("error.userNotFound"));

        // check if the token is valid then generate new token
        if (jwtTokenProvider.isValidRefreshToken(token, user.getUserName())) {
            String accessToken = jwtTokenProvider.generateAccessToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);

            revokeAllTokenByUser(user);
            saveUserToken(accessToken, refreshToken, user);

            return new ResponseEntity<>(new JWTAuthResponse(accessToken, refreshToken, user.getRole().getName()), HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserNameOrEmailOrPhone(username, username, username).orElseThrow(() -> new ReasApiException(HttpStatus.NOT_FOUND, "error.userNotFound"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.oldPasswordNotMatch");
        }
        if (!newPassword.matches(AppConstants.PASSWORD_REGEX))
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.passwordNotMatchRegex");
        user.setPassword(passwordEncoder.encode(newPassword));
        if (user.isFirstLogin()) user.setFirstLogin(false);
        userRepository.save(user);
    }

    private void validateUser(SignupDto dto) {
        if (Boolean.TRUE.equals(userRepository.existsByEmailAndStatusEntityEquals(dto.getEmail(), StatusEntity.ACTIVE))) {
            throw new ReasApiException(HttpStatus.BAD_REQUEST, "error.emailAlreadyExist");
        }
    }

    private User prepareUserData(SignupDto signupDto) {
        return User.builder()
                .email(signupDto.getEmail())
                .userName(signupDto.getEmail().substring(0, signupDto.getEmail().indexOf("@")))
                .fullName(signupDto.getFullName())
                .password(passwordEncoder.encode(signupDto.getPassword()))
                .image(null)
                .isFirstLogin(true)
                .build();
    }

    private void saveUserToken(String accessToken, String refreshToken, User user) {
        Token token = Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .loggedOut(false)
                .user(user)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllTokenByUser(User user) {
        List<Token> validTokens = tokenRepository.findAllByUserId(user.getId());
        validTokens.forEach(t -> t.setLoggedOut(true));
        tokenRepository.saveAll(validTokens);
    }
}
