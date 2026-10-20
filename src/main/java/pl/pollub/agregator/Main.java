package pl.pollub.agregator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String XML_FILE_PATH = "offers_db.xml";

    public static void main(String[] args) {
        OfferManager manager = new OfferManager();
        File xmlFile = new File(XML_FILE_PATH);
        List<Offer> offers;

        //Sprawdzenie czy plik XML istnieje
        if (!xmlFile.exists()) {
            System.out.println("Plik " + XML_FILE_PATH + " nie istnieje. Tworzenie nowego pliku...");
            manager.createEmptyXmlFile(XML_FILE_PATH);
            offers = new ArrayList<>();
        } else {
            System.out.println("Plik " + XML_FILE_PATH + " istnieje. Wczytywanie ofert...");
            offers = manager.readOffersFromXml(XML_FILE_PATH);
        }

        System.out.println("\nWczytane oferty (" + offers.size() + "):");
        offers.forEach(offer -> System.out.println(" - " + offer));

        //Dodanie 3 nowych ofert z poziomu programu
        System.out.println("\nDodawanie 3 nowych ofert...");
        offers.add(new Offer("DevOps Engineer", "Kubernetes, AWS, Docker", "Remote"));
        offers.add(new Offer("Python Data Engineer", "Python, Spark, SQL", "Krakow"));
        offers.add(new Offer("QA Automation Engineer", "Java, Selenium, Cypress", "Gdansk"));

        //Usuniecie jednej oferty z listy
        if (!offers.isEmpty()) {
            Offer removed = offers.remove(0);
            System.out.println("Usunieto oferte: " + removed);
        }

        System.out.println("\nAktualna lista ofert przed zapisem (" + offers.size() + "):");
        offers.forEach(offer -> System.out.println(" - " + offer));

        // Zapis zaktualizowanej listy z powrotem do pliku XML
        System.out.println("\nZapisywanie ofert do pliku " + XML_FILE_PATH + "...");
        manager.saveOffersToXml(XML_FILE_PATH, offers);
    }
}
