package io.bootify.social_pet.photo.service;

import io.bootify.social_pet.photo.domain.Photo;
import io.bootify.social_pet.photo.model.PhotoDTO;
import io.bootify.social_pet.photo.repos.PhotoRepository;
import io.bootify.social_pet.user.domain.User;
import io.bootify.social_pet.user.repos.UserRepository;
import io.bootify.social_pet.util.NotFoundException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;

    public PhotoService(final PhotoRepository photoRepository,
            final UserRepository userRepository) {
        this.photoRepository = photoRepository;
        this.userRepository = userRepository;
    }

    public List<PhotoDTO> findAll() {
        final List<Photo> photos = photoRepository.findAll(Sort.by("id"));
        return photos.stream()
                .map(photo -> mapToDTO(photo, new PhotoDTO()))
                .toList();
    }

    public PhotoDTO get(final Integer id) {
        return photoRepository.findById(id)
                .map(photo -> mapToDTO(photo, new PhotoDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Integer create(final PhotoDTO photoDTO) {
        final Photo photo = new Photo();
        mapToEntity(photoDTO, photo);
        return photoRepository.save(photo).getId();
    }

    public void update(final Integer id, final PhotoDTO photoDTO) {
        final Photo photo = photoRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(photoDTO, photo);
        photoRepository.save(photo);
    }

    public void delete(final Integer id) {
        photoRepository.deleteById(id);
    }

    private PhotoDTO mapToDTO(final Photo photo, final PhotoDTO photoDTO) {
        photoDTO.setId(photo.getId());
        photoDTO.setPhotoUrl(photo.getPhotoUrl());
        photoDTO.setCreatedAt(photo.getCreatedAt());
        photoDTO.setUser(photo.getUser() == null ? null : photo.getUser().getId());
        return photoDTO;
    }

    private Photo mapToEntity(final PhotoDTO photoDTO, final Photo photo) {
        photo.setPhotoUrl(photoDTO.getPhotoUrl());
        photo.setCreatedAt(photoDTO.getCreatedAt());
        final User user = photoDTO.getUser() == null ? null : userRepository.findById(photoDTO.getUser())
                .orElseThrow(() -> new NotFoundException("user not found"));
        photo.setUser(user);
        return photo;
    }

}
