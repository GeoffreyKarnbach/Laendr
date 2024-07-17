package at.ac.tuwien.sepm.groupphase.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AustriaState {
    EMPTY(""),
    W("Wien"),
    NOE("Niederösterreich"),
    OOE("Oberösterreich"),
    BGLD("Burgenland"),
    KTN("Kärnten"),
    SBG("Salzburg"),
    STMK("Steiermark"),
    T("Tirol"),
    VBG("Vorarlberg");

    private String name;
}
