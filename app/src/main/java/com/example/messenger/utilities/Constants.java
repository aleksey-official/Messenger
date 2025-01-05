package com.example.messenger.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVED_ID = "receivedId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static final String REMOTE_MSG_TYPE = "type";
    public static final String REMOTE_MSG_INVITATION = "invitation";
    public static final String REMOTE_MSG_MEETING_TYPE = "meetingType";
    public static final String REMOTE_MSG_INVITER_TOKEN = "inviterToken";
    public static final String REMOTE_MSG_MSG_DATA = "data";
    public static final String REMOTE_MSG_INVITATION_RESPONCE = "invitationResponse";
    public static final String REMOTE_MSG_INVITATION_ACCEPTED = "accepted";
    public static final String REMOTE_MSG_INVITATION_REJECTED = "rejected";
    public static final String REMOTE_MSG_INVITATION_CANCELLED = "cancelled";
    public static final String REMOTE_MSG_MEETING_ROOM = "meetingRoom";
    public static final String DATE_AVAILABILITY = "dataAvailability";
    public static final String KEY_USER_PHONE = "phone";
    public static final String STATUS = "status";
    public static final String SEND_IMAGE = "sendImage";
    public static final String LINK = "link";
    public static final String SENDER_FILE = "senderFile";
    public static final String RECEIVER_FILE = "receiverFile";
    public static final String KEY_TYPING = "typing";
    public static final String THEME = "theme";
    public static final String DELIVERED = "delivered";
    public static final String READ = "read";
    public static final String MSG_ID = "msgId";
    public static final String SENDER_MESSAGE = "senderMessage";
    public static final String CONVERSION_ID = "conversionId";
    public static final String BLOCKED_USERS = "blockedUsers";
    public static final String RESEND = "resend";
    public static final String PASSCODE = "passcode";
    public static final String BIRTHDAY = "birthday";
    public static final String FINGER = "finger";
    public static final String EMAIL_PASSWORD = "RbFbGj2aFiR9CXQbaMLs";
    public static final String EMAIL_DEVELOPER = "uptech.messenger@mail.ru";


    public static HashMap<String, String> remoteMsgHeaders = null;
    public static HashMap<String, String> getRemoteMsgHeaders(){
        if (remoteMsgHeaders == null){
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAEngHFww:APA91bEvrL9H-5_CFVmLJa2tOoBjKC5BRs0t07T_HiWbCijoZ05E22nP0xRlSv-gz5AF2fuIzjqwXkUUDvMsXxff4YFkNYahSvL4RyHyLXO_RfAcxY01LU54NPW7hJjY8NBqXrk3qagb"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }
}
