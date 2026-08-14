package miju.com.robodelivery.repositories;
import miju.com.robodelivery.entities.Box;
import miju.com.robodelivery.enums.BoxState;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BoxRepository extends JpaRepository<Box, String> {
    Optional<Box> findByTxref(String txref);
    boolean existsByTxref(String txref);
    List<Box> findByStateIn(List<BoxState> states);
}
