package com.mpcs51205.itemservice.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.GenericGenerator
import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
@Table
class ItemCategory(categoryName: String?): Serializable {

    @Id
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "uuid2")
    @Column(nullable = false, unique = true)
    var id: UUID? = null

    @Column(nullable = false, unique = true)
    var categoryDescription: String? = categoryName

    @JsonIgnore
    @ManyToMany(cascade = [CascadeType.PERSIST])
    @JoinTable(
        name = "categorized_items",
        joinColumns = [JoinColumn(name = "category_id")],
        inverseJoinColumns = [JoinColumn(name = "item_id")]
    )
    var items = mutableListOf<Item>()
}