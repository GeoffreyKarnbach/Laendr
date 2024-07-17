package at.ac.tuwien.sepm.groupphase.backend.datagenerator;

import at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext.ContextComputed;
import at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext.Exactly;
import at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext.Maybe;
import at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext.Node;
import at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext.OneOf;
import at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext.RandomComputed;
import at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext.Sequence;
import at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext.Shuffled;

public class LocationTemplate {

    private static final Node LOCATION_INNER_PREFIX = OneOf.of(
        "Neu!", "Preiswert!", "Günstig!", "Super!", "2023"
    );
    private static final Node LOCATION_PREFIX = OneOf.of(
        Sequence.of("(", LOCATION_INNER_PREFIX, ") "),
        Sequence.of("[", LOCATION_INNER_PREFIX, "] ")
    );
    private static final Node LOCATION_TYPE = Sequence.of(
        OneOf.of("Party", "Besprechungs", "Meeting", "Feier"),
        OneOf.of(
            Exactly.of("keller", "loctype", LocationDataGenerator.TestLocationType.LOCATION_CELLAR),
            Exactly.of("wiese", "loctype", LocationDataGenerator.TestLocationType.LOCATION_MEADOW),
            Exactly.of("zimmer", "loctype", LocationDataGenerator.TestLocationType.LOCATION_ROOM),
            Exactly.of("saal", "loctype", LocationDataGenerator.TestLocationType.LOCATION_HALL),
            Exactly.of("areal", "loctype", LocationDataGenerator.TestLocationType.LOCATION_AREA)
        )
    );
    private static final Node LOCATION_DESCRIPTOR = OneOf.of(
        Sequence.of(
            "in ",
            OneOf.of("angenehmer", "großartiger", "hervorragender", "perfekter"),
            " Lage"
        ),
        Sequence.of(
            "mit ",
            OneOf.of("ansprechender", "luxoriöser", "zweckdienlicher", "stylisher"),
            OneOf.of(" Lage", " Möblierung", " Umgebung")
        )
    );
    public static final Node LOCATION_TITLE = Sequence.of(
        Maybe.of(0.2, LOCATION_PREFIX, null),
        LOCATION_TYPE,
        " ",
        Maybe.of(0.7, LOCATION_DESCRIPTOR, null)
    );

    public static final Node ADDRESS = Sequence.of(
        OneOf.of("Stein", "Wiesen", "Wald", "Teich", "Berg", "Bahnhofs"),
        OneOf.of("strasse", "straße", "weg", "pfad", "gasse"),
        " ",
        RandomComputed.of((r) -> Integer.toString(r.nextInt(100) + 1)),
        Maybe.of(Sequence.of(
            "/",
            RandomComputed.of((r) -> Integer.toString(r.nextInt(100) + 1))
        )),
        Maybe.of(Sequence.of(
            "/",
            RandomComputed.of((r) -> Integer.toString(r.nextInt(100) + 1))
        ))
    );

    private static final Node LOCATION_DESCRIPTION_INTRO = Sequence.of(
        Maybe.of(
            0.4,
            ContextComputed.of("loctype", LocationDataGenerator.TestLocationType.LOCATION_UNKNOWN, LocationDataGenerator.TestLocationType::getDemonstrative),
            OneOf.of("Dieser Standort", "Diese Location", "Dieses Angebot")
        ),
        OneOf.of(
            Sequence.of(
                " ist unter den ",
                OneOf.of("besten", "tollsten", "großartigsten", "luxoriösesten"),
                OneOf.of(" in diesem Bundesland", " in meinem Angebot", " in Österreich", " in der ganzen Welt")
            ),
            Sequence.of(
                " ist sehr ",
                OneOf.of("begehrt", "beliebt", "gefragt", "zu empfehlen")
            )
        ),
        OneOf.of(".", "!", " :).", " :-)."),
        "\n"
    );
    private static final Node LOCATION_DESCRIPTION_PUBLIC_TRANSPORT = Sequence.of(
        OneOf.of(
            "Der Zugang zu öffentlichen Verkehrmitteln",
            "Die Anbindung an die Öffis",
            "Die Öffi-Anbindung",
            "Die Erreichbarkeit mit den Öffis"
        ),
        " ist ",
        OneOf.of(
            "ausgezeichnet", "sehr gut", "gut", "genügend",
            Sequence.of(
                OneOf.of("leider", "unglücklicherweise"),
                OneOf.of(" schlecht", " sehr schlecht", " nicht gegeben")
            )
        ),
        ".\n"
    );
    private static final Node LOCATION_DESCRIPTION_MESSAGE_REQUEST = Sequence.of(
        OneOf.of(
            "Bitte bei Buchung", "Bei Buchung bitte",
            "Bitte bei Interesse ", "Bei Interesse bitte",
            "Bitte vorab", "Vorab bitte"
        ),
        OneOf.of(" mitteilen ", " schreiben ", " bekanntgeben "),
        OneOf.of(
            "mit wievielen Leuten gerechnet wird",
            "wieviele Leute die Location nutzen werden",
            "für wieviele Leute Sie die Location nutzen wollen"
        ),
        ".\n"
    );
    private static final Node LOCATION_DESCRIPTION_OUTRO = Sequence.of(
        OneOf.of(
            "Bei Interesse",
            "Wenn alles gepasst hat",
            "Wenn das gut ausschaut"
        ),
        OneOf.of(
            " auf jeden Fall ",
            " bitte unbedingt ",
            " bitte ",
            " gerne auch "
        ),
        OneOf.of(
            "meine anderen Locations ",
            "meine anderen Angebote ",
            "mein restliches Profil "
        ),
        OneOf.of(
            "anschauen",
            "durchschauen",
            "durchstöbern"
        ),
        OneOf.of(
            ".",
            ". Danke sehr!",
            ". Vielen Dank!"
        )
    );
    public static final Node LOCATION_DESCRIPTION = Sequence.of(
        Shuffled.of(
            LOCATION_DESCRIPTION_INTRO,
            LOCATION_DESCRIPTION_PUBLIC_TRANSPORT,
            LOCATION_DESCRIPTION_MESSAGE_REQUEST
        ),
        LOCATION_DESCRIPTION_OUTRO
    );

}
