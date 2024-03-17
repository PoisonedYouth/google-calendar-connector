package de.dwins.calendar.application.port.output

import de.dwins.calendar.infrastructure.adapter.output.User

/**
 * Interface for interacting with the output of user-related operations.
 */
interface UserOutputPort {

    /**
     * Returns a list of all users.
     *
     * @return A list of User objects representing all the users.
     */
    fun getAll(): List<User>

    /**
     * Finds a User by their Google ID.
     *
     * @param googleId The Google ID of the User to find.
     * @return The User object corresponding to the provided Google ID, or null if no User is found.
     */
    fun findBy(googleId: String): User?

    /**
     * Updates the information of a User.
     *
     * @param user The User object containing the updated information.
     */
    fun update(user: User)

    /**
     * Removes a user from the system based on their Google ID.
     *
     * @param googleId The Google ID of the user to remove.
     */
    fun remove(googleId: String)
}


