package com.example.notify

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notify.screens.AllTasks
import com.example.notify.screens.Create
import com.example.notify.screens.HomeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navItemList=listOf(
        Navitem("Home", Icons.Default.Home),
        Navitem("Create", Icons.Default.Create),
        Navitem("AllTasks", Icons.Default.Star)
    )
    var selectedIndex by remember{
        mutableIntStateOf(0)

    }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state= rememberTopAppBarState()
    )
    Scaffold (
        modifier= Modifier.fillMaxSize(),
        topBar = {
            TopBar(scrollBehavior=scrollBehavior )
        },
        bottomBar = {
            NavigationBar {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected =selectedIndex==index,
                        onClick={
                            selectedIndex=index
                        },
                        icon={
                            Icon(
                                imageVector = navItem.icon,
                                contentDescription = "Icon",
                            )
                        },
                        label={
                            Text(text=navItem.label)
                        }
                    )
                }
            }
        }
    ){innerPadding ->
        ContentScreen(modifier = Modifier.padding(innerPadding),selectedIndex)

    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(modifier: Modifier = Modifier,
           scrollBehavior: TopAppBarScrollBehavior
){
    TopAppBar(
        modifier=modifier
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(100.dp)),
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        windowInsets = WindowInsets(top=0.dp),
        title = {
            Text(
                text = "Search",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 17.sp
            )
        },
        navigationIcon = {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "null",
                modifier = Modifier
                    .padding(start = 16.dp ,end=8.dp)
                    .size(27.dp)
            )
        },
        actions = {
            Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = "null",
                modifier = Modifier
                    .padding(start = 16.dp ,end=8.dp)
            )
        }
    )
}

@Composable
fun ContentScreen(modifier: Modifier = Modifier, selectedIndex: Int){
    when(selectedIndex){
        0->HomeScreen()
        1->Create()
        2->AllTasks()
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview(){
    MainScreen()
}