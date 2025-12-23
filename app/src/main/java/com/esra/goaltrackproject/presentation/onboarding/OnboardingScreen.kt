package com.esra.goaltrackproject.presentation.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.esra.goaltrackproject.R

@Composable
fun OnBoardingScreen(
    onFinished: (String, Int) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var userName by remember { mutableStateOf("") }
    var selectedAvatarResId by remember { mutableIntStateOf(R.drawable.avatar1) }
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.background,
                        colorScheme.primary.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn() + slideInHorizontally { it } togetherWith fadeOut() + slideOutHorizontally { -it }
            }, label = "onboarding_transition"
        ) { step ->
            when (step) {
                1 -> WelcomeStep { currentStep = 2 }
                2 -> NameInputStep(userName, onNameChange = { userName = it }) { currentStep = 3 }
                3 -> AvatarSelectStep(selectedAvatarResId, onAvatarSelected = { selectedAvatarResId = it }) { currentStep = 4}
                4 -> FinalStep(userName, selectedAvatarResId) { onFinished(userName, selectedAvatarResId) }
            }
        }
        
        // Step indicators
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                val isSelected = currentStep == index + 1
                val indicatorSize by animateDpAsState(if (isSelected) 12.dp else 8.dp,  label = "")
                val color = if (isSelected) colorScheme.secondary else colorScheme.primary.copy(alpha = 0.2f)
                Box(
                    modifier = Modifier
                        .size(indicatorSize)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun WelcomeStep(onNext: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Alışkanlıklarını\nYönet",
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            color = colorScheme.onBackground,
            lineHeight = 48.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        LottieAnimationView(
            resId = R.raw.goal,
            modifier = Modifier.size(240.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Akıllı hedefler, kolay takip.\nSMART metodolojisi ile hedeflerine ulaş",
            color = colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.weight(1.2f))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Hadi Başlayalım", color = colorScheme.onPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Icon(Icons.Default.ArrowForward, null, tint = colorScheme.onPrimary, modifier = Modifier.padding(start = 8.dp))
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun NameInputStep(name: String, onNameChange: (String) -> Unit, onNext: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp)
    ) {
        Spacer(modifier = Modifier.weight(0.6f))
        Text(
            text = "Sana nasıl\nhitap edebilirim?",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground,
            lineHeight = 44.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Deneyiminizi kişiselleştirmek için adınıza ihtiyacımız var.",
            fontSize = 14.sp,
            color = colorScheme.onBackground.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(48.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text("Adınızı yazın...", color = colorScheme.onSurface.copy(alpha = 0.4f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorScheme.primary,
                cursorColor = colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onNext,
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Devam Et", color = colorScheme.onPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun AvatarSelectStep(
    selectedId: Int,
    onAvatarSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.4f))
        Text("Seni temsil eden\navatarı seç", fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = colorScheme.onBackground)
        Spacer(modifier = Modifier.height(48.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        ) {
            items(avatarList) { avatarRes ->
                val isSelected = selectedId == avatarRes
                val scale by animateFloatAsState(
                    targetValue = if (selectedId == avatarRes) 1.15f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "avatar_scale"
                )
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .graphicsLayer{
                            scaleX = scale
                            scaleY = scale }
                        .clip(CircleShape)
                        .background(if (isSelected) colorScheme.secondary else colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) colorScheme.onSecondary else colorScheme.outline.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .clickable { onAvatarSelected(avatarRes) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = avatarRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Harika Görünüyor", color = colorScheme.onPrimary, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun FinalStep(name: String, avatarResId: Int, onStart: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(110.dp)
                .border(3.dp, colorScheme.secondary, CircleShape)
                .padding(6.dp)
                .clip(CircleShape)
                .background(colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = avatarResId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Her şey hazır,\n$name!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = colorScheme.onBackground,
            lineHeight = 38.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        LottieAnimationView(
            resId = R.raw.chekmark,
            modifier = Modifier.size(180.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Günlük takip et, istikrarlı kal,\nhedeflerine bir adım daha yaklaş.",
            color = colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.weight(1.2f))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Yolculuğa Başla", color = colorScheme.onPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun LottieAnimationView(
    resId: Int,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}












