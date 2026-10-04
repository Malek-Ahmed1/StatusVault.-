package com.kaboas.statusvault.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class UserMessage(
    val id: String = "",
    val type: String = "",
    val message: String = "",
    val reply: String = "",
    val status: String = "",
    val timestamp: Long = 0L
)

object MessageRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getMessagesWithReplies(): List<UserMessage> {
        return try {
            val snapshot = db.collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val type = doc.getString("type") ?: return@mapNotNull null
                val msg = doc.getString("message") ?: ""
                val reply = doc.getString("reply") ?: ""
                val status = doc.getString("status") ?: ""
                val ts = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L

                UserMessage(
                    id = doc.id,
                    type = type,
                    message = msg,
                    reply = reply,
                    status = status,
                    timestamp = ts
                )
            }.filter { it.reply.isNotEmpty() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getReplyCountByType(type: String, lastSeen: Long): Int {
        return try {
            val snapshot = db.collection("messages")
                .whereEqualTo("type", type)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.count { doc ->
                val reply = doc.getString("reply") ?: ""
                val ts = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                reply.isNotEmpty() && ts > lastSeen
            }
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getTotalReplyCount(lastSeen: Long): Int {
        return try {
            val snapshot = db.collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.count { doc ->
                val reply = doc.getString("reply") ?: ""
                val ts = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                reply.isNotEmpty() && ts > lastSeen
            }
        } catch (e: Exception) {
            0
        }
    }
}
