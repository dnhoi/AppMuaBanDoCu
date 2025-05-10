package com.example.appmuabandocu.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmuabandocu.model.ChatConversation
import com.example.appmuabandocu.model.ChatMessage
import com.example.appmuabandocu.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class ChatViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()
    private val messagesRef = db.getReference("messages")
    private val conversationsRef = db.getReference("conversations")

    private val _messageList = mutableStateListOf<ChatMessage>()
    val messageList: List<ChatMessage> get() = _messageList

    private val _conversationList = mutableStateListOf<ChatConversation>()
    val conversationList: List<ChatConversation> get() = _conversationList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var activeChannelListener: ValueEventListener? = null
    private var activeChannelId: String? = null
    private var conversationsListener: ValueEventListener? = null

    init {
        loadConversations()
    }

    fun getChatChannelId(userA: String, userB: String): String {
        return if (userA < userB) "${userA}_$userB" else "${userB}_$userA"
    }

    fun loadConversations() {
        val currentUserId = auth.currentUser?.uid ?: return
        conversationsListener?.let { conversationsRef.child(currentUserId).removeEventListener(it) }

        conversationsListener = conversationsRef.child(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<ChatConversation>()
                    snapshot.children.forEach { child ->
                        val conv = child.getValue(ChatConversation::class.java)
                        conv?.let { list.add(it) }
                    }
                    _conversationList.clear()
                    _conversationList.addAll(list.sortedByDescending { it.lastTimestamp })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatViewModel", "Error loading conversations: ${error.message}")
                }
            })
    }

    fun listenToMessages(otherUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val channelId = getChatChannelId(currentUserId, otherUserId)

        if (activeChannelId == channelId && activeChannelListener != null) return

        // Clean up previous listener
        activeChannelId?.let { chId ->
            activeChannelListener?.let { messagesRef.child(chId).removeEventListener(it) }
        }

        activeChannelId = channelId
        _isLoading.value = true

        activeChannelListener = messagesRef.child(channelId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<ChatMessage>()
                    snapshot.children.forEach { child ->
                        val msg = child.getValue(ChatMessage::class.java)
                        msg?.let { list.add(it) }
                    }
                    _messageList.clear()
                    _messageList.addAll(list.sortedBy { it.timestamp })
                    _isLoading.value = false
                }

                override fun onCancelled(error: DatabaseError) {
                    _isLoading.value = false
                    Log.e("ChatViewModel", "Error loading messages: ${error.message}")
                }
            })
    }

    fun sendMessage(
        otherUserId: String,
        otherUserName: String,
        otherUserAvatar: String,
        text: String,
        product: Product? = null
    ) {
        val currentUserId = auth.currentUser?.uid ?: return
        val currentUserName = auth.currentUser?.displayName ?: "Người mua"
        val currentUserAvatar = auth.currentUser?.photoUrl?.toString() ?: ""
        val trimmed = text.trim()
        if (trimmed.isEmpty() && product == null) return

        val channelId = getChatChannelId(currentUserId, otherUserId)
        val now = Date().time
        val msgRef = messagesRef.child(channelId).push()
        val msgId = msgRef.key ?: return

        val chatMessage = ChatMessage(
            id = msgId,
            senderId = currentUserId,
            receiverId = otherUserId,
            message = trimmed,
            timestamp = now,
            productId = product?.id ?: "",
            productName = product?.productName ?: "",
            productPrice = product?.price ?: "",
            productImage = product?.imageUrl?.ifEmpty { product.imageMota.firstOrNull() ?: "" } ?: "",
            read = false
        )

        viewModelScope.launch {
            try {
                // 1. Lưu tin nhắn vào phòng chat
                msgRef.setValue(chatMessage).await()

                // 2. Cập nhật conversation cho người gửi
                val convForSender = ChatConversation(
                    id = otherUserId,
                    otherUserId = otherUserId,
                    otherUserName = otherUserName,
                    otherUserAvatar = otherUserAvatar,
                    lastMessage = if (trimmed.isNotEmpty()) trimmed else "[Đã gửi bài đăng: ${product?.productName}]",
                    lastTimestamp = now,
                    unreadCount = 0,
                    productId = product?.id ?: "",
                    productName = product?.productName ?: ""
                )
                conversationsRef.child(currentUserId).child(otherUserId).setValue(convForSender).await()

                // 3. Cập nhật conversation cho người nhận
                val convForReceiver = ChatConversation(
                    id = currentUserId,
                    otherUserId = currentUserId,
                    otherUserName = currentUserName,
                    otherUserAvatar = currentUserAvatar,
                    lastMessage = if (trimmed.isNotEmpty()) trimmed else "[Đã gửi bài đăng: ${product?.productName}]",
                    lastTimestamp = now,
                    unreadCount = 1,
                    productId = product?.id ?: "",
                    productName = product?.productName ?: ""
                )
                conversationsRef.child(otherUserId).child(currentUserId).setValue(convForReceiver).await()

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        activeChannelId?.let { chId ->
            activeChannelListener?.let { messagesRef.child(chId).removeEventListener(it) }
        }
        auth.currentUser?.uid?.let { uid ->
            conversationsListener?.let { conversationsRef.child(uid).removeEventListener(it) }
        }
    }
}
