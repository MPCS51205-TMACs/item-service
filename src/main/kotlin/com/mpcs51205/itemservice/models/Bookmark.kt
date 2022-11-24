package com.mpcs51205.itemservice.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.GenericGenerator
import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
@Table
class Bookmark: Serializable {

    @Id
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "uuid2")
    @Column(nullable = false)
    var id: UUID? = null

    @Column(nullable = false)
    var userId: UUID? = null

    @JsonIgnore
    @ManyToOne
    var item: Item? = null

}