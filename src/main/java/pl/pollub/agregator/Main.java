package pl.pollub.agregator;

import pl.pollub.agregator.dto.CompanyDTO;
import pl.pollub.agregator.dto.OfferDTO;
import pl.pollub.agregator.dto.UserDTO;
import pl.pollub.agregator.entity.Company;
import pl.pollub.agregator.entity.Offer;
import pl.pollub.agregator.entity.User;

public class Main {

    public static void main(String[] args) {
        System.out.println("TWORZENIE ENCJI");

        Offer offer = Offer.builder()
                .id(1L)
                .title("Senior Java Developer")
                .location("Warsaw")
                .build();

        Company company = Company.builder()
                .id(10L)
                .name("TechCorp Solutions")
                .industry("IT & Software")
                .build();

        User user = User.builder()
                .id(100L)
                .username("jan_kowalski")
                .email("jan.kowalski@example.com")
                .build();

        System.out.println("Utworzona oferta: " + offer);
        System.out.println("Utworzona firma: " + company);
        System.out.println("Utworzony uzytkownik: " + user);

        System.out.println("\nTEST MECHANIZMU WYCOFYWANIA ZMIAN");

        //Utworzenie kopii zapasowej stanu poczatkowego oferty
        OfferDTO offerSnapshot = offer.toSnapshot();
        System.out.println("Zapisano snapshot oferty (DTO): " + offerSnapshot);

        //Celowa modyfikacja danych encji na bledne wartosci
        System.out.println("\nModyfikacja danych w encji Offer: ");
        offer.setTitle("HACKED");
        offer.setLocation("UNKNOWN");
        System.out.println("Stan oferty po blednej zmianie: " + offer);

        //Wycofanie zmian na podstawie zapisanego snapshotu
        System.out.println("\nWycofywanie zmian z kopii zapasowej:  ");
        offer.restoreFromSnapshot(offerSnapshot);
        System.out.println("Stan oferty po przywroceniu (rollback): " + offer);

//        CompanyDTO companySnapshot = company.toSnapshot();
//        company.setName("BLEDNA NAZWA");
//        company.restoreFromSnapshot(companySnapshot);
//
//        UserDTO userSnapshot = user.toSnapshot();
//        user.setEmail("bledny_email");
//        user.restoreFromSnapshot(userSnapshot);
    }
}
