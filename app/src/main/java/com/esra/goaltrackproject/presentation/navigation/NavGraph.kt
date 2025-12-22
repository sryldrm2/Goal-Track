package com.esra.goaltrackproject.presentation.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.esra.goaltrackproject.data.GoalDao
import com.esra.goaltrackproject.data.GoalDatabase
import com.esra.goaltrackproject.data.UserPreferences
import com.esra.goaltrackproject.presentation.addgoal.AddGoalScreen
import com.esra.goaltrackproject.presentation.addgoal.AddGoalViewModel
import com.esra.goaltrackproject.presentation.dashboard.DashboardScreen
import com.esra.goaltrackproject.presentation.dashboard.DashboardViewModel
import com.esra.goaltrackproject.presentation.detail.GoalDetailScreen
import com.esra.goaltrackproject.presentation.detail.GoalDetailViewModel
import com.esra.goaltrackproject.presentation.CustomViewModelFactory
import com.esra.goaltrackproject.presentation.onboarding.OnBoardingScreen

@Composable
fun GoalNavGraph(goalDao: GoalDao) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val application = context.applicationContext as Application

    val database = remember { GoalDatabase.getDatabase(context) }
    val userPreferences = remember { UserPreferences(context) }

    val factory = remember {
        CustomViewModelFactory(
            application = application,
            goalDao = goalDao,
            notificationDao = database.notificationDao()
        ) }

    val startRoute = if (userPreferences.isOnboardingCompleted()) Routes.DASHBOARD else Routes.ONBOARDING

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(Routes.ONBOARDING) {
            OnBoardingScreen(
                onFinished = { name, avatarId ->
                    userPreferences.saveUserName(name)
                    userPreferences.saveAvatar(avatarId)
                    userPreferences.setOnboardingCompleted()
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            val viewModel: DashboardViewModel = viewModel(factory = factory)

            DashboardScreen(
                viewModel = viewModel,
                onGoalClick = { goalId ->
                    navController.navigate("${Routes.DETAIL}/$goalId")
                },
                onAddGoalClick = {
                    navController.navigate(Routes.ADD_GOAL)
                }
            )
        }

        composable(Routes.ADD_GOAL) {
            val viewModel: AddGoalViewModel = viewModel(factory = factory)
            AddGoalScreen(
                viewModel = viewModel,
                onGoalSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "${Routes.DETAIL}/{goalId}",
            arguments = listOf(
                navArgument("goalId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getInt("goalId") ?: return@composable
            val viewModel: GoalDetailViewModel = viewModel(factory = factory) // ViewModel burada tanımlanmalı

            GoalDetailScreen(
                goalId = goalId,
                viewModel = viewModel,
                onDeleteSuccess = {
                    navController.popBackStack(Routes.DASHBOARD, inclusive = false)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}