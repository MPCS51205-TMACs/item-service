package com.mpcs51205.itemservice.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.*

@Entity
@Table
class ItemBookmark {

    @Id
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "uuid2")
    @Column(nullable = false)
    @JsonIgnore
    var bookmarkId: UUID? = null

    @Column(nullable = false)
    var userBookmark: UUID? = null

    @ManyToOne
    @JsonIgnore
    lateinit var bookmarkedItem: Item
}
