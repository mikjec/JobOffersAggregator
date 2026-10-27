package pl.pollub.agregator.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.pollub.agregator.dto.UserDTO;

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

    public UserDTO toSnapshot() {
        return UserDTO.builder()
                .id(this.id)
                .username(this.username)
                .email(this.email)
                .build();
    }

    public void restoreFromSnapshot(UserDTO dto) {
        if (dto == null) {
            return;
        }
        this.id = dto.getId();
        this.username = dto.getUsername();
        this.email = dto.getEmail();
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email + "'}";
    }
}
