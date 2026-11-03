package com.medicore.medicine.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="medicines")
public class Medicine {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String name;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal price;
    @Column(nullable=false) private boolean active = true;
    protected Medicine() {}
    public Medicine(String name, BigDecimal price) { this.name=name; this.price=price; }
    public Long getId(){return id;} public String getName(){return name;} public BigDecimal getPrice(){return price;} public boolean isActive(){return active;}
    public void update(String name, BigDecimal price, boolean active){this.name=name;this.price=price;this.active=active;}
}
