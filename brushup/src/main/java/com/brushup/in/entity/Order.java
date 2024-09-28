package com.brushup.in.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="order")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    private String ordername;
    private String ordertype;
    private float order_amount;

    @ManyToMany(mappedBy = "customer")
    private Set<Customer> customers = new HashSet<>();
}
