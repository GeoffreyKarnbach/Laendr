package at.ac.tuwien.sepm.groupphase.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpDto {
    @NotNull(message = "Username must not be null")
    private String username;

    @NotNull(message = "Email must not be null")
    @Email
    private String email;

    @NotNull(message = "Password must not be null")
    private String originalPassword;

    @NotNull(message = "Password must not be null")
    private String repeatedPassword;
}
