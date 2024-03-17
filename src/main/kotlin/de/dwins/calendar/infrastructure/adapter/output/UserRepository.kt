package de.dwins.calendar.infrastructure.adapter.output

import de.dwins.calendar.application.port.output.UserOutputPort
import jakarta.persistence.Entity
import jakarta.persistence.EntityManager
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.TypedQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Root
import jakarta.transaction.Transactional
import org.hibernate.annotations.UuidGenerator
import java.time.Instant


@Transactional
open class UserRepository(
    private val entityManager: EntityManager
) : UserOutputPort {

    override fun getAll(): List<User> {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val cq: CriteriaQuery<UserEntity> = cb.createQuery(UserEntity::class.java)
        val rootEntry: Root<UserEntity> = cq.from(UserEntity::class.java)
        val all: CriteriaQuery<UserEntity> = cq.select(rootEntry)
        val allQuery: TypedQuery<UserEntity> = entityManager.createQuery(all)
        return allQuery.resultList.map {
            it.toUser()
        }
    }

    override fun findBy(googleId: String): User? {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val cq: CriteriaQuery<UserEntity> = cb.createQuery(UserEntity::class.java)
        val rootEntry: Root<UserEntity> = cq.from(UserEntity::class.java)
        val byGoogleId: CriteriaQuery<UserEntity> =
            cq.select(rootEntry).where(cb.equal(rootEntry.get<String>("googleId"), googleId))
        val byGoogleIdQuery: TypedQuery<UserEntity> = entityManager.createQuery(byGoogleId)
        return byGoogleIdQuery.resultList.firstOrNull()?.toUser()
    }

    override fun update(user: User) {
        val existingEntity = findBy(user.googleId)?.toUserEntity()
        val entityToPersist = existingEntity?.copy(
            googleId = user.googleId,
            primaryMail = user.primaryMail,
            refreshToken = user.refreshToken,
            syncToken = user.syncToken,
            webhookRegistered = user.webhookRegistered
        )
            ?: UserEntity(
                googleId = user.googleId,
                primaryMail = user.primaryMail,
                refreshToken = user.refreshToken,
                syncToken = user.syncToken,
                webhookRegistered = user.webhookRegistered
            )
        entityManager.merge(entityToPersist)
    }

    override fun remove(googleId: String) {
        val existingEntity = findBy(googleId)
        if (existingEntity != null) {
            entityManager.remove(existingEntity)
        }
    }
}

@Table(name = "calendar_user")
@Entity
data class UserEntity(
    @Id @UuidGenerator val id: String? = null,
    val googleId: String,
    val primaryMail: String,
    val refreshToken: String,
    val syncToken: String?,
    val webhookRegistered: Instant?
)

fun UserEntity.toUser(): User {
    return User(
        id = this.id,
        googleId = this.googleId,
        primaryMail = this.primaryMail,
        refreshToken = this.refreshToken,
        syncToken = this.syncToken,
        webhookRegistered = this.webhookRegistered
    )
}

fun User.toUserEntity(): UserEntity {
    return UserEntity(
        id = this.id,
        googleId = this.googleId,
        primaryMail = this.primaryMail,
        refreshToken = this.refreshToken,
        syncToken = this.syncToken,
        webhookRegistered = this.webhookRegistered
    )
}