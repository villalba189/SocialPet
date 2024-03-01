package io.bootify.social_pet.photo.repos;

import io.bootify.social_pet.photo.domain.Photo;
import io.bootify.social_pet.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;


public interface PhotoRepository extends JpaRepository<Photo, Integer> {

    Photo findFirstByUser(User user);

    List<Photo> findPhotosByUserId(Integer Id);

    List<Photo> findAllByUserIdInOrderByDateCreatedDesc(Set<Integer> followedUserIds);

    List<Photo> findAllPhotosByUserIdInOrderByDateCreatedDesc(Set<Integer> followedUserIds);
}
