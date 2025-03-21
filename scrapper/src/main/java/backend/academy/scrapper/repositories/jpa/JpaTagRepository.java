package backend.academy.scrapper.repositories.jpa;

import backend.academy.scrapper.models.Tag;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.Set;

@Transactional
public interface JpaTagRepository extends JpaRepository<Tag, Long> {
    Tag findByName(String name);

    @Modifying
    @Query(value = "INSERT INTO Tag(name) VALUES (:name)", nativeQuery = true)
    void saveTag(@Param("name") String name);

}
