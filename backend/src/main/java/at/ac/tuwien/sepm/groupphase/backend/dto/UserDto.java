package at.ac.tuwien.sepm.groupphase.backend.dto;

import at.ac.tuwien.sepm.groupphase.backend.enums.AustriaState;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    @Nullable
    private Long id;
    private String email;
    private String name;
    private AustriaState state;
    @Nullable
    private PlzDto plz;
    private boolean isLocked;
    private int loginAttempts;
    private String renterPhone;
    private String renterEmail;
    private boolean isLender;
    private String lenderDescription;
    private String lenderPhone;
    private String lenderEmail;
    private CoordinateDto coordinates;
}
