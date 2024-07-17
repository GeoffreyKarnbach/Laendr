package at.ac.tuwien.sepm.groupphase.backend.datagenerator;

import at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext.Node;
import at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext.OneOf;
import at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext.Sequence;

public class TransactionTemplate {

    public static final Node MESSAGE = Sequence.of(
        OneOf.of("Hallo", "Hi", "Guten Tag"),
        OneOf.of(". ", "! "),
        OneOf.of("Ich will", "Ich möchte", "Ich würde gerne"),
        " diese",
        OneOf.of("", " tolle", " großartige", "fantastische"),
        " Location buchen.",
        OneOf.of(" Vielen Dank.", " Danke sehr.", " Bitte melden.", "")
    );

    public static final Node REVIEW_GOOD = Sequence.of(
        OneOf.of("Ur ", "Voll ", "Total ", "Mega ", "Vollkommen "),
        OneOf.of("super", "geil", "perfekt"),
        OneOf.of(".", "!"),
        OneOf.of(" Wirklich", " Ohne irgendwas", " Definitiv"),
        OneOf.of("empfehlenswert", "sehr empfehlenswert", "weiter zu empfehlen"),
        OneOf.of("!", ".", " :)")
    );

    public static final Node REVIEW_MEDIUM = Sequence.of(
        OneOf.of("Eigentlich eh ", "Größtenteils ", "Im großen und ganzen ", "Insgesamt "),
        OneOf.of("nicht so schlecht", "ganz ok", "nicht komplett schlecht", "ganz nett"),
        ". Ich würde ",
        OneOf.of("nicht unbedingt", "jetzt nicht unbedingt", "eigentlich nicht"),
        " davon abraten."
    );

    public static final Node REVIEW_BAD = Sequence.of(
        OneOf.of("Ur ", "Volle ", "Totale ", "Mega "),
        OneOf.of("Zeitverschwendung", "Geldverschwendung", "Abklatsche", "Widerlichkeit"),
        OneOf.of("! ", " >:(. ", " :(. ", " >:-(. "),
        OneOf.of("Auf sowas", "Auf so einen Blödsinn", "Auf das", "Auf diesen Anbieter"),
        " falle ich ",
        OneOf.of("nicht mehr", "nie wieder", "niemals wieder"),
        " rein.",
        OneOf.of("", " >>:(", " >:(", " Pfui!", " :(")
    );

}
