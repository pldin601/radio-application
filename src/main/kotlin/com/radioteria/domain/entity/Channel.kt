package com.radioteria.domain.entity

import com.radioteria.auth.BelongsToUser
import javax.persistence.*

@Entity
@Table(name = "channels")
data class Channel(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        @Column(name = "id")
        var id: Long? = null,

        @Column(name = "name", nullable = false)
        var name: String = "",

        @ManyToOne(targetEntity = User::class)
        @JoinColumn(name = "user_id")
        var user: User,

        @Column(name = "started_at")
        var startedAt: Long? = null,

        @JoinColumn(name = "artwork_file_id")
        @ManyToOne(targetEntity = File::class)
        var artworkFile: File? = null
) : BelongsToUser {
    fun isStarted(): Boolean {
        return startedAt != null
    }

    override fun belongsTo(user: User): Boolean {
        return user.id == this.user.id
    }
}