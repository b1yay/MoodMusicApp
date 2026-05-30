package com.example.moodmusicapp.ui

import android.graphics.BlurMaskFilter
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.moodmusicapp.AuthState
import com.example.moodmusicapp.AuthViewModel
import com.example.moodmusicapp.JamendoViewModel
import com.example.moodmusicapp.MediaManager
import com.example.moodmusicapp.MusicPlayer
import com.example.moodmusicapp.Song
import com.example.moodmusicapp.SongRepository
import com.example.moodmusicapp.YouTubePlayer
import com.example.moodmusicapp.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

// --- SHARED COMPONENTS ---

@Composable
fun ArbitifyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 14.sp
            )
        },
        textStyle = TextStyle(color = Color.White, fontSize = 14.sp, fontFamily = DmSansFontFamily),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.054f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.054f),
            focusedBorderColor = Color.White.copy(alpha = 0.094f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.094f),
            cursorColor = BrandPurple
        )
    )
}

@Composable
fun SocialButton(text: String, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = { },
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.094f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        )
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.54f),
            fontFamily = DmSansFontFamily
        )
    }
}

@Composable
fun SongRow(
    song: Song,
    index: Int,
    totalCount: Int,
    showNumber: Boolean = false,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val currentSong by MediaManager.currentSongState.collectAsState()
    val isPlaying = currentSong?.id == song.id
    val moodColor = when (song.mood.lowercase(Locale.ROOT)) {
        "happy" -> MoodYellow
        "romantic" -> MoodPink
        "sad" -> MoodBlue
        "chill" -> MoodTeal
        "angry" -> MoodRed
        else -> BrandPurple
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (showNumber) 72.dp else 68.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isPlaying && showNumber) BrandPurple.copy(alpha = 0.07f) else Color.Transparent)
                .clickable { onClick() }
                .padding(horizontal = if (showNumber) 12.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showNumber) {
                Box(
                    modifier = Modifier.width(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = AccentPurple
                        )
                    } else if (isPlaying) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = AccentPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = (index + 1).toString(),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.22f)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Album art implementation
            Box(
                modifier = Modifier
                    .size(if (showNumber) 44.dp else 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(moodColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                val context = LocalContext.current
                val imageResId = remember(song.imageFileName) {
                    if (!song.imageFileName.isNullOrEmpty()) {
                        context.resources.getIdentifier(song.imageFileName, "drawable", context.packageName)
                    } else 0
                }
                
                if (!song.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = song.imageUrl,
                        contentDescription = "Song Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (imageResId != 0) {
                    AsyncImage(
                        model = imageResId,
                        contentDescription = "Song Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = moodColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = song.title,
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = if (isPlaying && showNumber) LightPurple else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (!showNumber && isLoading) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = AccentPurple
                        )
                    }
                }
                Text(
                    text = song.artist,
                    fontFamily = DmSansFontFamily,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.36f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "3:45",
                fontSize = 11.sp,
                color = if (isPlaying && showNumber) BrandPurple else Color.White.copy(alpha = 0.24f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = { onFavoriteToggle() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (song.isFavorite) BrandPink else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        if (!showNumber && index < totalCount - 1) {
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
        }
    }
}

// --- SIGN IN SCREEN ---

@Composable
fun SignInScreen(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
            }
        } else if (authState is AuthState.Error) {
            Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(BrandPurple.copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = 280.dp.toPx()
                    ),
                    radius = 280.dp.toPx(),
                    center = Offset(0f, 0f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(BrandPink.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(size.width, 0f),
                        radius = 200.dp.toPx()
                    ),
                    radius = 200.dp.toPx(),
                    center = Offset(size.width, 0f)
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 58.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(BrandPurple, BrandPink)),
                            shape = RoundedCornerShape(9.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.GraphicEq,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Arbitify",
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(38.dp))

            Text(
                text = "Feel the beat.",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 27.sp,
                lineHeight = 32.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Music tuned to every mood you're in.",
                fontFamily = DmSansFontFamily,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(26.dp))

            ArbitifyTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "you@email.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(6.dp))

            ArbitifyTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "••••••••",
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    authViewModel.signIn(email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = authState !is AuthState.Loading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(listOf(BrandPurple, BrandPink)),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text(
                            text = "Sign In",
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.07f)
                )
                Text(
                    text = "or continue with",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.28f),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.07f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SocialButton(text = "Google", modifier = Modifier.weight(1f))
                SocialButton(text = "Apple", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val annotatedString = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0x47FFFFFF))) {
                        append("New here? ")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFFC084FC), fontWeight = FontWeight.Medium)) {
                        append("Create an account")
                    }
                }

                Text(
                    text = annotatedString,
                    fontSize = 13.sp,
                    fontFamily = DmSansFontFamily,
                    modifier = Modifier.clickable { 
                        navController.navigate(Screen.SignUp.route)
                    }
                )
            }
        }
    }
}

// --- SIGN UP SCREEN ---

@Composable
fun SignUpScreen(navController: NavController, authViewModel: AuthViewModel) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
            }
        } else if (authState is AuthState.Error) {
            Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(BrandPurple.copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = 280.dp.toPx()
                    ),
                    radius = 280.dp.toPx(),
                    center = Offset(0f, 0f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(BrandPink.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(size.width, 0f),
                        radius = 200.dp.toPx()
                    ),
                    radius = 200.dp.toPx(),
                    center = Offset(size.width, 0f)
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 58.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(BrandPurple, BrandPink)),
                            shape = RoundedCornerShape(9.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.GraphicEq,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Arbitify",
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(38.dp))

            Text(
                text = "Create Account",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 27.sp,
                lineHeight = 32.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Join Arbitify and feel the beat.",
                fontFamily = DmSansFontFamily,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(26.dp))

            ArbitifyTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = "Your name"
            )

            Spacer(modifier = Modifier.height(6.dp))

            ArbitifyTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "you@email.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(6.dp))

            ArbitifyTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Password",
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                    }
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            ArbitifyTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Confirm Password",
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = icon, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (username.isBlank()) {
                        Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (email.isBlank()) {
                        Toast.makeText(context, "Please enter an email", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (password.length < 6) {
                        Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (password != confirmPassword) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    authViewModel.signUp(username, email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = authState !is AuthState.Loading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(listOf(BrandPurple, BrandPink)),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text(
                            text = "Create Account",
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val annotatedString = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0x47FFFFFF))) {
                        append("Already have an account? ")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFFC084FC), fontWeight = FontWeight.Medium)) {
                        append("Sign In")
                    }
                }

                Text(
                    text = annotatedString,
                    fontSize = 13.sp,
                    fontFamily = DmSansFontFamily,
                    modifier = Modifier.clickable { 
                        navController.navigate(Screen.Login.route)
                    }
                )
            }
        }
    }
}

// --- HOME SCREEN ---

@Composable
fun HomeScreen(navController: NavController, authViewModel: AuthViewModel, onLogout: () -> Unit, onMoodClick: (String) -> Unit) {
    val userName by authViewModel.currentUserName.collectAsState()
    val moods = listOf(
        MoodItem("Happy", MoodYellow, Icons.Default.Mood),
        MoodItem("Romantic", MoodPink, Icons.Default.Favorite),
        MoodItem("Chill", MoodTeal, Icons.Default.SelfImprovement),
        MoodItem("Sad", MoodBlue, Icons.Default.SentimentVeryDissatisfied),
        MoodItem("Angry", MoodRed, Icons.Default.Bolt)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 24.dp)
            .padding(top = 58.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = buildAnnotatedString {
                        append("Hello ")
                        withStyle(style = SpanStyle(color = Color(0xFFC084FC))) {
                            append(userName)
                        }
                        append(",")
                    },
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color.White
                )
            }
            Row {
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(Icons.Default.Notifications, null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(Icons.Default.Logout, null, tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "How are you feeling?",
            fontFamily = SyneFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Mood Grid
        moods.chunked(2).forEach { rowMoods ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowMoods.forEach { mood ->
                    MoodCard(
                        mood = mood,
                        onClick = { onMoodClick(mood.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowMoods.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Made for your mood",
            fontFamily = SyneFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        val recentSongs = remember { SongRepository.allSongs.take(5) }
        val context = LocalContext.current
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(recentSongs.size) { index ->
                val song = recentSongs[index]
                val imageResId = remember(song.imageFileName) {
                    if (!song.imageFileName.isNullOrEmpty()) {
                        context.resources.getIdentifier(song.imageFileName, "drawable", context.packageName)
                    } else 0
                }
                
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(CardSurface)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                        .clickable {
                            if (song.imageUrl != null) {
                                MediaManager.playYouTube(context, song)
                            } else {
                                val localSongs = SongRepository.getSongsByMood(song.mood)
                                MediaManager.playLocal(context, song, localSongs)
                            }
                        },
                    contentAlignment = Alignment.BottomStart
                ) {
                    if (!song.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = song.imageUrl,
                            contentDescription = "Song Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (imageResId != 0) {
                        AsyncImage(
                            model = imageResId,
                            contentDescription = "Song Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)), startY = 200f))
                    )
                    
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = song.title,
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            fontFamily = DmSansFontFamily,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MoodCard(mood: MoodItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(mood.color.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width, 0f),
                        radius = size.width
                    )
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = mood.icon,
                    contentDescription = null,
                    tint = mood.color,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = mood.name,
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

// --- FAVOURITES SCREEN ---

@Composable
fun FavouritesScreen(onNavigateToNowPlaying: () -> Unit = {}) {
    val context = LocalContext.current
    var refreshTrigger by remember { mutableStateOf(0) }
    val favoriteSongs = remember(refreshTrigger) { SongRepository.getFavoriteSongs() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 20.dp)
            .padding(top = 52.dp)
    ) {
        Text(
            text = "Your Favourites",
            fontFamily = SyneFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = Color.White
        )
        Text(
            text = "${favoriteSongs.size} songs you loved",
            fontFamily = DmSansFontFamily,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.32f)
        )
        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn {
            itemsIndexed(favoriteSongs) { index, song ->
                SongRow(
                    song = song,
                    index = index,
                    totalCount = favoriteSongs.size,
                    onClick = {
                        if (song.imageUrl != null) {
                            MediaManager.playYouTube(context, song)
                        } else {
                            val localSongs = SongRepository.getSongsByMood(song.mood)
                            MediaManager.playLocal(context, song, localSongs)
                        }
                        onNavigateToNowPlaying()
                    },
                    onFavoriteToggle = {
                        song.isFavorite = !song.isFavorite
                        refreshTrigger++
                    }
                )
            }
        }
    }
}

// --- PLAYLIST SCREEN ---

@Composable
fun PlaylistScreen(
    moodName: String,
    onBack: () -> Unit = {},
    onNavigateToNowPlaying: () -> Unit = {},
    jamendoViewModel: JamendoViewModel = viewModel(factory = JamendoViewModel.Factory())
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    
    val tracks by jamendoViewModel.tracks.collectAsState()
    val isLoading by jamendoViewModel.isLoading.collectAsState()
    val error by jamendoViewModel.error.collectAsState()

    val currentSong by MediaManager.currentSongState.collectAsState()
    val isBuffering by MediaManager.isBuffering.collectAsState()
    
    val audioUrls = remember { mutableMapOf<String, String>() }

    LaunchedEffect(moodName) {
        jamendoViewModel.loadTracksForMood(moodName)
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    val moodSongs = remember(moodName, tracks) {
        if (tracks.isNotEmpty()) {
            tracks.map { track ->
                audioUrls[track.id] = track.audio
                Song(
                    id = track.id,
                    title = track.name,
                    artist = track.artist_name,
                    imageUrl = track.album_image,
                    mood = moodName,
                    fileName = null
                )
            }
        } else if (!isLoading) {
            SongRepository.getSongsByMood(moodName)
        } else {
            emptyList()
        }
    }

    val moodColor = when (moodName.lowercase(Locale.ROOT)) {
        "happy" -> MoodYellow
        "romantic" -> MoodPink
        "sad" -> MoodBlue
        "chill" -> MoodTeal
        "angry" -> MoodRed
        else -> BrandPurple
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(185.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(moodColor.copy(alpha = 0.22f), AppBackground),
                        center = center,
                        radius = size.width / 1.5f
                    )
                )
            }
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.Center)
                    .background(Color(0xFF1E1A2E), RoundedCornerShape(22.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = moodColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(42.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, AppBackground),
                            startY = with(density) { 185.dp.toPx() } * 0.2f
                        )
                    )
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(top = 40.dp, start = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(AccentPurple.copy(alpha = 0.16f))
                    .border(1.dp, AccentPurple.copy(alpha = 0.28f), RoundedCornerShape(100.dp))
                    .padding(vertical = 4.dp, horizontal = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Mood,
                    contentDescription = null,
                    tint = LightPurple,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = moodName.uppercase(Locale.ROOT),
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = LightPurple
                )
            }
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = "$moodName Mix",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = Color.White
            )
            Text(
                text = "${moodSongs.size} songs",
                fontFamily = DmSansFontFamily,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.32f)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Brush.linearGradient(listOf(BrandPurple, BrandPink)))
                    .clickable {
                        if (moodSongs.isNotEmpty()) {
                            val firstSong = moodSongs[0]
                            val audioUrl = audioUrls[firstSong.id]
                            if (audioUrl != null) {
                                Log.d("JAMENDO", "Playing All: ${firstSong.title}, URL: $audioUrl")
                                MusicPlayer.stop()
                                YouTubePlayer.initialize(context)
                                YouTubePlayer.playFromUrl(audioUrl)
                                MediaManager.playYouTube(context, firstSong)
                            } else {
                                val localSongs = SongRepository.getSongsByMood(firstSong.mood)
                                MediaManager.playLocal(context, firstSong, localSongs)
                            }
                            onNavigateToNowPlaying()
                        }
                    }
                    .padding(vertical = 11.dp, horizontal = 22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Play All",
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = BrandPurple
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(moodSongs) { index, song ->
                    val isThisSongBuffering = isBuffering && currentSong?.id == song.id
                    
                    SongRow(
                        song = song,
                        index = index,
                        totalCount = moodSongs.size,
                        showNumber = true,
                        isLoading = isThisSongBuffering,
                        onClick = {
                            Log.d("PLAY_DEBUG", "Click: ${song.title}")
                            val audioUrl = audioUrls[song.id]
                            if (audioUrl != null) {
                                Log.d("JAMENDO", "Playing: ${song.title}, URL: $audioUrl")
                                MusicPlayer.stop()
                                YouTubePlayer.initialize(context)
                                YouTubePlayer.playFromUrl(audioUrl)
                                MediaManager.playYouTube(context, song)
                            } else {
                                val localSongs = SongRepository.getSongsByMood(song.mood)
                                MediaManager.playLocal(context, song, localSongs)
                                Log.d("PLAY_DEBUG", "MusicPlayer local: ${song.title}")
                            }
                        },
                        onFavoriteToggle = {
                            song.isFavorite = !song.isFavorite
                        }
                    )
                }
            }
        }
    }
}

// --- NOW PLAYING SCREEN ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val currentSong by MediaManager.currentSongState.collectAsState()
    val isPlaying by MediaManager.isPlaying.collectAsState()
    val isBuffering by MediaManager.isBuffering.collectAsState()
    val playerType by MediaManager.currentPlayerType.collectAsState()
    
    var isFavorite by remember { mutableStateOf(currentSong?.isFavorite ?: false) }
    var isShuffle by remember { mutableStateOf(MediaManager.isShuffle) }
    var isLoop by remember { mutableStateOf(MediaManager.isLoop) }
    
    var progress by remember { mutableStateOf(0f) }
    var duration by remember { mutableLongStateOf(0L) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    
    var lyricsOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(currentSong) {
        isFavorite = currentSong?.isFavorite ?: false
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = MediaManager.getCurrentPosition()
            duration = MediaManager.getDuration()
            if (duration > 0) {
                progress = currentPosition.toFloat() / duration.toFloat()
            }
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 22.dp)
            .padding(top = 52.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Back",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = "NOW PLAYING",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.35f),
                letterSpacing = 1.3.sp
            )
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Album Art centered with glow
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.CenterHorizontally)
                .drawBehind {
                    val radiusPx = 140.dp.toPx()
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint()
                        paint.color = android.graphics.Color.parseColor("#9333EA")
                        paint.alpha = (0.22f * 255).toInt()
                        paint.maskFilter = BlurMaskFilter(60f, android.graphics.BlurMaskFilter.Blur.NORMAL)
                        canvas.nativeCanvas.drawCircle(
                            size.width / 2,
                            size.height / 2,
                            radiusPx,
                            paint
                        )
                    }
                }
                .background(Color(0xFF1A1628), RoundedCornerShape(28.dp))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            val song = currentSong
            val imageResId = remember(song?.imageFileName) {
                if (song != null && !song.imageFileName.isNullOrEmpty()) {
                    context.resources.getIdentifier(song.imageFileName, "drawable", context.packageName)
                } else 0
            }
            
            if (!song?.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = song?.imageUrl,
                    contentDescription = "Song Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (imageResId != 0) {
                AsyncImage(
                    model = imageResId,
                    contentDescription = "Song Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = BrandPurple.copy(alpha = 0.5f),
                    modifier = Modifier.size(72.dp)
                )
            }

            if (isBuffering) {
                CircularProgressIndicator(color = BrandPurple, modifier = Modifier.size(48.dp))
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentSong?.title ?: "Select a song",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = currentSong?.artist ?: "",
                fontFamily = DmSansFontFamily,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.42f),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionItem(
                icon = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                label = "Favourite",
                color = if (isFavorite) BrandPink else Color.White.copy(alpha = 0.3f),
                onClick = {
                    currentSong?.let {
                        it.isFavorite = !it.isFavorite
                        isFavorite = it.isFavorite
                    }
                }
            )
            ActionItem(Icons.Outlined.Share, "Share")
            ActionItem(Icons.AutoMirrored.Outlined.Article, "Lyrics", onClick = { lyricsOpen = true })
            ActionItem(Icons.Outlined.MoreVert, "More")
        }

        Spacer(modifier = Modifier.height(26.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = progress,
                onValueChange = {
                    progress = it
                    MediaManager.seekTo((it * duration).toLong())
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = BrandPurple,
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatTime(currentPosition.toInt()), fontSize = 11.sp, color = Color.White.copy(alpha = 0.28f))
                Text(text = formatTime(duration.toInt()), fontSize = 11.sp, color = Color.White.copy(alpha = 0.28f))
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                MediaManager.isShuffle = !MediaManager.isShuffle
                isShuffle = MediaManager.isShuffle
            }) {
                Icon(
                    imageVector = Icons.Default.Shuffle, 
                    contentDescription = null, 
                    tint = if (isShuffle) AccentPurple else Color.White.copy(alpha = 0.5f), 
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = { MediaManager.playPrevious(context) }) {
                Icon(Icons.Default.SkipPrevious, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
            }
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(BrandPurple, BrandPink)))
                    .clickable {
                        MediaManager.togglePlayPause()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            IconButton(onClick = { MediaManager.playNext(context) }) {
                Icon(Icons.Default.SkipNext, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
            }
            IconButton(onClick = { 
                MediaManager.isLoop = !MediaManager.isLoop
                isLoop = MediaManager.isLoop
            }) {
                Icon(
                    imageVector = Icons.Default.Repeat, 
                    contentDescription = null, 
                    tint = if (isLoop) AccentPurple else Color.White.copy(alpha = 0.5f), 
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        OutlinedButton(
            onClick = { lyricsOpen = true },
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.094f)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.054f))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(Icons.AutoMirrored.Filled.PlaylistPlay, null, tint = LightPurple, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "View song lyrics",
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }

    if (lyricsOpen) {
        ModalBottomSheet(
            onDismissRequest = { lyricsOpen = false },
            sheetState = sheetState,
            containerColor = Color(0xFF13131F),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                item {
                    Text(
                        text = "Lyrics implementation depends on metadata...\n\nSample line 1\nSample line 2\n...",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = DmSansFontFamily,
                        lineHeight = 27.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MiniPlayer(onExpand: () -> Unit) {
    val context = LocalContext.current
    val currentSong by MediaManager.currentSongState.collectAsState()
    val isPlaying by MediaManager.isPlaying.collectAsState()

    if (currentSong != null) {
        val infiniteTransition = rememberInfiniteTransition(label = "Rotation")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(15000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "Rotation"
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clickable { onExpand() },
            color = CardSurface,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rotating Album Art
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .rotate(if (isPlaying) rotation else 0f)
                ) {
                    val imageResId = remember(currentSong?.imageFileName) {
                        if (currentSong != null && !currentSong?.imageFileName.isNullOrEmpty()) {
                            context.resources.getIdentifier(currentSong?.imageFileName, "drawable", context.packageName)
                        } else 0
                    }
                    
                    if (!currentSong?.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = currentSong?.imageUrl,
                            contentDescription = "Song Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (imageResId != 0) {
                        AsyncImage(
                            model = imageResId,
                            contentDescription = "Song Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(BrandPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MusicNote, null, tint = BrandPurple)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSong?.title ?: "",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentSong?.artist ?: "",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { MediaManager.playPrevious(context) }) {
                    Icon(Icons.Default.SkipPrevious, null, tint = Color.White)
                }
                
                IconButton(onClick = { 
                    MediaManager.togglePlayPause()
                }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                
                IconButton(onClick = { MediaManager.playNext(context) }) {
                    Icon(Icons.Default.SkipNext, null, tint = Color.White)
                }
            }
        }
    }
}

private fun formatTime(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
}

@Composable
fun ActionItem(icon: ImageVector, label: String, color: Color = Color.White.copy(alpha = 0.3f), onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clickable { onClick() }
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.3f))
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(text = name, color = Color.White)
    }
}

data class MoodItem(val name: String, val color: Color, val icon: ImageVector)
