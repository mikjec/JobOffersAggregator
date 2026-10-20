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

public class OfferManager {

    public void appendOffer(String filePath, String offer) {
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.write(offer + System.lineSeparator());
            System.out.println("Dodano oferte: \"" + offer + "\"");
        } catch (IOException e) {
            System.err.println("Wystapil blad podczas zapisu do pliku: " + e.getMessage());
        }
    }

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
            System.out.println("Pomyslnie wczytano " + offers.size() + " ofert z pliku " + filePath);
        } catch (FileNotFoundException e) {
            System.err.println("Nie znaleziono pliku " + filePath + ": " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Blad odczytu z pliku " + filePath + ": " + e.getMessage());
        }

        return offers;
    }

    public List<String> filterOffers(List<String> offers, String keyword) {
        return offers.stream()
                .filter(offer -> offer.toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Map<String, Long> countOfferOccurrences(List<String> offers) {
        return offers.stream()
                .collect(Collectors.groupingBy(offer -> offer, Collectors.counting()));
    }

    public List<String> mapOffersToUpperCase(List<String> offers) {
        return offers.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }

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
            System.err.println("Blad podczas odczytu z pliku " + filePath + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }
}