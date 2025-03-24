package org.adhuc.library.website.catalog;

import java.util.List;
import java.util.UUID;

public final class BooksMother {

    public static final Book DU_CONTRAT_SOCIAL = new Book(
            "Du contrat social",
            List.of(new Author(UUID.fromString("83b5bf5d-b8bc-4ea7-82dd-51d7bd1af725"), "Jean-Jacques Rousseau")),
            "Du contrat social est un traité de philosophie politique présentant comment l’homme, passant de l’état de nature à l’état de société, peut instituer un ordre social au service de l'intérêt commun. Le pacte social que propose Rousseau établit que chacun doit renoncer à tous ses droits particuliers ou du plus fort pour obtenir l'égalité des droits que procure la société. Cette aliénation de chaque sujet de l’État est ce pacte qui offre à chacun l’égalité : « Les clauses [du pacte social] se réduisent toutes à une seule : l’aliénation totale de chaque associé avec tous ses droits à toute la communauté : car premièrement, chacun se donnant tout entier, la condition est égale pour tous ; et la condition étant égale pour tous, nul n’a intérêt de la rendre onéreuse aux autres. » (Livre I, Chapitre 6) La légitimité du pacte social repose sur le fait que l’homme n’aliène pas au sens propre (il ne l'échange pas ni ne le donne) son droit naturel mais il comprend que le pacte social est au contraire la condition de l’existence de ses droits naturels."
    );
    public static final Book LA_COMMUNAUTE_DE_L_ANNEAU = new Book(
            "La Communauté de l'Anneau",
            List.of(new Author(UUID.fromString("d8d94b64-603d-4fae-90dd-5296cde7fa2c"), "John Ronald Reuel Tolkien")),
            "La Fraternité de l'Anneau (The Fellowship of the Ring dans son titre original, La Communauté de l'Anneau dans la première traduction de Francis Ledoux) est la première partie du Seigneur des anneaux, roman en trois volumes de l'écrivain britannique J. R. R. Tolkien, parue le 29 juillet 1954 au Royaume-Uni. "
    );
    public static final Book A_DANCE_WITH_DRAGONS = new Book(
            "A Dance with Dragons",
            List.of(new Author(UUID.fromString("c5f1361f-61b4-42c4-9cf1-d82e9a45317a"), "George Raymond Richard Martin")),
            "A Dance with Dragons (littéralement « une danse avec les dragons ») est le cinquième livre de la saga Le Trône de fer écrite par George R. R. Martin. Le livre a été publié en version originale le 12 juillet 2011, alors que la publication de sa traduction française s'est étalée entre mars 2012 et janvier 2013. Il se concentre principalement sur les évènements se déroulant au nord du continent de Westeros et sur le continent oriental, les événements de la première moitié du roman se déroulant en même temps que ceux de A Feast for Crows alors que ceux de la deuxième moitié du roman se passent plus tard, certains cliffhangers d’A Feast for Crows trouvant leur résolution dans ce volume."
    );
    public static final Book BULLSHIT_JOBS = new Book(
            "Bullshit jobs",
            List.of(new Author(UUID.fromString("060d74f4-1d42-4087-b4ef-07dd00bb559c"), "David Graeber")),
            "L'étude de David Graeber sur les « Bullshit jobs » (expression en anglais américain signifiant « emplois à la con »), a été l'objet au départ d'un article de 2013, puis a été approfondie et publiée dans un ouvrage paru en 2018. Cette étude est largement médiatisée et suscite de nombreuses controverses sur sa pertinence. Les psychologues du travail ont repris le concept pour décrire une pathologie des travailleurs affectés à ce type de travail qui peut entraîner une « démission intérieure » encore appelée « brown-out »."
    );
    public static final Book HAMLET = new Book(
            "Hamlet",
            List.of(new Author(UUID.fromString("34100743-dcd0-45da-8142-8f4e650dd365"), "William Shakespeare")),
            "La Tragique histoire d'Hamlet, prince de Danemark (en anglais, The Tragedy of Hamlet, Prince of Denmark), plus couramment désigné sous le titre abrégé Hamlet, est la plus longue et l'une des plus célèbres pièces de William Shakespeare."
    );
    public static final Book LA_FERME_DES_ANIMAUX = new Book(
            "La Ferme des animaux",
            List.of(new Author(UUID.fromString("69831d56-5843-409b-9101-fc66bea8fc2f"), "George Orwell")),
            "La Ferme des animaux (titre original : Animal Farm. A Fairy Story) est un roman court de George Orwell, publié en 1945. Découpé en dix chapitres, il décrit une ferme dans laquelle les animaux se révoltent contre leur maître, prennent le pouvoir, chassent les hommes et vivent en autarcie."
    );

}
