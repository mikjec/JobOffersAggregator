package pl.pollub.agregator.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.pollub.agregator.dto.UserDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private Long id;
    private String username;
    private String email;

    public User(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public UserDTO toSnapshot() {
        return UserDTO.builder()
                .id(this.id)
                .username(this.username)
                .email(this.email)
                .build();
    }

    public UserDTO toDTO() {
        return toSnapshot();
    }

    public static User fromDTO(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        return User.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .build();
    }

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
