package pl.pollub.agregator.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pl.pollub.agregator.dto.UserDTO;
import pl.pollub.agregator.entity.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> User.builder()
            .id(rs.getLong("id"))
            .username(rs.getString("username"))
            .email(rs.getString("email"))
            .build();

    public List<UserDTO> getAll() {
        String sql = "SELECT * FROM `user`";
        return jdbcTemplate.query(sql, userRowMapper)
                .stream()
                .map(User::toDTO)
                .toList();
    }

    public Optional<UserDTO> getById(Long id) {
        String sql = "SELECT * FROM `user` WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.ofNullable(user != null ? user.toDTO() : null);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public UserDTO save(UserDTO dto) {
        User user = User.fromDTO(dto);

        if (user.getId() != null) {
            String sql = "INSERT INTO `user` (id, username, email) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, user.getId(), user.getUsername(), user.getEmail());
            return user.toDTO();
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO `user` (username, email) VALUES (?, ?)";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            user.setId(key.longValue());
        }

        return user.toDTO();
    }

    public Optional<UserDTO> update(Long id, UserDTO dto) {
        Optional<UserDTO> existing = getById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        User user = User.fromDTO(existing.get());
        user.restoreFromSnapshot(dto);
        user.setId(id);

        String sql = "UPDATE `user` SET username = ?, email = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(
                sql,
                user.getUsername(),
                user.getEmail(),
                id
        );

        if (rowsUpdated > 0) {
            return Optional.of(user.toDTO());
        }
        return Optional.empty();
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM `user` WHERE id = ?";
        int rows = jdbcTemplate.update(sql, id);
        return rows > 0;
    }

    public boolean hardDelete(Long id) {
        return delete(id);
    }
}
