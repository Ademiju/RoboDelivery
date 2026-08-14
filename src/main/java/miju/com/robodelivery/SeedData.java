package miju.com.robodelivery;
import miju.com.robodelivery.entities.Box;
import miju.com.robodelivery.entities.Item;
import miju.com.robodelivery.enums.BoxState;
import miju.com.robodelivery.helpers.SystemProperties;
import miju.com.robodelivery.repositories.BoxRepository;
import miju.com.robodelivery.repositories.ItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.util.List;
@Configuration
class SeedData {
 @Bean
 CommandLineRunner seedBoxes(BoxRepository boxRepository, ItemRepository itemRepository, SystemProperties systemProperties) {
 return args -> {
  Box idealBox = new Box("IDLE_001", BigDecimal.valueOf(systemProperties.getMaximumBoxWeightLimit()), 90);
  Box loadingBox = new Box("LOADING_001", BigDecimal.valueOf(systemProperties.getMaximumBoxWeightLimit()), 70);
  List<Item> seededItems = itemRepository.saveAll(List.of(
          new Item("medical-kit", new BigDecimal("100"), "MEDICAL_KIT_1"),
          new Item("groceries", new BigDecimal("200"), "GROC_1"),
          new Item("groceries", new BigDecimal("100"), "GROC_2")));
  seededItems.forEach(loadingBox::addItem);

  loadingBox.setState(BoxState.LOADING);
  boxRepository.save(idealBox);
  boxRepository.save(loadingBox);
 };
 }
}
