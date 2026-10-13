package pl.pollub.agregator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service class responsible for managing job offer persistence and stream processing.
 */
public class OfferManager {

    /**
     * Appends a new job offer to the specified text file.
     * Uses java.io.FileWriter in append mode (true) wrapped in a try-catch block catching IOException.
     *
     * @param filePath the path to the database file
     * @param offer    the job offer text to append
     */
    public void appendOffer(String filePath, String offer) {
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.write(offer + System.lineSeparator());
            System.out.println("[OfferManager] Successfully appended offer: \"" + offer + "\" to " + filePath);
        } catch (IOException e) {
            System.err.println("[OfferManager] IOException while writing to file " + filePath + ": " + e.getMessage());
        }
    }

    /**
     * Reads all job offers line-by-line from the specified text file using java.io.BufferedReader in a while loop.
     * Explicitly catches FileNotFoundException and IOException.
     *
     * @param filePath the path to the database file
     * @return a List containing all read offers
     */
    public List<String> readOffers(String filePath) {
        List<String> offers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    offers.add(trimmed);
                }
            }
            System.out.println("[OfferManager] Successfully read " + offers.size() + " offers from " + filePath);
        } catch (FileNotFoundException e) {
            System.err.println("[OfferManager] FileNotFoundException: File not found at " + filePath + ": " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[OfferManager] IOException: Failed to read from file " + filePath + ": " + e.getMessage());
        }

        return offers;
    }

    /**
     * Filters a collection of job offers using Stream API by checking if they contain the specified keyword.
     *
     * @param offers  the source collection of job offers
     * @param keyword the substring to filter by (case-insensitive)
     * @return a filtered List of job offers
     */
    public List<String> filterOffers(List<String> offers, String keyword) {
        return offers.stream()
                .filter(offer -> offer.toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Counts occurrences of identical job offers in the collection using Collectors.groupingBy and Collectors.counting().
     *
     * @param offers the source collection of job offers
     * @return a Map where keys are job offers and values are occurrence counts
     */
    public Map<String, Long> countOfferOccurrences(List<String> offers) {
        return offers.stream()
                .collect(Collectors.groupingBy(offer -> offer, Collectors.counting()));
    }

    /**
     * Maps job offers to uppercase using a new Stream.
     *
     * @param offers the source collection of job offers
     * @return a new List containing all offers converted to uppercase
     */
    public List<String> mapOffersToUpperCase(List<String> offers) {
        return offers.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }

    /**
     * Reads lines directly from a file, filters them by a keyword, and sorts them alphabetically
     * using java.nio.file.Files.lines().
     *
     * @param filePath the path to the database file
     * @param keyword  the keyword to filter lines by (case-insensitive)
     * @return a sorted and filtered List of lines
     */
    public List<String> readFilterAndSortDirectlyFromFile(String filePath, String keyword) {
        Path path = Paths.get(filePath);
        try (Stream<String> lines = Files.lines(path)) {
            return lines
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(line -> line.toLowerCase().contains(keyword.toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("[OfferManager] IOException reading lines via Files.lines() from " + filePath + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
