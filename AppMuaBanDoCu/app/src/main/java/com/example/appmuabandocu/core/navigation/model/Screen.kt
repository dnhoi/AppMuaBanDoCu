package com.example.appmuabandocu.core.navigation.model

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object SplashRole : Screen("splashRole")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object AddProduct : Screen("add_product")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    object Category : Screen("category_screen")
    object Favorite : Screen("favorite_screen")
    object ManageProduct : Screen("manage_product")
    object ProfileDetail : Screen("profile_detail/{userId}") {
        fun createRoute(userId: String) = "profile_detail/$userId"
    }
    object HomeMxh : Screen("home_mxh")
    object ProfileUser : Screen("profile_user/{userId}") {
        fun createRoute(userId: String) = "profile_user/$userId"
    }
    object AdminDashboard : Screen("admin_dashboard")
    object ChatList : Screen("chat_list")
    object Chat : Screen("chat/{receiverId}/{receiverName}?productId={productId}") {
        fun createRoute(receiverId: String, receiverName: String, productId: String = ""): String {
            val encodedName = try {
                java.net.URLEncoder.encode(receiverName, "UTF-8")
            } catch (_: Exception) {
                receiverName
            }
            return "chat/$receiverId/$encodedName?productId=$productId"
        }
    }
    object TransactionHistory : Screen("transaction_history")
    object HelpCenter : Screen("help_center")
    object AboutApp : Screen("about_app")
    object Reviews : Screen("reviews/{userId}/{userName}") {
        fun createRoute(userId: String, userName: String): String {
            val encoded = try {
                java.net.URLEncoder.encode(userName, "UTF-8")
            } catch (_: Exception) {
                userName
            }
            return "reviews/$userId/$encoded"
        }
    }
    object Notifications : Screen("notifications_screen")
}