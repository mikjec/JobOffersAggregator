package pl.pollub.agregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pollub.agregator.dto.UserDTO;
import pl.pollub.agregator.entity.User;
import pl.pollub.agregator.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserDTO> getAll() {
        return userRepository.findAll()
                .stream()
                .map(User::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getById(Long id) {
        return userRepository.findById(id)
                .map(User::toDTO);
    }

    @Transactional
    public UserDTO save(UserDTO dto) {
        User user = User.fromDTO(dto);
        User saved = userRepository.save(user);
        return saved.toDTO();
    }

    @Transactional
    public Optional<UserDTO> update(Long id, UserDTO dto) {
        return userRepository.findById(id)
                .map(user -> {
                    user.restoreFromSnapshot(dto);
                    user.setId(id);
                    return userRepository.save(user).toDTO();
                });
    }

    @Transactional
    public boolean delete(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
