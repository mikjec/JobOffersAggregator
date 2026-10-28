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
import pl.pollub.agregator.dto.UserDTO;

/**
 * Entity representing an application user.
 */
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @NonNull
    private Long id;

    @NonNull
    private String username;

    private String email;

    /**
     * Creates a DTO snapshot of the current user state.
     *
     * @return UserDTO containing copied fields
     */
    public UserDTO toSnapshot() {
        return UserDTO.builder()
                .id(this.id)
                .username(this.username)
                .email(this.email)
                .build();
    }

    /**
     * Restores user fields from the provided DTO snapshot.
     *
     * @param snapshot non-null UserDTO snapshot
     */
    public void restoreFromSnapshot(UserDTO snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Snapshot dla encji User (id=" + this.id + ") nie moze byc null.");
        }
        this.id = snapshot.getId();
        this.username = snapshot.getUsername();
        this.email = snapshot.getEmail();
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email + "'}";
    }
}
