package com.esra.goaltrackproject.presentation.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esra.goaltrackproject.R

@Composable
fun OnBoardingScreen(
    onFinished: (String, Int) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var userName by remember { mutableStateOf("") }
    var selectedAvatarResId by remember { mutableIntStateOf(R.drawable.avatar1) }

    val primaryYellow = Color(0xFFFDD835)
    val backgroundWhite = Color(0xFFF8F8F5)

    Box(modifier = Modifier.fillMaxSize().background(backgroundWhite)) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn() + slideInHorizontally { it } togetherWith fadeOut() + slideOutHorizontally { -it }
            }, label = "onboarding_transition"
        ) { step ->
            when (step) {
                1 -> WelcomeStep(primaryYellow) { currentStep = 2 }
                2 -> NameInputStep(userName, primaryYellow, onNameChange = { userName = it }) { currentStep = 3 }
                3 -> AvatarSelectStep(selectedAvatarResId, primaryYellow, onAvatarSelected = { selectedAvatarResId = it }) { currentStep = 4}
                4 -> FinalStep(userName, selectedAvatarResId,primaryYellow) { onFinished(userName, selectedAvatarResId) }
            }
        }
    }
}

@Composable
fun WelcomeStep(primary: Color, onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Alışkanlıklarını\nYönet",
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black,
            lineHeight = 48.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Akıllı hedefler, kolay takip.\nSMART metodolojisi ile hedeflerine ulaş",
            color = Color.Gray,
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Hadi Başlayalım", color = primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Icon(Icons.Default.ArrowForward, null, tint = primary, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
fun NameInputStep(name: String, primary: Color, onNameChange: (String) -> Unit, onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sana nasıl\nhitap edebilirim?",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text("Adınızı yazın...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primary,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = Color.Black,
                cursorColor = Color.Black
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onNext,
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Devam Et", color = primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun AvatarSelectStep(
    selectedId: Int,
    primary: Color,
    onAvatarSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    val avatarList = listOf(
        R.drawable.avatar1,
        R.drawable.avatar2,
        R.drawable.avatar3,
        R.drawable.avatar4,
        R.drawable.avatar5,
        R.drawable.avatar6,
        R.drawable.avatar7,
        R.drawable.avatar8
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Seni temsil eden\navatarı seç", fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.height(300.dp)
        ) {
            items(avatarList) { avatarRes ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(if (selectedId == avatarRes) primary else Color.White)
                        .border(
                            width = if (selectedId == avatarRes) 3.dp else 1.dp,
                            color = if (selectedId == avatarRes) Color.Black else Color.LightGray,
                            shape = CircleShape
                        )
                        .clickable { onAvatarSelected(avatarRes) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = avatarRes),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Harika Görünüyor", color = primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FinalStep(name: String, avatarResId: Int, primary: Color, onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = avatarResId),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(4.dp, primary, CircleShape)
        )

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Her şey hazır,\n$name!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Günlük takip et, istikrarlı kal,\nhedeflerine bir adım daha yaklaş.",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Yolculuğa Başla", color = primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}













