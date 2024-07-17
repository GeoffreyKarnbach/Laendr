package at.ac.tuwien.sepm.groupphase.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeUserPasswordDto {

    @NotNull
    private String currentPassword;
    @NotNull
    private String newPassword;
    @NotNull
    private String repeatedPassword;
}
