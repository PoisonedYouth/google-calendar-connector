package de.dwins.calendar.infrastructure.adapter.output

import de.dwins.calendar.application.port.output.GoogleCalendarEventOutputPort
import de.dwins.calendar.domain.entity.GoogleCalendarEvent
import de.dwins.calendar.domain.entity.GoogleCalendarEventAttendee
import jakarta.persistence.Entity
import jakarta.persistence.EntityManager
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.TypedQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Root
import jakarta.transaction.Transactional
import org.hibernate.annotations.UuidGenerator
import java.time.Instant

@Transactional
open class GoogleCalendarEventRepository(
    private val entityManager: EntityManager
) : GoogleCalendarEventOutputPort {

    override fun getGoogleCalendarEvents(googleId: String): List<GoogleCalendarEvent> {
        val allQuery: TypedQuery<GoogleCalendarEventEntity> = allByGoogleIdQuery()
        return allQuery.resultList.map {
            it.toGoogleCalendarEvent()
        }
    }

    private fun allByGoogleIdQuery(): TypedQuery<GoogleCalendarEventEntity> {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val cq: CriteriaQuery<GoogleCalendarEventEntity> = cb.createQuery(GoogleCalendarEventEntity::class.java)
        val rootEntry: Root<GoogleCalendarEventEntity> = cq.from(GoogleCalendarEventEntity::class.java)
        val all: CriteriaQuery<GoogleCalendarEventEntity> = cq.select(rootEntry)
        val allQuery: TypedQuery<GoogleCalendarEventEntity> = entityManager.createQuery(all)
        return allQuery
    }

    override fun saveGoogleCalendarEvents(googleId: String, calendarEvents: List<GoogleCalendarEvent>) {
        allByGoogleIdQuery().resultList.forEach {
            entityManager.remove(it)
        }
        entityManager.flush()
        calendarEvents.forEach {
            entityManager.merge(it.toGoogleCalendarEventEntity(googleId))
        }
    }
}

@Table(name = "calendar_event")
@Entity
data class GoogleCalendarEventEntity(
        @Id @UuidGenerator val id: String,
        val googleId: String,
        val description: String,
        val summary: String,
        @OneToMany(mappedBy = "id")
    val attendees: Set<GoogleCalendarEventAttendeeEntity>,
        val creator: String,
        val organizer: String,
        val location: String,
        val startTimeStamp: Instant,
        val endTimeStamp: Instant,
        val recurringEventId: String? = null,
        val eventType: String,
        val status: String,
)

@Table(name = "event_attendee")
@Entity
data class GoogleCalendarEventAttendeeEntity(
    @Id @UuidGenerator val id: String? = null,
    val email: String,
    val optional: Boolean,
    val organizer: Boolean,
    val responseStatus: String,
)

fun GoogleCalendarEvent.toGoogleCalendarEventEntity(googleId: String): GoogleCalendarEventEntity {
    return GoogleCalendarEventEntity(
        id = this.id,
        googleId = googleId,
        description = this.description,
        summary = this.summary,
        attendees = this.attendees.map { it.toGoogleCalendarEventAttendeeEntity() }.toSet(),
        creator = this.creator,
        organizer = this.organizer,
        location = this.location,
        startTimeStamp = this.start,
        endTimeStamp = this.end,
        recurringEventId = this.recurringEventId,
        eventType = this.eventType,
        status = this.status
    )
}

fun GoogleCalendarEventAttendee.toGoogleCalendarEventAttendeeEntity(): GoogleCalendarEventAttendeeEntity {
    return GoogleCalendarEventAttendeeEntity(
        id = this.id,
        email = this.email,
        optional = this.optional,
        organizer = this.organizer,
        responseStatus = this.responseStatus
    )
}

fun GoogleCalendarEventEntity.toGoogleCalendarEvent(): GoogleCalendarEvent {
    return GoogleCalendarEvent(
        id = this.id,
        description = this.description,
        summary = this.summary,
        attendees = this.attendees.map { it.toGoogleCalendarEventAttendee() },
        creator = this.creator,
        organizer = this.organizer,
        location = this.location,
        start = this.startTimeStamp,
        end = this.endTimeStamp,
        recurringEventId = this.recurringEventId,
        eventType = this.eventType,
        status = this.status
    )
}

fun GoogleCalendarEventAttendeeEntity.toGoogleCalendarEventAttendee(): GoogleCalendarEventAttendee {
    return GoogleCalendarEventAttendee(
        id = id,
        email = this.email,
        optional = this.optional,
        organizer = this.organizer,
        responseStatus = this.responseStatus
    )
}