package miju.com.robodelivery.repositories;

import miju.com.robodelivery.entities.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, String> {
    List<Item> findByCodeIn(List<String> codes);
    boolean existsByCodeIn(List<String> codes);
}
