package io.bootify.social_pet.user.service;

import io.bootify.social_pet.photo.domain.Photo;
import io.bootify.social_pet.photo.repos.PhotoRepository;
import io.bootify.social_pet.user.domain.User;
import io.bootify.social_pet.user.model.UserDTO;
import io.bootify.social_pet.user.repos.UserRepository;
import io.bootify.social_pet.util.NotFoundException;
import io.bootify.social_pet.util.ReferencedWarning;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PhotoRepository photoRepository;

    public UserService(final UserRepository userRepository, final PhotoRepository photoRepository) {
        this.userRepository = userRepository;
        this.photoRepository = photoRepository;
    }

    public List<UserDTO> findAll() {
        final List<User> users = userRepository.findAll(Sort.by("id"));
        return users.stream()
                .map(user -> mapToDTO(user, new UserDTO()))
                .toList();
    }

    public UserDTO get(final Integer id) {
        return userRepository.findById(id)
                .map(user -> mapToDTO(user, new UserDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Integer create(final UserDTO userDTO) {
        final User user = new User();
        mapToEntity(userDTO, user);
        return userRepository.save(user).getId();
    }

    public void update(final Integer id, final UserDTO userDTO) {
        final User user = userRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(userDTO, user);
        userRepository.save(user);
    }

    public void delete(final Integer id) {
        final User user = userRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        // remove many-to-many relations at owning side
        userRepository.findAllByFollowerUsers(user)
                .forEach(u -> u.getFollowerUsers().remove(u));
        userRepository.delete(user);
    }

    private UserDTO mapToDTO(final User user, final UserDTO userDTO) {
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setNombre(user.getNombre());
        userDTO.setNumeroTlf(user.getNumeroTlf());
        userDTO.setContraseA(user.getContrasea());
        userDTO.setFechaNacimiento(user.getFechaNacimiento());
        return userDTO;
    }

    private User mapToEntity(final UserDTO userDTO, final User user) {
        user.setEmail(userDTO.getEmail());
        user.setNombre(userDTO.getNombre());
        user.setNumeroTlf(userDTO.getNumeroTlf());
        user.setContrasea(userDTO.getContraseA());
        user.setFechaNacimiento(userDTO.getFechaNacimiento());
        return user;
    }

    public ReferencedWarning getReferencedWarning(final Integer id) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final User user = userRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        final Photo userPhoto = photoRepository.findFirstByUser(user);
        if (userPhoto != null) {
            referencedWarning.setKey("user.photo.user.referenced");
            referencedWarning.addParam(userPhoto.getId());
            return referencedWarning;
        }
        return null;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Integer userId) {
        return userRepository.findById(userId);
    }
    @Transactional
    public boolean followUser(Integer currentUserId, Integer userIdToFollow) {
        Optional<User> currentUserOpt = userRepository.findById(currentUserId);
        Optional<User> userToFollowOpt = userRepository.findById(userIdToFollow);

        if (currentUserOpt.isPresent() && userToFollowOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            User userToFollow = userToFollowOpt.get();

            // Verificar si ya sigue al usuario
            if (!currentUser.getFollowedUsers().contains(userToFollow)) {
                currentUser.getFollowedUsers().add(userToFollow);
                userToFollow.getFollowerUsers().add(currentUser);
                userRepository.save(currentUser);
                userRepository.save(userToFollow);
                return true;
            }
        }
        return false;
    }
    @Transactional
    public boolean unfollowUser(Integer currentUserId, Integer userIdToUnfollow) {
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        User userToUnfollow = userRepository.findById(userIdToUnfollow).orElse(null);
        if (currentUser != null && userToUnfollow != null) {
            currentUser.getFollowedUsers().remove(userToUnfollow);
            userToUnfollow.getFollowerUsers().remove(currentUser);
            System.out.println("User unfollowed");
            userRepository.save(currentUser);
            userRepository.save(userToUnfollow);
            return true;
        }
        return false;
    }
}



