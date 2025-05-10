package com.example.appmuabandocu.core.navigation

import android.app.Activity.RESULT_OK
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appmuabandocu.core.navigation.model.Screen
import com.example.appmuabandocu.core.ui.SplashRoleScreen
import com.example.appmuabandocu.core.ui.SplashScreen
import com.example.appmuabandocu.feature_add_product.ui.AddProductScreen
import com.example.appmuabandocu.feature_add_product.ui.CategoryScreen
import com.example.appmuabandocu.feature_auth.LoginScreen
import com.example.appmuabandocu.feature_auth.RegisterMainScreen
import com.example.appmuabandocu.feature_favorite.FavoriteScreen
import com.example.appmuabandocu.feature_home.HomeScreen
import com.example.appmuabandocu.feature_mxh.MxhScreen
import com.example.appmuabandocu.feature_product.ProductDetailScreen
import com.example.appmuabandocu.feature_profile.ManageProductScreen
import com.example.appmuabandocu.feature_profile.ProfileDetailScreen
import com.example.appmuabandocu.feature_profile.ProfileScreen
import com.example.appmuabandocu.feature_profile.ProfileUserScreen
import com.example.appmuabandocu.viewmodel.AuthViewModel
import com.example.appmuabandocu.viewmodel.FavoriteViewModel
import com.example.appmuabandocu.viewmodel.ManageProductViewModel
import com.example.appmuabandocu.viewmodel.ProductViewModel
import com.example.appmuabandocu.viewmodel.ProfileViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private const val THOI_GIAN_CHUYEN_DONG = 500

private fun AnimatedContentTransitionScope<NavBackStackEntry>.taoHieuUngVao(): EnterTransition {
    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(THOI_GIAN_CHUYEN_DONG)
    ) + fadeIn(animationSpec = tween(THOI_GIAN_CHUYEN_DONG))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.taoHieuUngRa(): ExitTransition {
    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(THOI_GIAN_CHUYEN_DONG)
    ) + fadeOut(animationSpec = tween(THOI_GIAN_CHUYEN_DONG))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.taoHieuUngQuayLai(): EnterTransition {
    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(THOI_GIAN_CHUYEN_DONG)
    ) + fadeIn(animationSpec = tween(THOI_GIAN_CHUYEN_DONG))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.taoHieuUngThoatQuayLai(): ExitTransition {
    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(THOI_GIAN_CHUYEN_DONG)
    ) + fadeOut(animationSpec = tween(THOI_GIAN_CHUYEN_DONG))
}

@Composable
fun MyAppNavigation(
) {

    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val authViewModel: AuthViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    val favoriteViewModel: FavoriteViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val manageProductViewModel: ManageProductViewModel = viewModel()

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val currentUserData by profileViewModel.userData.collectAsState()

    val context = LocalContext.current

    // Tự động đăng xuất nếu tài khoản bị Admin khóa
    LaunchedEffect(currentUserData?.isBanned) {
        if (currentUserData?.isBanned == true) {
            val reason = currentUserData?.banReason.orEmpty().ifEmpty { "Vi phạm tiêu chuẩn cộng đồng" }
            Toast.makeText(
                context,
                "Tài khoản của bạn đã bị khóa: $reason. Hệ thống tự động đăng xuất.",
                Toast.LENGTH_LONG
            ).show()
            authViewModel.signOut(context)
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(
            Screen.Splash.route ,
            enterTransition = { taoHieuUngVao() },
            exitTransition = { taoHieuUngRa() },
            popEnterTransition = { taoHieuUngQuayLai() },
            popExitTransition = { taoHieuUngThoatQuayLai() }
        ) {
            SplashScreen(onSplashFinished = {
                navController.navigate(Screen.SplashRole.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }
        composable(
            Screen.SplashRole.route,
            enterTransition = { taoHieuUngVao() },
            exitTransition = { taoHieuUngRa() },
            popEnterTransition = { taoHieuUngQuayLai() },
            popExitTransition = { taoHieuUngThoatQuayLai() }
        ) {
            SplashRoleScreen(
                navController
            )
        }
        composable(
            Screen.Login.route,
            enterTransition = { taoHieuUngVao() },
            exitTransition = { taoHieuUngRa() },
            popEnterTransition = { taoHieuUngQuayLai() },
            popExitTransition = { taoHieuUngThoatQuayLai() }
        ) {
            LoginScreen(
                authViewModel,
                navController,
            )
        }
        composable(
            Screen.Register.route,
            enterTransition = { taoHieuUngVao() },
            exitTransition = { taoHieuUngRa() },
            popEnterTransition = { taoHieuUngQuayLai() },
            popExitTransition = { taoHieuUngThoatQuayLai() }
        ) {
            RegisterMainScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(
            Screen.HomeMxh.route,
            enterTransition = { taoHieuUngVao() },
            exitTransition = { taoHieuUngRa() },
            popEnterTransition = { taoHieuUngQuayLai() },
            popExitTransition = { taoHieuUngThoatQuayLai() }
        ) {
            MxhScreen(
                navController = navController,
                productViewModel = productViewModel,
            )
        }
        composable(Screen.Home.route,
            enterTransition = { taoHieuUngVao() },
            exitTransition = { taoHieuUngRa() },
            popEnterTransition = { taoHieuUngQuayLai() },
            popExitTransition = { taoHieuUngThoatQuayLai() }
        ) {
            HomeScreen(
                navController = navController
            )
        }
        composable(Screen.AddProduct.route) {
            AddProductScreen(
                category = "",
                navController = navController
            )
        }
        composable(Screen.Category.route,
            enterTransition = { taoHieuUngVao() },
            exitTransition = { taoHieuUngRa() },
            popEnterTransition = { taoHieuUngQuayLai() },
            popExitTransition = { taoHieuUngThoatQuayLai() }
        ) {
            CategoryScreen(
                navController = navController,
                auth = auth // Pass FirebaseAuth instance
            )
        }
        composable(Screen.Favorite.route,
            enterTransition = { taoHieuUngVao() },
            exitTransition = { taoHieuUngRa() },
            popEnterTransition = { taoHieuUngQuayLai() },
            popExitTransition = { taoHieuUngThoatQuayLai() }
        ) {
            FavoriteScreen(
                navController = navController,
                viewModel = productViewModel,
                favoriteViewModel = favoriteViewModel,
            )
        }

        composable(Screen.Profile.route,
            enterTransition = { taoHieuUngVao() },
            exitTransition = { taoHieuUngRa() },
            popEnterTransition = { taoHieuUngQuayLai() },
            popExitTransition = { taoHieuUngThoatQuayLai() }
        ) {
            ProfileScreen(
                auth = auth,
                onSignOut = {
                    authViewModel.signOut(context)
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Profile.route) { inclusive = true }
                    }
                },
                navController = navController,
                profileViewModel = profileViewModel,
                manageProductViewModel = manageProductViewModel,
                favoriteViewModel

            )
        }
        composable(Screen.ManageProduct.route) {
            ManageProductScreen(
                navController = navController,
                viewModel = manageProductViewModel
            )
        }
        composable(Screen.ProfileDetail.route,
            enterTransition = { taoHieuUngVao() },
            exitTransition = { taoHieuUngRa() },
            popEnterTransition = { taoHieuUngQuayLai() },
            popExitTransition = { taoHieuUngThoatQuayLai() }
        ) {
            ProfileDetailScreen(
                navController,
                auth,
                profileViewModel = profileViewModel
            )
        }

        composable(Screen.ProductDetail.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(navController = navController, id = productId)
        }

        composable(
            Screen.ProfileUser.route,
        ) {
            val userId = it.arguments?.getString("userId") ?: ""
            ProfileUserScreen(
                navController = navController,
                userId = userId
            )
        }

        composable(
            Screen.AdminDashboard.route,
            enterTransition = { taoHieuUngVao() },
            exitTransition = { taoHieuUngRa() },
            popEnterTransition = { taoHieuUngQuayLai() },
            popExitTransition = { taoHieuUngThoatQuayLai() }
        ) {
            com.example.appmuabandocu.feature_admin.AdminDashboardScreen(
                navController = navController
            )
        }

        composable(
            Screen.ChatList.route,
            enterTransition = { taoHieuUngVao() },
            exitTransition = { taoHieuUngRa() },
            popEnterTransition = { taoHieuUngQuayLai() },
            popExitTransition = { taoHieuUngThoatQuayLai() }
        ) {
            com.example.appmuabandocu.feature_chat.ChatListScreen(
                navController = navController
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                androidx.navigation.navArgument("receiverId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("receiverName") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("productId") {
                    type = androidx.navigation.NavType.StringType
                    defaultValue = ""
                }
            ),
            enterTransition = { taoHieuUngVao() },
            exitTransition = { taoHieuUngRa() },
            popEnterTransition = { taoHieuUngQuayLai() },
            popExitTransition = { taoHieuUngThoatQuayLai() }
        ) { backStackEntry ->
            val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""
            val receiverName = backStackEntry.arguments?.getString("receiverName") ?: ""
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            com.example.appmuabandocu.feature_chat.ChatScreen(
                navController = navController,
                receiverId = receiverId,
                receiverName = receiverName,
                productId = productId
            )
        }

        composable(
            Screen.TransactionHistory.route,
            enterTransition = { taoHieuUngVao() },
            exitTransition = { taoHieuUngRa() },
            popEnterTransition = { taoHieuUngQuayLai() },
            popExitTransition = { taoHieuUngThoatQuayLai() }
        ) {
            com.example.appmuabandocu.feature_profile.TransactionHistoryScreen(
                navController = navController
            )
        }

        composable(
            Screen.HelpCenter.route,
            enterTransition = { taoHieuUngVao() },
            exitTransition = { taoHieuUngRa() },
            popEnterTransition = { taoHieuUngQuayLai() },
            popExitTransition = { taoHieuUngThoatQuayLai() }
        ) {
            com.example.appmuabandocu.feature_profile.HelpCenterScreen(
                navController = navController
            )
        }

        composable(
            Screen.AboutApp.route,
            enterTransition = { taoHieuUngVao() },
            exitTransition = { taoHieuUngRa() },
            popEnterTransition = { taoHieuUngQuayLai() },
            popExitTransition = { taoHieuUngThoatQuayLai() }
        ) {
            com.example.appmuabandocu.feature_profile.AboutAppScreen(
                navController = navController
            )
        }

        composable(
            route = Screen.Reviews.route,
            arguments = listOf(
                androidx.navigation.navArgument("userId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("userName") { type = androidx.navigation.NavType.StringType }
            ),
            enterTransition = { taoHieuUngVao() },
            exitTransition = { taoHieuUngRa() },
            popEnterTransition = { taoHieuUngQuayLai() },
            popExitTransition = { taoHieuUngThoatQuayLai() }
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            com.example.appmuabandocu.feature_profile.ReviewsScreen(
                navController = navController,
                userId = userId,
                userName = userName
            )
        }

        composable(
            Screen.Notifications.route,
            enterTransition = { taoHieuUngVao() },
            exitTransition = { taoHieuUngRa() },
            popEnterTransition = { taoHieuUngQuayLai() },
            popExitTransition = { taoHieuUngThoatQuayLai() }
        ) {
            com.example.appmuabandocu.feature_notification.NotificationsScreen(
                navController = navController
            )
        }
    }
}