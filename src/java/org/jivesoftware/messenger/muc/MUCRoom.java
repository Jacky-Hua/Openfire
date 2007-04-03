/**
 * $RCSfile$
 * $Revision$
 * $Date$
 *
 * Copyright (C) 2004 Jive Software. All rights reserved.
 *
 * This software is published under the terms of the GNU Public License (GPL),
 * a copy of which is included in this distribution.
 */

package org.jivesoftware.messenger.muc;

import java.util.List;
import java.util.Date;
import java.util.Collection;

import org.jivesoftware.messenger.muc.spi.IQAdminHandler;
import org.jivesoftware.messenger.muc.spi.IQOwnerHandler;
import org.jivesoftware.util.NotFoundException;
import org.jivesoftware.messenger.auth.UnauthorizedException;
import org.jivesoftware.messenger.user.UserAlreadyExistsException;
import org.jivesoftware.messenger.user.UserNotFoundException;
import org.xmpp.packet.Presence;
import org.xmpp.packet.Message;
import org.xmpp.packet.JID;


/**
 * A chat room on the chat server manages its users, and
 * enforces it's own security rules.
 *
 * @author Gaston Dombiak
 */
public interface MUCRoom extends ChatDeliverer {

    /**
     * Get the name of this room.
     *
     * @return The name for this room
     */
    String getName();

    /**
     * Obtain a unique numerical id for this room. Useful for storing rooms in databases. If the 
     * room is persistent or is logging the conversation then the returned ID won't be -1.
     *
     * @return The unique id for this room or -1 if the room is temporary and is not logging the
     * conversation.
     */
    long getID();

    /**
     * Sets a new room ID if the room has just been saved to the database or sets the saved ID of
     * the room in the database while loading the room. 
     * 
     * @param roomID the saved ID of the room in the DB or a new one if the room is being saved to 
     * the DB.
     */
    void setID(long roomID);

    /**
     * Returns the date when the room was created.
     *
     * @return the date when the room was created.
     */
    Date getCreationDate();

    /**
     * Sets the date when the room was created.
     *
     * @param creationDate the date when the room was created.
     */
    void setCreationDate(Date creationDate);

    /**
     * Returns the last date when the room's configuration was modified. If the room's configuration
     * was never modified then the creation date will be returned.
     *
     * @return the last date when the room's configuration was modified.
     */
    Date getModificationDate();

    /**
     * Sets the last date when the room's configuration was modified. If the room's configuration
     * was never modified then the initial value will be the same as the creation date.
     *
     * @param modificationDate the last date when the room's configuration was modified.
     */
    void setModificationDate(Date modificationDate);

    /**
     * Sets the date when the last occupant left the room. A null value means that there are
     * occupants in the room at the moment.
     *
     * @param emptyDate the date when the last occupant left the room or null if there are occupants
     *        in the room.
     */
    void setEmptyDate(Date emptyDate);

    /**
     * Returns the date when the last occupant left the room. A null value means that there are
     * occupants in the room at the moment.
     *
     * @return the date when the last occupant left the room or null if there are occupants in the
     *         room at the moment.
     */
    Date getEmptyDate();

    /**
     * Obtain the role of the chat server (mainly for addressing messages and presence).
     *
     * @return The role for the chat room itself
     */
    MUCRole getRole();

    /**
     * Obtain the role of a given user by nickname.
     *
     * @param nickname The nickname of the user you'd like to obtain
     * @return The user's role in the room
     * @throws UserNotFoundException If there is no user with the given nickname
     */
    MUCRole getOccupant(String nickname) throws UserNotFoundException;

    /**
     * Obtain the roles of a given user in the room by his bare JID. A user can have several roles,
     * one for each client resource from which the user has joined the room. 
     *
     * @param jid The bare jid of the user you'd like to obtain
     * @return The user's roles in the room
     * @throws UserNotFoundException If there is no user with the given nickname
     */
    List<MUCRole> getOccupantsByBareJID(String jid) throws UserNotFoundException;

    /**
     * Obtain the role of a given user in the room by his full JID.
     *
     * @param jid The full jid of the user you'd like to obtain
     * @return The user's role in the room
     * @throws UserNotFoundException If there is no user with the given nickname
     */
    MUCRole getOccupantByFullJID(String jid) throws UserNotFoundException;

    /**
     * Obtain the roles of all users in the chatroom.
     *
     * @return a collection with all users in the chatroom
     */
    Collection<MUCRole> getOccupants();

    /**
     * Returns the number of occupants in the chatroom at the moment.
     *
     * @return int the number of occupants in the chatroom at the moment.
     */
    int getOccupantsCount();

    /**
     * Determine if a given nickname is taken.
     *
     * @param nickname The nickname of the user you'd like to obtain
     * @return True if a nickname is taken
     */
    boolean hasOccupant(String nickname);

    /**
     * Returns the reserved room nickname for the bare JID or null if none.
     * 
     * @param bareJID The bare jid of the user of which you'd like to obtain his reserved nickname.
     * @return the reserved room nickname for the bare JID or null if none.
     */
    String getReservedNickname(String bareJID);

    /**
     * Returns the affiliation state of the user in the room. Possible affiliations are 
     * MUCRole.OWNER, MUCRole.ADMINISTRATOR, MUCRole.MEMBER, MUCRole.OUTCAST and MUCRole.NONE.<p>
     * 
     * Note: Prerequisite - A lock must already be obtained before sending this message.
     *  
     * @param bareJID The bare jid of the user of which you'd like to obtain his affiliation.
     * @return the affiliation state of the user in the room.
     */
    int getAffiliation(String bareJID);

    /**
     * Joins the room using the given nickname.
     * 
     * @param nickname The nickname the user wants to use in the chatroom.
     * @param password The password provided by the user to enter the chatroom or null if none.
     * @param historyRequest The amount of history that the user request or null meaning default.
     * @param user The user joining.
     * @param presence The presence sent by the user to join the room.
     * @return The role created for the user.
     * @throws UnauthorizedException If the user doesn't have permision to join the room.
     * @throws UserAlreadyExistsException If the nickname is already taken.
     * @throws RoomLockedException If the user is trying to join a locked room.
     * @throws ForbiddenException If the user is an outcast.
     * @throws RegistrationRequiredException If the user is not a member of a members-only room.
     * @throws NotAllowedException If the user is has exceded the max number of occupants.
     * @throws ConflictException If another user attempts to join the room with a nickname reserved
     *             by the first user.
     */
    MUCRole joinRoom(String nickname, String password, HistoryRequest historyRequest, MUCUser user,
            Presence presence) throws UnauthorizedException, UserAlreadyExistsException,
            RoomLockedException, ForbiddenException, RegistrationRequiredException,
            NotAllowedException, ConflictException;

    /**
     * Remove a member from the chat room.
     *
     * @param nickname The user to remove
     * @throws UserNotFoundException If the nickname is not found.
     */
    void leaveRoom(String nickname) throws UserNotFoundException;

    /**
     * Destroys the room. Each occupant will be removed and will receive a presence stanza of type
     * "unavailable" whose "from" attribute will be the occupant's nickname that the user knows he 
     * or she has been removed from the room.
     * 
     * @param alternateJID the alternate JID. Commonly used to provide a replacement room.
     * @param reason the reason why the room was destroyed.
     */
    void destroyRoom(String alternateJID, String reason);

    /**
     * Create a new presence in this room for the given role.
     *
     * @return The new presence
     * @throws UnauthorizedException If the user doesn't have permission to leave the room
     */
    Presence createPresence(Presence.Type type) throws UnauthorizedException;

    /**
     * Broadcast a given message to all members of this chat room. The sender is always set to 
     * be the chatroom.
     *
     * @param msg The message to broadcast
     */
    void serverBroadcast(String msg);
    
    /**
     * Returns the total length of the chat session.
     * 
     * @return length of chat session in milliseconds.
     */
    public long getChatLength();

    /**
     * Adds a new user to the list of owners. The user is the actual creator of the room. Only the
     * MultiUserChatServer should use this method. Regular owners list maintenance MUST be done
     * through {@link #addOwner(String,MUCRole)}.
     * 
     * @param bareJID The bare JID of the user to add as owner.
     */
    public void addFirstOwner(String bareJID);

    /**
     * Adds a new user to the list of owners.
     * 
     * @param bareJID The bare JID of the user to add as owner.
     * @param senderRole the role of the user that is trying to modify the owners list.
     * @return the list of updated presences of all the client resources that the client used to
     *         join the room.
     * @throws ForbiddenException If the user is not allowed to modify the owner list.
     */
    public List<Presence> addOwner(String bareJID, MUCRole senderRole) throws ForbiddenException;

    /**
     * Adds a list of users to the list of owners.
     *
     * @param newOwners the list of bare JIDs of the users to add to the list of existing owners.
     * @param senderRole the role of the user that is trying to modify the owners list.
     * @return the list of updated presences of all the clients resources that the clients used to
     *         join the room.
     * @throws ForbiddenException If the user is not allowed to modify the owner list.
     */
    public List<Presence> addOwners(List<String> newOwners, MUCRole senderRole)
            throws ForbiddenException;

    /**
     * Adds a list of users to the list of admins.
     *
     * @param newAdmins the list of bare JIDs of the users to add to the list of existing admins.
     * @param senderRole the role of the user that is trying to modify the admins list.
     * @return the list of updated presences of all the clients resources that the clients used to
     *         join the room.
     * @throws ForbiddenException If the user is not allowed to modify the admin list.
     * @throws ConflictException If the room was going to lose all its owners.
     */
    public List<Presence> addAdmins(List<String> newAdmins, MUCRole senderRole)
            throws ForbiddenException, ConflictException;

    /**
     * Adds a new user to the list of admins.
     * 
     * @param bareJID The bare JID of the user to add as admin.
     * @param senderRole The role of the user that is trying to modify the admins list.
     * @return the list of updated presences of all the client resources that the client used to
     *         join the room.
     * @throws ForbiddenException If the user is not allowed to modify the admin list.
     * @throws ConflictException If the room was going to lose all its owners.
     */
    public List<Presence> addAdmin(String bareJID, MUCRole senderRole) throws ForbiddenException,
            ConflictException;

    /**
     * Adds a new user to the list of members.
     * 
     * @param bareJID The bare JID of the user to add as a member.
     * @param nickname The reserved nickname of the member for the room or null if none.
     * @param senderRole the role of the user that is trying to modify the members list.
     * @return the list of updated presences of all the client resources that the client used to
     *         join the room.
     * @throws ForbiddenException If the user is not allowed to modify the members list.
     * @throws ConflictException If the desired room nickname is already reserved for the room or if
     *             the room was going to lose all its owners.
     */
    public List<Presence> addMember(String bareJID, String nickname, MUCRole senderRole)
            throws ForbiddenException, ConflictException;

    /**
     * Adds a new user to the list of outcast users.
     * 
     * @param bareJID The bare JID of the user to add as an outcast.
     * @param reason The reason why the user was banned.
     * @param senderRole The role of the user that initiated the ban.
     * @return the list of updated presences of all the client resources that the client used to
     *         join the room.
     * @throws NotAllowedException Thrown if trying to ban an owner or an administrator.
     * @throws ForbiddenException If the user is not allowed to modify the outcast list.
     * @throws ConflictException If the room was going to lose all its owners.
     */
    public List<Presence> addOutcast(String bareJID, String reason, MUCRole senderRole)
            throws NotAllowedException, ForbiddenException, ConflictException;

    /**
     * Removes the user from all the other affiliation list thus giving the user a NONE affiliation.
     * 
     * @param bareJID The bare JID of the user to keep with a NONE affiliation.
     * @param senderRole The role of the user that set the affiliation to none.
     * @return the list of updated presences of all the client resources that the client used to
     *         join the room or null if none was updated.
     * @throws ForbiddenException If the user is not allowed to modify the none list.
     * @throws ConflictException If the room was going to lose all its owners.
     */
    public List<Presence> addNone(String bareJID, MUCRole senderRole) throws ForbiddenException,
            ConflictException;

    /**
     * Changes the role of the user within the room to moderator. A moderator is allowed to kick
     * occupants as well as granting/revoking voice from occupants.
     *
     * @param fullJID The full JID of the occupant to give moderator privileges.
     * @param senderRole The role of the user that is granting moderator privileges to an occupant.
     * @return the updated presence of the occupant or <tt>null</tt> if the JID does not belong to
     *         an existing occupant.
     * @throws ForbiddenException If the user is not allowed to grant moderator privileges.
     */
    public Presence addModerator(JID fullJID, MUCRole senderRole) throws ForbiddenException;

    /**
     * Changes the role of the user within the room to participant. A participant is allowed to send
     * messages to the room (i.e. has voice) and may change the room's subject.
     *
     * @param fullJID The full JID of the occupant to give participant privileges.
     * @param reason The reason why participant privileges were gave to the user or <tt>null</tt>
     *        if none.
     * @param senderRole The role of the user that is granting participant privileges to an occupant.
     * @return the updated presence of the occupant or <tt>null</tt> if the JID does not belong to
     *         an existing occupant.
     * @throws NotAllowedException If trying to change the moderator role to an owner or an admin.
     * @throws ForbiddenException If the user is not allowed to grant participant privileges.
     */
    public Presence addParticipant(JID fullJID, String reason, MUCRole senderRole)
            throws NotAllowedException, ForbiddenException;

    /**
     * Changes the role of the user within the room to visitor. A visitor can receive messages but
     * is not allowed to send messages to the room (i.e. does not has voice) and may invite others
     * to the room.
     *
     * @param jid the full JID of the occupant to change to visitor.
     * @param senderRole the role of the user that is changing the role to visitor.
     * @return the updated presence of the occupant or <tt>null</tt> if the JID does not belong to
     *         an existing occupant.
     * @throws NotAllowedException if trying to change the moderator role to an owner or an admin.
     * @throws ForbiddenException if the user is not a moderator.
     */
    public Presence addVisitor(JID jid, MUCRole senderRole) throws NotAllowedException,
            ForbiddenException;

    /**
     * Returns true if the room is locked. The lock will persist for a defined period of time. If 
     * the room owner does not configure the room within the timeout period, the room owner is 
     * assumed to have accepted the default configuration.
     * 
     * @return true if the room is locked. 
     */
    public boolean isLocked();

    /**
     * Returns true if the room is locked and it was locked by a room owner after the room was
     * initially configured.
     *
     * @return true if the room is locked and it was locked by a room owner after the room was
     *         initially configured.
     */
    public boolean isManuallyLocked();

    /**
     * An event callback fired whenever an occupant changes his nickname within the chatroom.
     *  
     * @param oldNick old nickname within the room.
     * @param newNick new nickname within the room.
     */
    public void nicknameChanged(String oldNick, String newNick);
    
    /**
     * Changes the room's subject if the occupant has enough permissions. The occupant must be
     * a moderator or the room must be configured so that anyone can change its subject. Otherwise
     * a forbidden exception will be thrown.<p>
     * 
     * The new subject will be added to the history of the room.
     *  
     * @param packet the sent packet to change the room's subject.
     * @param role the role of the user that is trying to change the subject.
     * @throws ForbiddenException If the user is not allowed to change the subject.
     */
    public void changeSubject(Message packet, MUCRole role) throws ForbiddenException;

    /**
     * Returns the last subject that some occupant set to the room.
     * 
     * @return the last subject that some occupant set to the room.
     */
    public String getSubject();

    /**
     * Sets the last subject that some occupant set to the room. This message will only be used
     * when loading a room from the database. 
     * 
     * @param subject the last known subject of the room.
     */
    public void setSubject(String subject);

    /**
     * Sends a message to the all the occupants. In a moderated room, this privilege is restricted
     * to occupants with a role of participant or higher. In an unmoderated room, any occupant can
     * send a message to all other occupants.
     * 
     * @param message The message to send.
     * @param senderRole the role of the user that is trying to send a public message.
     * @throws ForbiddenException If the user is not allowed to send a public message (i.e. does not
     *             have voice in the room).
     */
    public void sendPublicMessage(Message message, MUCRole senderRole) throws ForbiddenException;

    /**
     * Sends a private message to a selected occupant. 
     * 
     * @param message The message to send.
     * @param senderRole the role of the user that is trying to send a public message.
     * @throws NotFoundException If the user is sending a message to a room JID that does not exist.
     */
    public void sendPrivateMessage(Message message, MUCRole senderRole) throws NotFoundException;

    /**
     * Kicks a user from the room. If the user was in the room, the returned updated presence will
     * be sent to the remaining occupants. 
     * 
     * @param fullJID The full JID of the kicked user.
     * @param actorJID The JID of the actor that initiated the kick.
     * @param reason The reason why the user was kicked.
     * @return the updated presence of the kicked user or null if the user was not in the room.
     * @throws NotAllowedException Thrown if trying to ban an owner or an administrator.
     */
    public Presence kickOccupant(JID fullJID, JID actorJID, String reason)
            throws NotAllowedException;

    public IQOwnerHandler getIQOwnerHandler();

    public IQAdminHandler getIQAdminHandler();

    /**
     * Returns the history of the room which includes chat transcripts.
     *
     * @return the history of the room which includes chat transcripts.
     */
    public MUCRoomHistory getRoomHistory();

    /**
     * Returns a collection with the current list of owners. The collection contains the bareJID of
     * the users with owner affiliation.
     *
     * @return a collection with the current list of owners.
     */
    public Collection<String> getOwners();

    /**
     * Returns a collection with the current list of admins. The collection contains the bareJID of
     * the users with admin affiliation.
     *
     * @return a collection with the current list of admins.
     */
    public Collection<String> getAdmins();

    /**
     * Returns a collection with the current list of room members. The collection contains the
     * bareJID of the users with member affiliation. If the room is not members-only then the list
     * will contain the users that registered with the room and therefore they may have reserved a
     * nickname.
     *
     * @return a collection with the current list of members.
     */
    public Collection<String> getMembers();

    /**
     * Returns a collection with the current list of outcast users. An outcast user is not allowed
     * to join the room again. The collection contains the bareJID of the users with outcast
     * affiliation.
     *
     * @return a collection with the current list of outcast users.
     */
    public Collection<String> getOutcasts();

    /**
     * Returns a collection with the current list of room moderators. The collection contains the
     * MUCRole of the occupants with moderator role.
     *
     * @return a collection with the current list of moderators.
     */
    public Collection<MUCRole> getModerators();

    /**
     * Returns a collection with the current list of room participants. The collection contains the
     * MUCRole of the occupants with participant role.
     *
     * @return a collection with the current list of moderators.
     */
    public Collection<MUCRole> getParticipants();

    /**
     * Returns true if every presence packet will include the JID of every occupant. This
     * configuration can be modified by the owner while editing the room's configuration.
     *
     * @return true if every presence packet will include the JID of every occupant.
     */
    public boolean canAnyoneDiscoverJID();

    /**
     * Sets if every presence packet will include the JID of every occupant. This
     * configuration can be modified by the owner while editing the room's configuration.
     *
     * @param canAnyoneDiscoverJID boolean that specifies if every presence packet will include the
     *        JID of every occupant.
     */
    public void setCanAnyoneDiscoverJID(boolean canAnyoneDiscoverJID);

    /**
     * Returns true if participants are allowed to change the room's subject.
     *
     * @return true if participants are allowed to change the room's subject.
     */
    public boolean canOccupantsChangeSubject();

    /**
     * Sets if participants are allowed to change the room's subject.
     *
     * @param canOccupantsChangeSubject boolean that specifies if participants are allowed to
     *        change the room's subject.
     */
    public void setCanOccupantsChangeSubject(boolean canOccupantsChangeSubject);

    /**
     * Returns true if occupants can invite other users to the room. If the room does not require an
     * invitation to enter (i.e. is not members-only) then any occupant can send invitations. On
     * the other hand, if the room is members-only and occupants cannot send invitation then only
     * the room owners and admins are allowed to send invitations.
     *
     * @return true if occupants can invite other users to the room.
     */
    public boolean canOccupantsInvite();

    /**
     * Sets if occupants can invite other users to the room. If the room does not require an
     * invitation to enter (i.e. is not members-only) then any occupant can send invitations. On
     * the other hand, if the room is members-only and occupants cannot send invitation then only
     * the room owners and admins are allowed to send invitations.
     *
     * @param canOccupantsInvite boolean that specified in any occupant can invite other users to
     *        the room.
     */
    public void setCanOccupantsInvite(boolean canOccupantsInvite);

    /**
     * Returns the natural language name of the room. This name can only be modified by room owners.
     * It's mainly used for users while discovering rooms hosted by the Multi-User Chat service.
     *
     * @return the natural language name of the room.
     */
    public String getNaturalLanguageName();

    /**
     * Sets the natural language name of the room. This name can only be modified by room owners.
     * It's mainly used for users while discovering rooms hosted by the Multi-User Chat service.
     *
     * @param naturalLanguageName the natural language name of the room.
     */
    public void setNaturalLanguageName(String naturalLanguageName);

    /**
     * Returns a description set by the room's owners about the room. This information will be used
     * when discovering extended information about the room.
     *
     * @return a description set by the room's owners about the room.
     */
    public String getDescription();

    /**
     * Sets a description set by the room's owners about the room. This information will be used
     * when discovering extended information about the room.
     *
     * @param description a description set by the room's owners about the room.
     */
    public void setDescription(String description);

    /**
     * Returns true if the room requires an invitation to enter. That is if the room is
     * members-only.
     *
     * @return true if the room requires an invitation to enter.
     */
    public boolean isMembersOnly();

    /**
     * Sets if the room requires an invitation to enter. That is if the room is members-only.
     *
     * @param membersOnly if true then the room is members-only.
     * @return the list of updated presences of all the occupants that aren't members of the room if
     *         the room is now members-only.
     */
    public List<Presence> setMembersOnly(boolean membersOnly);

    /**
     * Returns true if the room's conversation is being logged. If logging is activated the room
     * conversation will be saved to the database every couple of minutes. The saving frequency is
     * the same for all the rooms and can be configured by changing the property
     * "xmpp.muc.tasks.log.timeout" of MultiUserChatServerImpl.
     *
     * @return true if the room's conversation is being logged.
     */
    public boolean isLogEnabled();

    /**
     * Sets if the room's conversation is being logged. If logging is activated the room
     * conversation will be saved to the database every couple of minutes. The saving frequency is
     * the same for all the rooms and can be configured by changing the property
     * "xmpp.muc.tasks.log.timeout" of MultiUserChatServerImpl.
     *
     * @param logEnabled boolean that specified if the room's conversation must be logged.
     */
    public void setLogEnabled(boolean logEnabled);

    /**
     * Returns the maximum number of occupants that can be simultaneously in the room. If the number
     * is zero then there is no limit.
     *
     * @return the maximum number of occupants that can be simultaneously in the room. Zero means
     *         unlimited number of occupants.
     */
    public int getMaxUsers();

    /**
     * Sets the maximum number of occupants that can be simultaneously in the room. If the number
     * is zero then there is no limit.
     *
     * @param maxUsers the maximum number of occupants that can be simultaneously in the room. Zero
     *        means unlimited number of occupants.
     */
    public void setMaxUsers(int maxUsers);

    /**
     * Returns if the room in which only those with "voice" may send messages to all occupants.
     *
     * @return if the room in which only those with "voice" may send messages to all occupants.
     */
    public boolean isModerated();

    /**
     * Sets if the room in which only those with "voice" may send messages to all occupants.
     *
     * @param moderated if the room in which only those with "voice" may send messages to all
     *        occupants.
     */
    public void setModerated(boolean moderated);

    /**
     * Returns true if a user cannot enter without first providing the correct password.
     *
     * @return true if a user cannot enter without first providing the correct password.
     */
    public boolean isPasswordProtected();

    /**
     * Returns the password that the user must provide to enter the room.
     *
     * @return the password that the user must provide to enter the room.
     */
    public String getPassword();

    /**
     * Sets the password that the user must provide to enter the room.
     *
     * @param password the password that the user must provide to enter the room.
     */
    public void setPassword(String password);

    /**
     * Returns true if the room is not destroyed if the last occupant exits. Persistent rooms are
     * saved to the database to make their configurations persistent together with the affiliation
     * of the users.
     *
     * @return true if the room is not destroyed if the last occupant exits.
     */
    public boolean isPersistent();

    /**
     * Sets if the room is not destroyed if the last occupant exits. Persistent rooms are
     * saved to the database to make their configurations persistent together with the affiliation
     * of the users.
     *
     * @param persistent if the room is not destroyed if the last occupant exits.
     */
    public void setPersistent(boolean persistent);

    /**
     * Returns true if the room has already been made persistent. If the room is temporary the 
     * answer will always be false.
     * 
     * @return true if the room has already been made persistent.
     */
    public boolean wasSavedToDB();

    /**
     * Sets if the room has already been made persistent.
     * 
     * @param saved boolean that indicates if the room was saved to the database.
     */
    public void setSavedToDB(boolean saved);

    /**
     * Saves the room configuration to the DB. After the room has been saved to the DB it will
     * become persistent. 
     */
    public void saveToDB();

    /**
     * Returns true if the room is searchable and visible through service discovery. 
     * 
     * @return true if the room is searchable and visible through service discovery.
     */
    public boolean isPublicRoom();

    /**
     * Sets if the room is searchable and visible through service discovery.
     * 
     * @param publicRoom if the room is searchable and visible through service discovery.
     */
    public void setPublicRoom(boolean publicRoom);

    /**
     * Returns the list of roles of which presence will be broadcasted to the rest of the occupants.
     * This feature is useful for implementing "invisible" occupants.
     * 
     * @return the list of roles of which presence will be broadcasted to the rest of the occupants.
     */
    public List<String> getRolesToBroadcastPresence();

    /**
     * Sets the list of roles of which presence will be broadcasted to the rest of the occupants.
     * This feature is useful for implementing "invisible" occupants.
     * 
     * @param rolesToBroadcastPresence the list of roles of which presence will be broadcasted to 
     * the rest of the occupants.
     */
    public void setRolesToBroadcastPresence(List<String> rolesToBroadcastPresence);

    /**
     * Returns true if the presences of the requested role will be broadcasted.
     * 
     * @param roleToBroadcast the role to check if its presences will be broadcasted.
     * @return true if the presences of the requested role will be broadcasted.
     */
    public boolean canBroadcastPresence(String roleToBroadcast);

    /**
     * Locks the room so that users cannot join the room. Only the owner of the room can lock/unlock
     * the room.
     *
     * @param senderRole the role of the occupant that locked the room.
     * @throws ForbiddenException If the user is not an owner of the room.
     */
    public void lock(MUCRole senderRole) throws ForbiddenException;

    /**
     * Unlocks the room so that users can join the room. The room is locked when created and only
     * the owner of the room can unlock it by sending the configuration form to the Multi-User Chat
     * service.
     *
     * @param senderRole the role of the occupant that unlocked the room.
     * @throws ForbiddenException If the user is not an owner of the room.
     */
    public void unlock(MUCRole senderRole) throws ForbiddenException;

    /**
     * Sends an invitation to a user. The invitation will be sent as if the room is inviting the 
     * user. The invitation will include the original occupant the sent the invitation together with
     * the reason for the invitation if any. Since the invitee could be offline at the moment we 
     * need the originating session so that the offline strategy could potentially bounce the 
     * message with the invitation.
     * 
     * @param to the JID of the user that is being invited.
     * @param reason the reason of the invitation or null if none.
     * @param role the role of the occupant that sent the invitation.
     * @throws ForbiddenException If the user is not allowed to send the invitation.
     */
    public void sendInvitation(JID to, String reason, MUCRole role)
            throws ForbiddenException;

    /**
     * Sends the rejection to the inviter. The rejection will be sent as if the room is rejecting 
     * the invitation is named of the invitee. The rejection will include the address of the invitee
     * together with the reason for the rejection if any. Since the inviter could be offline at the 
     * moment we need the originating session so that the offline strategy could potentially bounce 
     * the message with the rejection.
     * 
     * @param to the JID of the user that is originated the invitation.
     * @param reason the reason for the rejection or null if none.
     * @param from the JID of the invitee that is rejecting the invitation.
     */
    public void sendInvitationRejection(JID to, String reason, JID from);
}