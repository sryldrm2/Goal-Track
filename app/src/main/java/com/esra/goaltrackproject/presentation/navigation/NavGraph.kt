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
import com.esra.goaltrackproject.presentation.addgoal.AddGoalScreen
import com.esra.goaltrackproject.presentation.addgoal.AddGoalViewModel
import com.esra.goaltrackproject.presentation.dashboard.DashboardScreen
import com.esra.goaltrackproject.presentation.dashboard.DashboardViewModel
import com.esra.goaltrackproject.presentation.detail.GoalDetailScreen
import com.esra.goaltrackproject.presentation.detail.GoalDetailViewModel
import com.esra.goaltrackproject.presentation.CustomViewModelFactory

@Composable
fun GoalNavGraph(goalDao: GoalDao) {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as Application
    val factory = remember { CustomViewModelFactory(application, goalDao) }

    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD
    ) {
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