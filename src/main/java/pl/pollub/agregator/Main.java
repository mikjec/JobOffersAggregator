/*
 * Copyright (C) 2026
 * by Lublin University of Technology.
 * All rights reserved.
 */
package pl.pollub.agregator;

import pl.pollub.agregator.dto.OfferDTO;
import pl.pollub.agregator.entity.Company;
import pl.pollub.agregator.entity.Offer;
import pl.pollub.agregator.entity.User;

/**
 * Main application class demonstrating the Builder pattern and snapshot rollback mechanism.
 */
public class Main {

    private static final long DEFAULT_OFFER_ID = 1L;
    private static final long DEFAULT_COMPANY_ID = 10L;
    private static final long DEFAULT_USER_ID = 100L;
    private static final String INVALID_TITLE = "HACKED";
    private static final String INVALID_LOCATION = "UNKNOWN";

    public static void main(String[] args) {
        Offer offer = createSampleOffer();
        Company company = createSampleCompany();
        User user = createSampleUser();

        printCreatedEntities(offer, company, user);
        demonstrateSnapshotRollback(offer);
    }

    private static Offer createSampleOffer() {
        return Offer.builder()
                .id(DEFAULT_OFFER_ID)
                .title("Senior Java Developer")
                .location("Warsaw")
                .build();
    }

    private static Company createSampleCompany() {
        return Company.builder()
                .id(DEFAULT_COMPANY_ID)
                .name("TechCorp Solutions")
                .industry("IT & Software")
                .build();
    }

    private static User createSampleUser() {
        return User.builder()
                .id(DEFAULT_USER_ID)
                .username("jan_kowalski")
                .email("jan.kowalski@example.com")
                .build();
    }

    private static void printCreatedEntities(Offer offer, Company company, User user) {
        System.out.println("TEST BUILDERA");
        System.out.println("Utworzona oferta: " + offer);
        System.out.println("Utworzona firma: " + company);
        System.out.println("Utworzony uzytkownik: " + user);
    }

    /**
     * Creates a snapshot of the given offer, modifies its fields, and restores the original state.
     *
     * @param offer offer entity used to test the rollback mechanism
     */
    private static void demonstrateSnapshotRollback(Offer offer) {
        System.out.println("\nTEST MECHANIZMU WYCOFYWANIA ZMIAN");

        OfferDTO offerSnapshot = offer.toSnapshot();
        System.out.println("Zapisano snapshot oferty (DTO): " + offerSnapshot);

        corruptOfferData(offer);
        System.out.println("Stan oferty po blednej zmianie: " + offer);

        System.out.println("\nWycofywanie zmian z kopii zapasowej:");
        offer.restoreFromSnapshot(offerSnapshot);
        System.out.println("Stan oferty po przywroceniu (rollback): " + offer);

        // TODO: extend rollback verification to Company and User entities
    }

    private static void corruptOfferData(Offer offer) {
        System.out.println("\nModyfikacja danych w encji Offer:");
        offer.setTitle(INVALID_TITLE);
        offer.setLocation(INVALID_LOCATION);
    }
}
