package org.inssg.backend.item;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( nullable = false, length = 50)
    private String name;

    @Column( nullable = false)
    private String imgPath;

    @Column( nullable = false)
    private int price;

    @Builder
    public Item(String name, String imgPath, int price) {
        this.name = name;
        this.imgPath = imgPath;
        this.price = price;
    }

    public static Item create(String name, String imgPath, int price) {
        return Item.builder()
                .name(name)
                .imgPath(imgPath)
                .price(price)
                .build();
    }

}
