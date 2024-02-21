package io.bootify.social_pet.user.repos;

import io.bootify.social_pet.user.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Integer> {

    User findFirstByFollowerUsersAndIdNot(User user, final Integer id);

    List<User> findAllByFollowerUsers(User user);

}
