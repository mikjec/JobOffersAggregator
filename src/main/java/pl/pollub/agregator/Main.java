package pl.pollub.agregator;

import java.util.List;
import java.util.Map;

/**
 * Main application runner testing OfferManager functionality:
 * file I/O (FileWriter, BufferedReader), Stream processing, and Files.lines().
 */
public class Main {

    private static final String DB_FILE_PATH = "offers_db.txt";

    public static void main(String[] args) {
        OfferManager manager = new OfferManager();

        System.out.println("==================================================");
        System.out.println("       JOB OFFERS AGGREGATOR - LABORATORY DEMO     ");
        System.out.println("==================================================");

        // 1. Appending new offers to file using FileWriter (append mode = true)
        System.out.println("\n--- Step 1: Appending New Offer (FileWriter) ---");
        String newOffer = "Cloud Solutions Architect (Java, GCP) - Warsaw";
        manager.appendOffer(DB_FILE_PATH, newOffer);

        // 2. Reading all offers using BufferedReader (while loop)
        System.out.println("\n--- Step 2: Reading All Offers (BufferedReader) ---");
        List<String> loadedOffers = manager.readOffers(DB_FILE_PATH);
        System.out.println("All Loaded Offers (" + loadedOffers.size() + " total):");
        loadedOffers.forEach(offer -> System.out.println("  • " + offer));

        // 3. Stream API Operations on Collection
        System.out.println("\n--- Step 3: Stream API Operations on In-Memory List ---");

        // 3a. Filtering stream (keyword "Java")
        String filterKeyword = "Java";
        System.out.println("\n[3a] Offers containing keyword \"" + filterKeyword + "\":");
        List<String> javaOffers = manager.filterOffers(loadedOffers, filterKeyword);
        javaOffers.forEach(offer -> System.out.println("  ✓ " + offer));

        // 3b. Counting occurrences (groupingBy + counting)
        System.out.println("\n[3b] Offer occurrences count (groupingBy + counting):");
        Map<String, Long> occurrences = manager.countOfferOccurrences(loadedOffers);
        occurrences.forEach((offer, count) ->
                System.out.printf("  [%d x] %s%n", count, offer)
        );

        // 3c. Mapping to a new stream (uppercase)
        System.out.println("\n[3c] Transformed offers mapped to UPPERCASE:");
        List<String> upperCaseOffers = manager.mapOffersToUpperCase(javaOffers);
        upperCaseOffers.forEach(offer -> System.out.println("  ▲ " + offer));

        // 4. Direct reading, filtering, and sorting using java.nio.file.Files.lines()
        System.out.println("\n--- Step 4: Direct File Processing via Files.lines() ---");
        System.out.println("Sorted offers containing \"Java\" read directly from file:");
        List<String> sortedJavaOffersFromFile = manager.readFilterAndSortDirectlyFromFile(DB_FILE_PATH, "Java");
        sortedJavaOffersFromFile.forEach(offer -> System.out.println("  ➤ " + offer));

        System.out.println("\n==================================================");
        System.out.println("       DEMO COMPLETED SUCCESSFULLY               ");
        System.out.println("==================================================");
    }
}
