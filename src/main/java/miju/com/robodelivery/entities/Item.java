package miju.com.robodelivery.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    @Column(nullable = false, unique = true)
    private String code;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal weight;
    public Item(String name, BigDecimal weight, String code) {
        this.name = name;
        this.weight = weight;
        this.code = code;
    }
}
