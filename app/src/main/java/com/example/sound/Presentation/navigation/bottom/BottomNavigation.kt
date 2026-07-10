package com.example.sound.Presentation.navigation.bottom

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController


@Composable
fun BottomNavigation(
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination
    NavigationBar(
        modifier = Modifier.wrapContentSize()
    ) {
        BottomScreen.items.forEach { screen ->

            NavigationBarItem(
                selected = currentRoute?.hierarchy?.any { it.route == screen.route::class.qualifiedName || it.route == screen.route.toString() } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(
                            navController.graph.findStartDestination().id
                        ) {
                            saveState = true
                        }

                        launchSingleTop = true
                        restoreState = true
                    }
                },

                icon = {
                    Icon(
                        modifier = Modifier.size(34.dp),
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                    )
                },

                label = {
                    Text(screen.title)
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.LightGray,
                    unselectedTextColor = Color.LightGray
                )
            )
        }

    }
}


@Composable
@Preview
private fun PreviewBottomNavigation() {
    BottomNavigation(
        navController = rememberNavController()
    )
}