/*
 * Copyright (C) 2026
 * by Lublin University of Technology.
 * All rights reserved.
 */
package pl.pollub.agregator.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.pollub.agregator.dto.OfferDTO;

/**
 * Entity representing a job offer.
 */
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Offer {

    @NonNull
    private Long id;

    @NonNull
    private String title;

    private String location;

    /**
     * Creates a DTO snapshot of the current offer state.
     *
     * @return OfferDTO containing copied fields
     */
    public OfferDTO toSnapshot() {
        return OfferDTO.builder()
                .id(this.id)
                .title(this.title)
                .location(this.location)
                .build();
    }

    /**
     * Restores offer fields from the provided DTO snapshot.
     *
     * @param snapshot non-null OfferDTO snapshot
     */
    public void restoreFromSnapshot(OfferDTO snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Snapshot dla encji Offer (id=" + this.id + ") nie moze byc null.");
        }
        this.id = snapshot.getId();
        this.title = snapshot.getTitle();
        this.location = snapshot.getLocation();
    }

    @Override
    public String toString() {
        return "Offer{id=" + id + ", title='" + title + "', location='" + location + "'}";
    }
}
