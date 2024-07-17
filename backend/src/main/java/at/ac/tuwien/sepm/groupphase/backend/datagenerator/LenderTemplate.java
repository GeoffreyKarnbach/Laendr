package at.ac.tuwien.sepm.groupphase.backend.datagenerator;

import at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext.Exactly;
import at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext.Maybe;
import at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext.Node;
import at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext.OneOf;
import at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext.Sequence;

public class LenderTemplate {

    private static final Node LENDER_DESCRIPTION_GREETING = Sequence.of(
        OneOf.of(
            "Hallo",
            "Willkommen",
            "Herzlich willkommen",
            "Danke für deinen Besuch"
        ),
        " auf ",
        OneOf.of(
            "meinem Profil",
            "meiner Seite",
            "dieser Seite"
        ),
        OneOf.of(".", "!", " :).", " :-)."),
        "\n"
    );
    private static final Node LENDER_DESCRIPTION_CONTACT = OneOf.of(
        Sequence.of(
            OneOf.of("Vor Kontaktaufnahme", "Vor einer Nachricht", "Vor einem Anruf"),
            " bitte ",
            Maybe.of(Exactly.of("zuerst ")),
            OneOf.of("einen Zeitslot buchen", "eine Location buchen")
        ),
        Sequence.of(
            "Kontaktdaten stehen ",
            OneOf.of("auf dieser Seite.", "auf der rechten Seite.", "hier angegeben."),
            Maybe.of(
                Sequence.of(
                    OneOf.of(" Telefon", " E-Mail", " Anruf", " Email"),
                    "bevorzugt."
                )
            ),
            "\n"
        )
    );
    public static Node LENDER_DESCRIPTION = Sequence.of(
        LENDER_DESCRIPTION_GREETING,
        LENDER_DESCRIPTION_CONTACT
    );
}
