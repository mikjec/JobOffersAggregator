package pl.pollub.agregator;

import java.util.List;
import java.util.Map;

public class Main {

    private static final String DB_FILE_PATH = "offers_db.txt";

    public static void main(String[] args) {
        OfferManager manager = new OfferManager();

        // Dodawanie nowych ofert do pliku
        System.out.println("\nDodawanie nowej oferty");
        String newOffer = "Cloud Solutions Architect (Java, GCP) - Warsaw";
        manager.appendOffer(DB_FILE_PATH, newOffer);

        // Wczytywanie wszystkich ofert z pliku
        System.out.println("\nWczytywanie wszystkich ofert");
        List<String> loadedOffers = manager.readOffers(DB_FILE_PATH);
        System.out.println("Wszystkie wczytane oferty (łącznie " + loadedOffers.size() + "):");
        loadedOffers.forEach(offer -> System.out.println(" - " + offer));

        // Filtrowanie ofert po słowie kluczowym Java
        String filterKeyword = "Java";
        System.out.println("\nOferty zawierajace słowo kluczowe \"" + filterKeyword + "\":");
        List<String> javaOffers = manager.filterOffers(loadedOffers, filterKeyword);
        javaOffers.forEach(offer -> System.out.println(" - " + offer));

        // Zliczanie wystąpien
        System.out.println("\nZliczanie ofert:");
        Map<String, Long> occurrences = manager.countOfferOccurrences(loadedOffers);
        occurrences.forEach((offer, count) ->
                System.out.printf("  (%d) %s%n", count, offer)
        );

        // Mapowanie nowego strumienia, zamiana na wielkie litery
        System.out.println("\nOferty po zmianie na wielkie litery:");
        List<String> upperCaseOffers = manager.mapOffersToUpperCase(javaOffers);
        upperCaseOffers.forEach(offer -> System.out.println(" " + offer));

        // Bezposredni odczyt, filtrowanie i sortowanie
        System.out.println("\nBezposrednie przetwarzanie pliku (Files.lines)");
        System.out.println("Posortowane oferty zawierajace \"Java\" wczytane prosto z pliku:");
        List<String> sortedJavaOffersFromFile = manager.readFilterAndSortDirectlyFromFile(DB_FILE_PATH, "Java");
        sortedJavaOffersFromFile.forEach(offer -> System.out.println(" - " + offer));

    }
}