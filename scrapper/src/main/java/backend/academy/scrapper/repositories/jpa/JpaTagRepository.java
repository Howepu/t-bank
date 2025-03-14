package backend.academy.scrapper.repositories.jpa;

import backend.academy.scrapper.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.Set;

public interface JpaTagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
}
