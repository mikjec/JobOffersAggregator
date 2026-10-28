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
import pl.pollub.agregator.dto.CompanyDTO;

/**
 * Entity representing a company posting job offers.
 */
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @NonNull
    private Long id;

    @NonNull
    private String name;

    private String industry;

    /**
     * Creates a DTO snapshot of the current company state.
     *
     * @return CompanyDTO containing copied fields
     */
    public CompanyDTO toSnapshot() {
        return CompanyDTO.builder()
                .id(this.id)
                .name(this.name)
                .industry(this.industry)
                .build();
    }

    /**
     * Restores company fields from the provided DTO snapshot.
     *
     * @param snapshot non-null CompanyDTO snapshot
     */
    public void restoreFromSnapshot(CompanyDTO snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Snapshot dla encji Company (id=" + this.id + ") nie moze byc null.");
        }
        this.id = snapshot.getId();
        this.name = snapshot.getName();
        this.industry = snapshot.getIndustry();
    }

    @Override
    public String toString() {
        return "Company{id=" + id + ", name='" + name + "', industry='" + industry + "'}";
    }
}
