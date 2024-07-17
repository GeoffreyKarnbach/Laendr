package at.ac.tuwien.sepm.groupphase.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CancelReason {

    NO_INTEREST("Kein Interesse"),
    LOCATION_REMOVED("Location entfernt"),
    USER_REMOVED("Anderer User gelöscht"),
    SCAM("Scam");

    private final String displayValue;
}
