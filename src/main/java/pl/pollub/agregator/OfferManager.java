/*
 * Copyright (C) 2026
 * by Lublin University of Technology.
 * All rights reserved.
 */
package pl.pollub.agregator;

import pl.pollub.agregator.entity.Offer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
 * Service responsible for reading, writing, and filtering job offers from XML and text files.
 */
public class OfferManager {

    private static final String ENCODING_UTF8 = "UTF-8";
    private static final String XML_VERSION = "1.0";
    private static final String TAG_OFFERS = "offers";
    private static final String TAG_OFFER = "offer";
    private static final String TAG_ID = "id";
    private static final String TAG_TITLE = "title";
    private static final String TAG_LOCATION = "location";
    private static final String INDENT_OFFER = "    ";
    private static final String INDENT_ELEMENT = "        ";
    private static final String NEW_LINE = "\n";
    private static final long INITIAL_AUTO_ID = 1L;

    /**
     * Creates an empty XML file containing the root offers element.
     *
     * @param filePath target XML file path
     */
    public void createEmptyXmlFile(String filePath) {
        saveOffersToXml(filePath, Collections.emptyList());
    }

    /**
     * Reads job offers from an XML file using StAX parser.
     *
     * @param filePath source XML file path
     * @return list of valid Offer entities
     */
    public List<Offer> readOffersFromXml(String filePath) {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = null;

        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            reader = inputFactory.createXMLStreamReader(fileInputStream, ENCODING_UTF8);
            List<Offer> offers = parseOffers(reader);
            System.out.println("Wczytano " + offers.size() + " poprawnych ofert z pliku " + filePath);
            return offers;
        } catch (IOException | XMLStreamException e) {
            System.err.println("Blad podczas odczytu pliku XML " + filePath + ": " + e.getMessage());
            return Collections.emptyList();
        } finally {
            closeReaderQuietly(reader);
        }
    }

    private List<Offer> parseOffers(XMLStreamReader reader) throws XMLStreamException {
        List<Offer> offers = new ArrayList<>();
        Long id = null;
        String title = null;
        String location = null;
        boolean insideOffer = false;
        long nextAutoId = INITIAL_AUTO_ID;

        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
                String elementName = reader.getLocalName();
                if (TAG_OFFER.equals(elementName)) {
                    insideOffer = true;
                    id = null;
                    title = null;
                    location = null;
                } else if (insideOffer) {
                    switch (elementName) {
                        case TAG_ID -> id = parseId(reader.getElementText());
                        case TAG_TITLE -> title = reader.getElementText();
                        case TAG_LOCATION -> location = reader.getElementText();
                        default -> { }
                    }
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if (TAG_OFFER.equals(reader.getLocalName()) && insideOffer) {
                    if (isValidOfferData(title, location)) {
                        Long resolvedId = (id != null) ? id : nextAutoId++;
                        offers.add(buildOffer(resolvedId, title, location));
                    } else {
                        System.out.println("Pominieto niepoprawna oferte podczas odczytu (brak wymaganego pola).");
                    }
                    insideOffer = false;
                }
            }
        }
        return offers;
    }

    private Long parseId(String rawId) {
        if (isBlank(rawId)) {
            return null;
        }
        return Long.parseLong(rawId.trim());
    }

    private boolean isValidOfferData(String title, String location) {
        return !isBlank(title) && !isBlank(location);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Offer buildOffer(Long id, String title, String location) {
        return Offer.builder()
                .id(id)
                .title(title.trim())
                .location(location.trim())
                .build();
    }

    /**
     * Saves the provided list of job offers to an XML file.
     *
     * @param filePath target XML file path
     * @param offers   list of Offer entities to persist
     */
    public void saveOffersToXml(String filePath, List<Offer> offers) {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = null;

        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            writer = outputFactory.createXMLStreamWriter(fileOutputStream, ENCODING_UTF8);
            writeOffersDocument(writer, offers);
            System.out.println("Zapisano " + offers.size() + " ofert do pliku " + filePath);
        } catch (IOException | XMLStreamException e) {
            System.err.println("Blad podczas zapisu do pliku XML " + filePath + ": " + e.getMessage());
        } finally {
            closeWriterQuietly(writer);
        }
    }

    private void writeOffersDocument(XMLStreamWriter writer, List<Offer> offers) throws XMLStreamException {
        writer.writeStartDocument(ENCODING_UTF8, XML_VERSION);
        writer.writeCharacters(NEW_LINE);
        writer.writeStartElement(TAG_OFFERS);
        writer.writeCharacters(NEW_LINE);

        for (Offer offer : offers) {
            writeSingleOffer(writer, offer);
        }

        writer.writeEndElement();
        writer.writeCharacters(NEW_LINE);
        writer.writeEndDocument();
        writer.flush();
    }

    private void writeSingleOffer(XMLStreamWriter writer, Offer offer) throws XMLStreamException {
        writer.writeCharacters(INDENT_OFFER);
        writer.writeStartElement(TAG_OFFER);
        writer.writeCharacters(NEW_LINE);

        if (offer.getId() != null) {
            writeXmlElement(writer, TAG_ID, String.valueOf(offer.getId()));
        }
        writeXmlElement(writer, TAG_TITLE, offer.getTitle());
        writeXmlElement(writer, TAG_LOCATION, offer.getLocation());

        writer.writeCharacters(INDENT_OFFER);
        writer.writeEndElement();
        writer.writeCharacters(NEW_LINE);
    }

    private void writeXmlElement(XMLStreamWriter writer, String name, String value) throws XMLStreamException {
        writer.writeCharacters(INDENT_ELEMENT);
        writer.writeStartElement(name);
        writer.writeCharacters(value != null ? value : "");
        writer.writeEndElement();
        writer.writeCharacters(NEW_LINE);
    }

    private void closeReaderQuietly(XMLStreamReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (XMLStreamException e) {
                System.err.println("Blad podczas zamykania strumienia odczytu XML: " + e.getMessage());
            }
        }
    }

    private void closeWriterQuietly(XMLStreamWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (XMLStreamException e) {
                System.err.println("Blad podczas zamykania strumienia zapisu XML: " + e.getMessage());
            }
        }
    }

    public void appendOffer(String filePath, String offer) {
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.write(offer + System.lineSeparator());
            System.out.println("Dodano oferte: \"" + offer + "\"");
        } catch (IOException e) {
            System.err.println("Wystapil blad podczas zapisu do pliku " + filePath + ": " + e.getMessage());
        }
    }

    public List<String> readOffers(String filePath) {
        List<String> offers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    offers.add(trimmedLine);
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
                .filter(offer -> containsKeywordIgnoreCase(offer, keyword))
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

    /**
     * Filters and sorts non-empty lines from the given text file that match the keyword.
     *
     * @param filePath source text file path
     * @param keyword  search phrase (case-insensitive)
     * @return sorted list of matching offer lines
     */
    public List<String> filterAndSortFromFile(String filePath, String keyword) {
        Path path = Paths.get(filePath);
        try (Stream<String> lines = Files.lines(path)) {
            return lines
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(line -> containsKeywordIgnoreCase(line, keyword))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Blad podczas odczytu z pliku " + filePath + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private boolean containsKeywordIgnoreCase(String text, String keyword) {
        return text != null && keyword != null && text.toLowerCase().contains(keyword.toLowerCase());
    }
}
