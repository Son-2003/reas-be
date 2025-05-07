package vn.fptu.reasbe.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.fptu.reasbe.model.constant.AppConstants;
import vn.fptu.reasbe.model.enums.user.Gender;

/**
 *
 * @author ntig
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateStaffRequest {

    @NotNull(message = "Id cannot be null")
    Integer id;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 2, message = "Username must have at least 2 characters")
    String userName;

    @NotBlank(message = "Full name cannot be blank")
    @Size(min = 2, message = "Full name must have at least 2 characters")
    String fullName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email is not valid")
    @Pattern(regexp = AppConstants.EMAIL_REGEX, message = "Email is invalid!")
    String email;

    @NotBlank(message = "Phone cannot be blank")
    @Pattern(regexp = AppConstants.PHONE_REGEX, message = "Invalid phone number!")
    String phone;

    @NotNull(message = "Gender cannot be blank")
    Gender gender;

    String image;

    @NotBlank(message = "Password cannot be blank")
    @Pattern(regexp = AppConstants.PASSWORD_REGEX, message = "Password must be at least 8 characters with at least one uppercase letter, one number, and one special character (!@#$%^&*).")
    String password;

    @NotBlank(message = "Confirm password cannot be blank")
    String confirmPassword;
}
