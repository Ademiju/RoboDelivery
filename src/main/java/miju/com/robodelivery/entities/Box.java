package miju.com.robodelivery.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import miju.com.robodelivery.enums.BoxState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Table(name = "boxes")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Box {

    @Id
    @Column(nullable = false, unique = true, length = 20)
    private String txref;
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal weightLimit;
    @Column(nullable = false)
    private int batteryCapacity;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private BoxState state = BoxState.IDLE;
    /**
     * Items are independent catalogue records. A record can be loaded into more
     * than one box (and may occur more than once in this list), so the
     * association is stored separately from either entity.
     */
    @ManyToMany
    @JoinTable(
            name = "box_items",
            joinColumns = @JoinColumn(name = "box_txref"),
            inverseJoinColumns = @JoinColumn(name = "item_code"))
    private List<Item> items = new ArrayList<>();



    public Box(String txref, BigDecimal weightLimit, int batteryCapacity) {
        this.txref = txref;
        this.weightLimit = weightLimit;
        this.batteryCapacity = batteryCapacity;
    }

    public BigDecimal loadedWeight() {
        return items.stream().map(Item::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addItem(Item item) {
        items.add(item);
    }
}
