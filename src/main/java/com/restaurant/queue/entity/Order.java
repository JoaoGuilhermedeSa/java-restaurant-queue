package com.restaurant.queue.entity;

import com.restaurant.queue.enums.OrderStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_order")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerName;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime estimatedCompletionTime;

    private LocalDateTime statusUpdatedAt;

    @Version
    private Long version;

    public Order() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.statusUpdatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) {
        this.status = status;
        this.statusUpdatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getEstimatedCompletionTime() { return estimatedCompletionTime; }
    public void setEstimatedCompletionTime(LocalDateTime estimatedCompletionTime) { this.estimatedCompletionTime = estimatedCompletionTime; }

    public LocalDateTime getStatusUpdatedAt() { return statusUpdatedAt; }
    public void setStatusUpdatedAt(LocalDateTime statusUpdatedAt) { this.statusUpdatedAt = statusUpdatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
