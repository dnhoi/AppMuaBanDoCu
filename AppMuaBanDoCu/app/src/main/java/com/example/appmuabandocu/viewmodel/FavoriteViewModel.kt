package com.example.appmuabandocu.viewmodel

     import android.util.Log
     import androidx.lifecycle.ViewModel
     import com.google.firebase.auth.FirebaseAuth
     import com.google.firebase.database.DataSnapshot
     import com.google.firebase.database.DatabaseError
     import com.google.firebase.database.FirebaseDatabase
     import com.google.firebase.database.ValueEventListener
     import kotlinx.coroutines.flow.MutableStateFlow
     import kotlinx.coroutines.flow.StateFlow
     import kotlinx.coroutines.flow.asStateFlow

class FavoriteViewModel : ViewModel() {

         private val _favoriteProductIds = MutableStateFlow<Set<String>>(emptySet())
         val favoriteProductIds: StateFlow<Set<String>> = _favoriteProductIds

         private val db = FirebaseDatabase.getInstance().getReference("users")
         private val auth = FirebaseAuth.getInstance()

         private val _isLoading = MutableStateFlow(true)
         val isLoading = _isLoading.asStateFlow()

         private var valueEventListener: ValueEventListener? = null

         init {
             setupFavoritesListener()
         }

         private fun setupFavoritesListener() {
             // Check if user is logged in before setting up listener
             val currentUserId = auth.currentUser?.uid
             if (currentUserId != null) {
                 loadFavorites(currentUserId)
             } else {
                 _favoriteProductIds.value = emptySet()
             }

             // Add auth state listener to handle login/logout
             auth.addAuthStateListener { firebaseAuth ->
                 val userId = firebaseAuth.currentUser?.uid
                 if (userId != null) {
                     loadFavorites(userId)
                 } else {
                     // Remove previous listener if any
                     removeValueEventListener()
                     _favoriteProductIds.value = emptySet()
                 }
             }
         }

         private fun removeValueEventListener() {
             val userId = auth.currentUser?.uid
             valueEventListener?.let { listener ->
                 userId?.let { uid ->
                     db.child(uid).child("favorites").removeEventListener(listener)
                 }
                 valueEventListener = null
             }
         }

         private fun loadFavorites(userId: String) {

             _isLoading.value = true
             // Remove any existing listener
             removeValueEventListener()

             // Create and add new listener
             valueEventListener = object : ValueEventListener {
                 override fun onDataChange(snapshot: DataSnapshot) {
                     val favorites = snapshot.children.mapNotNull { it.key }.toSet()
                     _favoriteProductIds.value = favorites
                     _isLoading.value = false
                     Log.d("FavoriteViewModel", "Favorites loaded: $favorites")
                 }

                 override fun onCancelled(error: DatabaseError) {
                     _isLoading.value = false
                     Log.e("FavoriteViewModel", "Error loading favorites: ${error.message}")
                 }
             }

             db.child(userId).child("favorites").addValueEventListener(valueEventListener!!)
         }

         fun toggleFavorite(productId: String) {
             val userId = auth.currentUser?.uid
             if (userId == null) {
                 Log.d("FavoriteViewModel", "Cannot toggle favorite: User not logged in")
                 return
             }

             val favoriteRef = db.child(userId).child("favorites").child(productId)

             // Check if product is already favorited
             if (_favoriteProductIds.value.contains(productId)) {
                 // If favorited, remove from favorites
                 favoriteRef.removeValue().addOnSuccessListener {
                     Log.d("FavoriteViewModel", "Product removed from favorites: $productId")
                 }.addOnFailureListener {
                     Log.e("FavoriteViewModel", "Error removing product from favorites: ${it.message}")
                 }
             } else {
                 // If not favorited, add to favorites
                 favoriteRef.setValue(true).addOnSuccessListener {
                     Log.d("FavoriteViewModel", "Product added to favorites: $productId")
                 }.addOnFailureListener {
                     Log.e("FavoriteViewModel", "Error adding product to favorites: ${it.message}")
                 }
             }
         }

         override fun onCleared() {
             super.onCleared()
             removeValueEventListener()
         }
     }