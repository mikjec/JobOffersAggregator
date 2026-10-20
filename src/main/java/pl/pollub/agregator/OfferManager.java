package pl.pollub.agregator;

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

public class OfferManager {

    public void createEmptyXmlFile(String filePath) {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(fos, "UTF-8");
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeCharacters("\n");
            writer.writeStartElement("offers");
            writer.writeCharacters("\n");
            writer.writeEndElement();
            writer.writeCharacters("\n");
            writer.writeEndDocument();
            writer.flush();
            writer.close();
            System.out.println("Utworzono nowy plik XML: " + filePath);
        } catch (IOException | XMLStreamException e) {
            System.err.println("Blad podczas tworzenia pliku XML " + filePath + ": " + e.getMessage());
        }
    }

    public List<Offer> readOffersFromXml(String filePath) {
        List<Offer> offers = new ArrayList<>();
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        try (FileInputStream fis = new FileInputStream(filePath)) {
            XMLStreamReader reader = inputFactory.createXMLStreamReader(fis, "UTF-8");

            String title = null;
            String skills = null;
            String location = null;
            boolean insideOffer = false;

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT) {
                    String elementName = reader.getLocalName();
                    if ("offer".equals(elementName)) {
                        insideOffer = true;
                        title = null;
                        skills = null;
                        location = null;
                    } else if (insideOffer) {
                        switch (elementName) {
                            case "title" -> title = reader.getElementText();
                            case "skills" -> skills = reader.getElementText();
                            case "location" -> location = reader.getElementText();
                        }
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    if ("offer".equals(reader.getLocalName()) && insideOffer) {
                        try {
                            Offer offer = new Offer(title, skills, location);
                            offers.add(offer);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Pominieto niepoprawna oferte podczas odczytu: " + e.getMessage());
                        }
                        insideOffer = false;
                    }
                }
            }
            reader.close();
            System.out.println("Wczytano " + offers.size() + " poprawnych ofert z pliku " + filePath);
        } catch (IOException | XMLStreamException e) {
            System.err.println("Blad podczas odczytu pliku XML " + filePath + ": " + e.getMessage());
        }

        return offers;
    }

    public void saveOffersToXml(String filePath, List<Offer> offers) {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(fos, "UTF-8");
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeCharacters("\n");
            writer.writeStartElement("offers");
            writer.writeCharacters("\n");

            for (Offer offer : offers) {
                writer.writeCharacters("    ");
                writer.writeStartElement("offer");
                writer.writeCharacters("\n");

                writeXmlElement(writer, "title", offer.getTitle());
                writeXmlElement(writer, "skills", offer.getSkills());
                writeXmlElement(writer, "location", offer.getLocation());

                writer.writeCharacters("    ");
                writer.writeEndElement();
                writer.writeCharacters("\n");
            }

            writer.writeEndElement();
            writer.writeCharacters("\n");
            writer.writeEndDocument();
            writer.flush();
            writer.close();
            System.out.println("Zapisano " + offers.size() + " ofert do pliku " + filePath);
        } catch (IOException | XMLStreamException e) {
            System.err.println("Blad podczas zapisu do pliku XML " + filePath + ": " + e.getMessage());
        }
    }

    private void writeXmlElement(XMLStreamWriter writer, String name, String value) throws XMLStreamException {
        writer.writeCharacters("        ");
        writer.writeStartElement(name);
        writer.writeCharacters(value);
        writer.writeEndElement();
        writer.writeCharacters("\n");
    }

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
