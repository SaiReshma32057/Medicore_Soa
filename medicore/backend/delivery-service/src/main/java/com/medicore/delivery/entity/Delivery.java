package com.medicore.delivery.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "deliveries")
public class Delivery {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long orderId;

  @Column(nullable = false)
  private String address;

  @Column(nullable = false)
  private String status = "CREATED";

  protected Delivery() {}

  public Delivery(Long orderId, String address) {
    this.orderId = orderId;
    this.address = address;
  }

  public Long getId() {
    return id;
  }

  public Long getOrderId() {
    return orderId;
  }

  public String getAddress() {
    return address;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String s) {
    status = s;
  }
}
