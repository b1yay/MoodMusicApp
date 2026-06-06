package com.example.moodmusicapp.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.BlurMaskFilter
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.example.moodmusicapp.AuthState
import com.example.moodmusicapp.AuthViewModel
import com.example.moodmusicapp.FavouritesRepository
import com.example.moodmusicapp.FavouritesViewModel
import com.example.moodmusicapp.JamendoViewModel
import com.example.moodmusicapp.LyricsRepository
import com.example.moodmusicapp.MediaManager
import com.example.moodmusicapp.MoodReminderWorker
import com.example.moodmusicapp.MusicPlayer
import com.example.moodmusicapp.PlaylistViewModel
import com.example.moodmusicapp.Song
import com.example.moodmusicapp.UserPlaylist
import com.example.moodmusicapp.SongRepository
import com.example.moodmusicapp.YouTubePlayer
import com.example.moodmusicapp.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

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
    isNew: Boolean = false,
    isFavourite: Boolean = song.isFavorite,
    onAddToPlaylist: (() -> Unit)? = null,
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

            if (isNew) {
                Surface(
                    color = BrandPurple.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "New",
                        color = LightPurple,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "3:45",
                fontSize = 11.sp,
                color = if (isPlaying && showNumber) BrandPurple else Color.White.copy(alpha = 0.24f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            if (onAddToPlaylist != null) {
                IconButton(
                    onClick = { onAddToPlaylist() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlaylistAdd,
                        contentDescription = "Add to playlist",
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            IconButton(
                onClick = { onFavoriteToggle() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavourite) BrandPink else Color.White.copy(alpha = 0.3f),
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

          /*  Spacer(modifier = Modifier.height(24.dp))

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
            }*/

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
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    favouritesViewModel: FavouritesViewModel,
    onLogout: () -> Unit,
    onMoodClick: (String) -> Unit
) {
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
                /*IconButton(
                    onClick = { },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(Icons.Default.Notifications, null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))*/
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
                        onClick = {
                            favouritesViewModel.recordMoodUsed(mood.name)
                            onMoodClick(mood.name)
                        },
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
            text = "Top Hits",
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
                            favouritesViewModel.recordSongPlayed()
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

// --- DISCOVER SCREEN ---

@Composable
fun DiscoverScreen(
    navController: NavController,
    jamendoViewModel: JamendoViewModel,
    favouritesViewModel: FavouritesViewModel
) {
    val tracks by jamendoViewModel.tracks.collectAsState()
    val isLoading by jamendoViewModel.isLoading.collectAsState()
    val favourites by favouritesViewModel.favourites.collectAsState()
    val favouriteIds = remember(favourites) { favourites.map { it.id }.toSet() }
    var selectedMood by remember { mutableStateOf("All") }
    val moods = listOf("All", "Happy", "Sad", "Chill", "Angry", "Romantic")
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    LaunchedEffect(selectedMood, isSearchActive) {
        if (!isSearchActive) {
            jamendoViewModel.loadTracksForMood(if (selectedMood == "All") "" else selectedMood)
        }
    }

    // Debounce search — auto-search as the user types
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            delay(500)
            jamendoViewModel.searchTracks(searchQuery)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C18))
            .padding(horizontal = 24.dp)
            .padding(top = 58.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Discover",
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = Color.White
                )
                Text(
                    text = "Find new music by mood",
                    fontFamily = DmSansFontFamily,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
            IconButton(
                onClick = { },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            ) {
                Icon(Icons.Default.Notifications, null, tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Search Bar (functional)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                isSearchActive = it.isNotBlank()
            },
            placeholder = {
                Text(
                    "Search songs, artists...",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 14.sp,
                    fontFamily = DmSansFontFamily
                )
            },
            leadingIcon = { Icon(Icons.Outlined.Search, null, tint = Color.White.copy(alpha = 0.4f)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        isSearchActive = false
                    }) {
                        Icon(Icons.Outlined.Close, "Clear", tint = Color.White.copy(alpha = 0.4f))
                    }
                }
            },
            singleLine = true,
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp, fontFamily = DmSansFontFamily),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (searchQuery.isNotBlank()) {
                        jamendoViewModel.searchTracks(searchQuery)
                    }
                }
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFA855F7),
                unfocusedBorderColor = Color(0x18FFFFFF),
                focusedContainerColor = Color(0x0EFFFFFF),
                unfocusedContainerColor = Color(0x0EFFFFFF),
                cursorColor = Color(0xFFA855F7)
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (isSearchActive) {
            // --- SEARCH RESULTS VIEW ---
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandPurple)
                }
            } else if (tracks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No results for \"$searchQuery\"",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp,
                        fontFamily = DmSansFontFamily
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(tracks) { index, track ->
                        val song = Song(
                            id = track.id,
                            title = track.name,
                            artist = track.artist_name,
                            imageUrl = track.album_image,
                            mood = selectedMood,
                            fileName = null
                        )
                        SongRow(
                            song = song,
                            index = index,
                            totalCount = tracks.size,
                            isFavourite = favouriteIds.contains(song.id),
                            onClick = {
                                YouTubePlayer.initialize(context)
                                YouTubePlayer.playFromUrl(track.audio)
                                MediaManager.playYouTube(context, song)
                                favouritesViewModel.recordSongPlayed()
                                navController.navigate(Screen.NowPlaying.route)
                            },
                            onFavoriteToggle = { favouritesViewModel.toggleFavourite(song) }
                        )
                    }
                }
            }
        } else {

        Text(
            text = "BROWSE BY MOOD",
            fontFamily = SyneFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.35f),
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(moods.size) { index ->
                val mood = moods[index]
                val isSelected = selectedMood == mood
                Surface(
                    onClick = { selectedMood = mood },
                    shape = RoundedCornerShape(100.dp),
                    color = if (isSelected) Color(0x28A855F7) else Color(0xFFFFFFFF).copy(alpha = 0.05f),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) Color(0x47A855F7) else Color(0xFFFFFFFF).copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = mood,
                        color = if (isSelected) Color(0xFFC084FC) else Color(0xFFFFFFFF).copy(alpha = 0.35f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = DmSansFontFamily,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandPurple)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Text(
                        text = "TRENDING NOW",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.35f),
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(230.dp)
                    ) {
                        val trending = tracks.take(4)
                        items(trending) { track ->
                            Card(
                                onClick = {
                                    val song = Song(
                                        id = track.id,
                                        title = track.name,
                                        artist = track.artist_name,
                                        imageUrl = track.album_image,
                                        mood = selectedMood,
                                        fileName = null
                                    )
                                    YouTubePlayer.initialize(context)
                                    YouTubePlayer.playFromUrl(track.audio)
                                    MediaManager.playYouTube(context, song)
                                    favouritesViewModel.recordSongPlayed()
                                    navController.navigate(Screen.NowPlaying.route)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF13131F))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(70.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(BrandPurple.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.MusicNote,
                                            null,
                                            tint = BrandPurple,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        if (!track.album_image.isNullOrEmpty()) {
                                            AsyncImage(
                                                model = track.album_image,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = track.name,
                                        fontFamily = SyneFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = track.artist_name,
                                        fontFamily = DmSansFontFamily,
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.4f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "NEW RELEASES",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.35f),
                        letterSpacing = 1.2.sp
                    )
                }

                val newReleases = tracks.drop(4)
                itemsIndexed(newReleases) { index, track ->
                    val song = Song(
                        id = track.id,
                        title = track.name,
                        artist = track.artist_name,
                        imageUrl = track.album_image,
                        mood = selectedMood,
                        fileName = null
                    )
                    SongRow(
                        song = song,
                        index = index,
                        totalCount = newReleases.size,
                        isNew = true,
                        isFavourite = favouriteIds.contains(song.id),
                        onClick = {
                            YouTubePlayer.initialize(context)
                            YouTubePlayer.playFromUrl(track.audio)
                            MediaManager.playYouTube(context, song)
                            favouritesViewModel.recordSongPlayed()
                            navController.navigate(Screen.NowPlaying.route)
                        },
                        onFavoriteToggle = { favouritesViewModel.toggleFavourite(song) }
                    )
                }
            }
        }
        }
    }
}

// --- PROFILE SCREEN ---

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    favouritesViewModel: FavouritesViewModel,
    playlistViewModel: PlaylistViewModel
) {
    val userName by authViewModel.currentUserName.collectAsState()
    val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    val songsPlayed by favouritesViewModel.songsPlayed.collectAsState()
    val favCount by favouritesViewModel.count.collectAsState()
    val moodsUsed by favouritesViewModel.moodsUsed.collectAsState()
    val playlists by playlistViewModel.playlists.collectAsState()
    
    val initials = remember(userName) {
        userName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(BrandPurple.copy(alpha = 0.2f), BrandPink.copy(alpha = 0.2f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .offset(y = (-36).dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(listOf(BrandPurple, BrandPink))
                        )
                        .border(3.dp, AppBackground, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = { navController.navigate("editprofile") },
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        "Edit Profile",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontFamily = DmSansFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userName,
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = Color.White
            )
            Text(
                text = userEmail,
                fontFamily = DmSansFontFamily,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Songs Played", songsPlayed.toString(), Modifier.weight(1f))
                StatCard("Favourites", favCount.toString(), Modifier.weight(1f))
                StatCard("Moods Used", moodsUsed.toString(), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "MOOD HISTORY",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.35f),
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val moods = listOf(
                    MoodItem("Happy", MoodYellow, Icons.Default.Mood),
                    MoodItem("Sad", MoodBlue, Icons.Default.SentimentVeryDissatisfied),
                    MoodItem("Chill", MoodTeal, Icons.Default.SelfImprovement),
                    MoodItem("Angry", MoodRed, Icons.Default.Bolt),
                    MoodItem("Romantic", MoodPink, Icons.Default.Favorite)
                )
                moods.forEach { mood ->
                    Surface(
                        color = mood.color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(mood.icon, null, tint = mood.color, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(mood.name, color = mood.color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "LIBRARY",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.35f),
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsRow(
                icon = Icons.Outlined.LibraryMusic,
                title = "My Playlists",
                subtitle = "${playlists.size} playlists",
                iconColor = Color(0xFFA855F7)
            ) {
                navController.navigate("myplaylists")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SETTINGS",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.35f),
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsRow(Icons.Default.Notifications, "Notifications", "Manage your alerts", Color(0xFFA855F7)) {
                navController.navigate("notifications")
            }
            SettingsRow(Icons.Default.Lock, "Privacy", "Security and data", Color(0xFF60A5FA)) {
                navController.navigate("privacy")
            }
            SettingsRow(Icons.Default.Info, "About Arbitify", "Version 1.0.0", Color(0xFF34D399)) {
                navController.navigate("about")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    MediaManager.stop()
                    YouTubePlayer.stop()
                    MusicPlayer.stop()
                    authViewModel.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x19F87171)),
                border = BorderStroke(1.dp, Color(0x40F87171))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Logout, null, tint = Color(0xFFF87171), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign Out",
                        color = Color(0xFFF87171),
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(85.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = Color(0xFFC084FC),
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    titleColor: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = titleColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color.White.copy(alpha = 0.35f), fontSize = 11.sp)
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            null,
            tint = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.size(20.dp)
        )
    }
}

// --- FAVOURITES SCREEN ---

@Composable
fun FavouritesScreen(
    favouritesViewModel: FavouritesViewModel,
    playlistViewModel: PlaylistViewModel,
    onNavigateToNowPlaying: () -> Unit = {}
) {
    val context = LocalContext.current
    val songs by favouritesViewModel.favourites.collectAsState()
    val isLoading by favouritesViewModel.isLoading.collectAsState()
    val playlists by playlistViewModel.playlists.collectAsState()
    var playlistSheetSong by remember { mutableStateOf<Song?>(null) }

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
            text = "${songs.size} songs you loved",
            fontFamily = DmSansFontFamily,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.32f)
        )
        Spacer(modifier = Modifier.height(20.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandPurple)
                }
            }
            songs.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No favourites yet. Pick a mood and heart a song!",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp,
                        fontFamily = DmSansFontFamily,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                LazyColumn {
                    itemsIndexed(songs) { index, song ->
                        SongRow(
                            song = song,
                            index = index,
                            totalCount = songs.size,
                            isFavourite = true,
                            onAddToPlaylist = { playlistSheetSong = song },
                            onClick = {
                                if (song.imageUrl != null) {
                                    MediaManager.playYouTube(context, song)
                                } else {
                                    val localSongs = SongRepository.getSongsByMood(song.mood)
                                    MediaManager.playLocal(context, song, localSongs)
                                }
                                favouritesViewModel.recordSongPlayed()
                                onNavigateToNowPlaying()
                            },
                            onFavoriteToggle = {
                                favouritesViewModel.toggleFavourite(song)
                            }
                        )
                    }
                }
            }
        }
    }

    playlistSheetSong?.let { sheetSong ->
        AddToPlaylistSheet(
            song = sheetSong,
            playlists = playlists,
            onDismiss = { playlistSheetSong = null },
            onAddToExisting = { playlistId ->
                playlistViewModel.addSongToPlaylist(playlistId, sheetSong)
                Toast.makeText(context, "Added to playlist", Toast.LENGTH_SHORT).show()
            },
            onCreateNew = { name ->
                playlistViewModel.createPlaylist(name)
                Toast.makeText(context, "Playlist '$name' created!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// --- PLAYLIST SCREEN ---

@Composable
fun PlaylistScreen(
    moodName: String,
    onBack: () -> Unit = {},
    onNavigateToNowPlaying: () -> Unit = {},
    jamendoViewModel: JamendoViewModel,
    favouritesViewModel: FavouritesViewModel,
    playlistViewModel: PlaylistViewModel
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val tracks by jamendoViewModel.tracks.collectAsState()
    val isLoading by jamendoViewModel.isLoading.collectAsState()
    val error by jamendoViewModel.error.collectAsState()

    val favourites by favouritesViewModel.favourites.collectAsState()
    val favouriteIds = remember(favourites) { favourites.map { it.id }.toSet() }

    val playlists by playlistViewModel.playlists.collectAsState()
    var playlistSheetSong by remember { mutableStateOf<Song?>(null) }

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

    val moodKey = moodName.lowercase(Locale.ROOT)
    val moodColor = when (moodKey) {
        "happy" -> Color(0xFFFBBF24)
        "sad" -> Color(0xFF60A5FA)
        "angry" -> Color(0xFFF87171)
        "chill" -> Color(0xFF34D399)
        "romantic" -> Color(0xFFF472B6)
        else -> Color(0xFFA855F7)
    }
    val moodIcon = when (moodKey) {
        "happy" -> Icons.Outlined.WbSunny
        "sad" -> Icons.Outlined.WaterDrop
        "angry" -> Icons.Outlined.FlashOn
        "chill" -> Icons.Outlined.Eco
        "romantic" -> Icons.Outlined.Favorite
        else -> Icons.Outlined.MusicNote
    }
    val moodSubtitle = when (moodKey) {
        "happy" -> "upbeat & bright"
        "sad" -> "mellow & deep"
        "angry" -> "raw & intense"
        "chill" -> "lo-fi & easy"
        "romantic" -> "soulful & tender"
        else -> "feel the beat"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(moodColor.copy(alpha = 0.25f), AppBackground),
                        center = center,
                        radius = size.width / 1.4f
                    )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, AppBackground),
                            startY = with(density) { 200.dp.toPx() } * 0.55f
                        )
                    )
            )
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(moodColor.copy(alpha = 0.15f))
                        .border(1.dp, moodColor.copy(alpha = 0.40f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = moodIcon,
                        contentDescription = null,
                        tint = moodColor,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = moodName.uppercase(Locale.ROOT),
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = Color.White,
                    letterSpacing = 1.4.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = moodSubtitle,
                    fontFamily = DmSansFontFamily,
                    fontSize = 13.sp,
                    color = Color(0x59FFFFFF)
                )
            }
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
                            favouritesViewModel.recordSongPlayed()
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
                        isFavourite = favouriteIds.contains(song.id),
                        onAddToPlaylist = { playlistSheetSong = song },
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
                            favouritesViewModel.recordSongPlayed()
                        },
                        onFavoriteToggle = {
                            favouritesViewModel.toggleFavourite(song)
                        }
                    )
                }
            }
        }
    }

    playlistSheetSong?.let { sheetSong ->
        AddToPlaylistSheet(
            song = sheetSong,
            playlists = playlists,
            onDismiss = { playlistSheetSong = null },
            onAddToExisting = { playlistId ->
                playlistViewModel.addSongToPlaylist(playlistId, sheetSong)
                Toast.makeText(context, "Added to playlist", Toast.LENGTH_SHORT).show()
            },
            onCreateNew = { name ->
                playlistViewModel.createPlaylist(name)
                Toast.makeText(context, "Playlist '$name' created!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// --- NOW PLAYING SCREEN ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    favouritesViewModel: FavouritesViewModel,
    playlistViewModel: PlaylistViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentSong by MediaManager.currentSongState.collectAsState()
    var showPlaylistSheet by remember { mutableStateOf(false) }
    val playlists by playlistViewModel.playlists.collectAsState()
    val isPlaying by MediaManager.isPlaying.collectAsState()
    val isBuffering by MediaManager.isBuffering.collectAsState()
    val playerType by MediaManager.currentPlayerType.collectAsState()
    
    var isFavorite by remember { mutableStateOf(currentSong?.isFavorite ?: false) }
    var isShuffle by remember { mutableStateOf(MediaManager.isShuffle) }
    var isLoop by remember { mutableStateOf(MediaManager.isLoop) }
    
    var progress by remember { mutableStateOf(0f) }
    var duration by remember { mutableLongStateOf(0L) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    
    var lyricsText by remember { mutableStateOf<String?>(null) }
    var isLoadingLyrics by remember { mutableStateOf(false) }
    var showLyricsSheet by remember { mutableStateOf(false) }
    val lyricsScope = rememberCoroutineScope()

    val onLyricsClick = {
        showLyricsSheet = true
        if (lyricsText == null && !isLoadingLyrics) {
            isLoadingLyrics = true
            lyricsScope.launch {
                val result = withContext(Dispatchers.IO) {
                    LyricsRepository.getLyrics(
                        currentSong?.artist ?: "",
                        currentSong?.title ?: ""
                    )
                }
                lyricsText = result
                isLoadingLyrics = false
            }
        }
    }

    LaunchedEffect(currentSong?.id) {
        lyricsText = null
        isLoadingLyrics = false
    }

    LaunchedEffect(currentSong) {
        val song = currentSong
        isFavorite = if (song != null) {
            try { FavouritesRepository.isFavourite(song.id) } catch (e: Exception) { false }
        } else false
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
            /*IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
            }*/
            Spacer(modifier = Modifier.size(48.dp))
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
                        favouritesViewModel.toggleFavourite(it)
                        isFavorite = !isFavorite
                        it.isFavorite = isFavorite
                    }
                }
            )
            ActionItem(
                icon = Icons.Outlined.PlaylistAdd,
                label = "Playlist",
                onClick = { showPlaylistSheet = true }
            )
            ActionItem(
                icon = Icons.Outlined.Share,
                label = "Share",
                onClick = {
                    val song = MediaManager.currentSongState.value
                    if (song != null) {
                        val shareText = """
                            🎵 Now listening to "${song.title}" by ${song.artist}

                            Vibe: ${song.mood} mood

                            Discover mood-based music on Arbitify!
                        """.trimIndent()

                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = android.content.Intent.createChooser(
                            sendIntent, "Share this song via"
                        )
                        context.startActivity(shareIntent)
                    }
                }
            )
            ActionItem(Icons.AutoMirrored.Outlined.Article, "Lyrics", onClick = { onLyricsClick() })
           /* ActionItem(Icons.Outlined.MoreVert, "More")*/
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
            @Suppress("UNUSED_VARIABLE")
            val dummy = 0 // Spacer workaround if needed
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
            onClick = { onLyricsClick() },
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

    if (showLyricsSheet) {
        LyricsBottomSheet(
            song = currentSong,
            lyrics = lyricsText,
            isLoading = isLoadingLyrics,
            onDismiss = { showLyricsSheet = false }
        )
    }

    if (showPlaylistSheet && currentSong != null) {
        AddToPlaylistSheet(
            song = currentSong!!,
            playlists = playlists,
            onDismiss = { showPlaylistSheet = false },
            onAddToExisting = { playlistId ->
                playlistViewModel.addSongToPlaylist(playlistId, currentSong!!)
                Toast.makeText(context, "Added to playlist", Toast.LENGTH_SHORT).show()
            },
            onCreateNew = { name ->
                playlistViewModel.createPlaylist(name)
                Toast.makeText(context, "Playlist '$name' created!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsBottomSheet(
    song: Song?,
    lyrics: String?,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF13131F)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                song?.title ?: "",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
            Text(
                song?.artist ?: "",
                fontFamily = DmSansFontFamily,
                fontSize = 13.sp,
                color = Color(0x99FFFFFF)
            )
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color(0x0EFFFFFF))
            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color(0xFFA855F7))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Finding lyrics...",
                        fontFamily = DmSansFontFamily,
                        fontSize = 13.sp,
                        color = Color(0x47FFFFFF)
                    )
                }

                lyrics != null -> Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        lyrics,
                        fontFamily = DmSansFontFamily,
                        fontSize = 15.sp,
                        color = Color(0x99FFFFFF),
                        lineHeight = 24.sp
                    )
                }

                else -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.MusicOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0x20FFFFFF)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Lyrics not available",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0x47FFFFFF)
                    )
                    Text(
                        "for this song",
                        fontFamily = DmSansFontFamily,
                        fontSize = 13.sp,
                        color = Color(0x28FFFFFF)
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

// --- EDIT PROFILE SCREEN ---

@Composable
fun EditProfileScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val userName by authViewModel.currentUserName.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

    var nameField by remember { mutableStateOf(authViewModel.currentUserName.value) }
    var saving by remember { mutableStateOf(false) }

    val initials = remember(nameField) {
        nameField.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
            .ifEmpty { "U" }
    }

    LaunchedEffect(authState) {
        if (!saving) return@LaunchedEffect
        when (authState) {
            is AuthState.Authenticated -> {
                saving = false
                Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is AuthState.Error -> {
                saving = false
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 52.dp, bottom = 24.dp)
    ) {
        // Top bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Edit Profile",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .background(brush = Brush.linearGradient(listOf(BrandPurple, BrandPink))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Change avatar color",
            color = Color(0xFFC084FC),
            fontSize = 12.sp,
            fontFamily = DmSansFontFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(8.dp))
                .clickable { Toast.makeText(context, "Coming soon!", Toast.LENGTH_SHORT).show() }
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "DISPLAY NAME",
            fontFamily = SyneFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.35f),
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        ArbitifyTextField(
            value = nameField,
            onValueChange = { nameField = it },
            placeholder = "Your name"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "EMAIL",
            fontFamily = SyneFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.35f),
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = userEmail,
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 14.sp,
                fontFamily = DmSansFontFamily
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Contact support to change email",
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 11.sp,
            fontFamily = DmSansFontFamily
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                saving = true
                authViewModel.updateDisplayName(nameField)
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
                        text = "Save Changes",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// --- PRIVACY SCREEN ---

@Composable
fun PrivacyScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    favouritesViewModel: FavouritesViewModel
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Unauthenticated -> navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
            is AuthState.Error -> Toast.makeText(
                context,
                (authState as AuthState.Error).message,
                Toast.LENGTH_LONG
            ).show()
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 52.dp, bottom = 24.dp)
    ) {
        // Top bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Privacy & Security",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "ACCOUNT SECURITY",
            fontFamily = SyneFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.35f),
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        SettingsRow(
            icon = Icons.Default.Lock,
            title = "Change Password",
            subtitle = "Send reset link to your email",
            iconColor = Color(0xFF60A5FA),
            onClick = { showPasswordDialog = true }
        )
        SettingsRow(
            icon = Icons.Default.Delete,
            title = "Delete Account",
            subtitle = "Permanently delete your account",
            iconColor = Color(0xFFF87171),
            titleColor = Color(0xFFF87171),
            onClick = { showDeleteDialog = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "DATA",
            fontFamily = SyneFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.35f),
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        SettingsRow(
            icon = Icons.Default.HeartBroken,
            title = "Clear Favourites",
            subtitle = "Remove all saved songs",
            iconColor = Color(0xFFF472B6),
            onClick = { showClearDialog = true }
        )
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            containerColor = Color(0xFF13131F),
            title = { Text("Reset Password", color = Color.White, fontFamily = SyneFontFamily) },
            text = {
                Text(
                    "A password reset link will be sent to $userEmail",
                    color = Color.White.copy(alpha = 0.6f)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showPasswordDialog = false
                    authViewModel.sendPasswordReset()
                }) {
                    Text("Send Link", color = Color(0xFFC084FC))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.5f))
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF13131F),
            title = { Text("Delete Account", color = Color.White, fontFamily = SyneFontFamily) },
            text = {
                Text(
                    "This action cannot be undone. All your data will be deleted.",
                    color = Color.White.copy(alpha = 0.6f)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    authViewModel.deleteAccount()
                }) {
                    Text("Delete", color = Color(0xFFF87171))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.5f))
                }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = Color(0xFF13131F),
            title = { Text("Clear Favourites", color = Color.White, fontFamily = SyneFontFamily) },
            text = {
                Text(
                    "Remove all saved songs from your favourites?",
                    color = Color.White.copy(alpha = 0.6f)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    favouritesViewModel.clearAllFavourites()
                    Toast.makeText(context, "Favourites cleared", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Clear", color = Color(0xFFF472B6))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.5f))
                }
            }
        )
    }
}

// --- NOTIFICATIONS SCREEN ---

@Composable
fun NotificationScreen(navController: NavController) {
    val context = LocalContext.current
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderHour by remember { mutableIntStateOf(20) }
    var reminderMinute by remember { mutableIntStateOf(0) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scheduleReminder(context, reminderHour, reminderMinute)
            reminderEnabled = true
        } else {
            Toast.makeText(context, "Permission needed for notifications", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 52.dp, bottom = 24.dp)
    ) {
        // Top bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Notifications",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Illustration
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .background(Color(0x20A855F7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = Color(0xFFA855F7),
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "DAILY MOOD REMINDER",
            fontFamily = SyneFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.35f),
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF13131F))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x20A855F7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            null,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Daily Reminder", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Get reminded to pick your mood",
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    scheduleReminder(context, reminderHour, reminderMinute)
                                    reminderEnabled = true
                                }
                            } else {
                                cancelReminder(context)
                                reminderEnabled = false
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFA855F7),
                            uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }

                if (reminderEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        reminderHour = h
                                        reminderMinute = m
                                        scheduleReminder(context, h, m)
                                    },
                                    reminderHour,
                                    reminderMinute,
                                    false
                                ).show()
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Remind me at", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        Text(
                            text = formatTime12(reminderHour, reminderMinute),
                            color = Color(0xFFC084FC),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SyneFontFamily
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime12(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.ROOT, "%d:%02d %s", displayHour, minute, amPm)
}

fun scheduleReminder(context: Context, hour: Int = 20, minute: Int = 0) {
    val workManager = WorkManager.getInstance(context)

    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
    }
    val delay = target.timeInMillis - now.timeInMillis

    val request = OneTimeWorkRequestBuilder<MoodReminderWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .addTag("mood_reminder")
        .build()

    workManager.enqueueUniqueWork(
        "mood_reminder",
        ExistingWorkPolicy.REPLACE,
        request
    )

    Toast.makeText(
        context,
        "Reminder set for ${String.format(Locale.ROOT, "%02d:%02d", hour, minute)}",
        Toast.LENGTH_SHORT
    ).show()
}

fun cancelReminder(context: Context) {
    WorkManager.getInstance(context).cancelAllWorkByTag("mood_reminder")
    Toast.makeText(context, "Reminder cancelled", Toast.LENGTH_SHORT).show()
}

// --- ABOUT SCREEN ---

@Composable
fun AboutScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C18))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 8.dp)
    ) {
        // 1. Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
            Text(
                text = "About Arbitify",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
        }

        // 2. Hero
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(brush = Brush.linearGradient(listOf(BrandPurple, BrandPink))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.GraphicEq,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Arbitify",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                color = Color.White
            )
            Text(
                text = "Version 1.0.0",
                fontFamily = DmSansFontFamily,
                fontSize = 13.sp,
                color = Color(0x47FFFFFF)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(Color(0xFFFBBF24), Color(0xFFA855F7), Color(0xFFF472B6)).forEach { dot ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(dot)
                    )
                }
            }
        }

        // 3. Gen-Z intro card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0x40A855F7), RoundedCornerShape(16.dp))
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(brush = Brush.linearGradient(listOf(BrandPurple, BrandPink)))
            )
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "NO CAP, WE ACTUALLY BUILT THIS",
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFFA855F7),
                    letterSpacing = 1.1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "two gen-z girlies, one shared music addiction, and a whole lot of late night coding sessions. Arbitify was born because we were too indecisive about what to play — so we let the vibe decide.",
                    fontFamily = DmSansFontFamily,
                    fontSize = 14.sp,
                    color = Color(0x99FFFFFF),
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "built different. literally.",
                    fontFamily = DmSansFontFamily,
                    fontSize = 13.sp,
                    color = Color(0x47FFFFFF),
                    fontStyle = FontStyle.Italic
                )
            }
        }

        // 4. Tagline card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF13131F))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Feel the right beat.",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Arbitify is a mood-based music app that curates the perfect playlist for how you're feeling right now. Whether you're happy, sad, chill, romantic, or fired up — we've got the soundtrack for every emotion.",
                fontFamily = DmSansFontFamily,
                fontSize = 14.sp,
                color = Color(0x99FFFFFF),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }

        // 5. Name origin card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF13131F))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Why Arbitify?",
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0x99FFFFFF), fontFamily = DmSansFontFamily)) {
                        append("The name is a mix of ")
                    }
                    withStyle(SpanStyle(color = Color(0xFFA855F7), fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold)) {
                        append("\"Ar\"")
                    }
                    withStyle(SpanStyle(color = Color(0x99FFFFFF), fontFamily = DmSansFontFamily)) {
                        append(" from Areeba and ")
                    }
                    withStyle(SpanStyle(color = Color(0xFFF472B6), fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold)) {
                        append("\"Bi\"")
                    }
                    withStyle(SpanStyle(color = Color(0x99FFFFFF), fontFamily = DmSansFontFamily)) {
                        append(" from Biya — two names, one app.")
                    }
                },
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }

        // 6. Info cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AboutInfoCard(
                icon = Icons.Outlined.MusicNote,
                iconColor = Color(0xFFA855F7),
                title = "5 Moods",
                subtitle = "Happy, Sad, Angry\nChill, Romantic",
                modifier = Modifier.weight(1f)
            )
            AboutInfoCard(
                icon = Icons.Outlined.LibraryMusic,
                iconColor = Color(0xFFF472B6),
                title = "Powered by",
                subtitle = "Jamendo Music\nAPI",
                modifier = Modifier.weight(1f)
            )
        }

        // 7. Features list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp)
        ) {
            Text(
                text = "FEATURES",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.35f),
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            AboutFeatureRow(Icons.Outlined.SentimentSatisfied, Color(0xFFFBBF24), "Mood Detection", "Pick your emotion, we pick your music", showDivider = true)
            AboutFeatureRow(Icons.Outlined.MusicNote, Color(0xFFA855F7), "Smart Playlists", "Curated tracks for every mood", showDivider = true)
            AboutFeatureRow(Icons.Outlined.FavoriteBorder, Color(0xFFF472B6), "Favourites", "Save songs you love", showDivider = true)
            AboutFeatureRow(Icons.Outlined.Notifications, Color(0xFF34D399), "Mood Reminders", "Daily notifications to set your vibe", showDivider = false)
        }

        // 8. Team section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp)
        ) {
            Text(
                text = "THE MASTERMINDS",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.35f),
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF13131F))
                    .padding(16.dp)
            ) {
                Text(
                    text = "two brain-rotted gen-z devs who somehow shipped an app",
                    fontFamily = DmSansFontFamily,
                    fontSize = 12.sp,
                    color = Color(0x47FFFFFF),
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = Color(0x0AFFFFFF))
                Spacer(modifier = Modifier.height(14.dp))

                AboutTeamMember(
                    initials = "BA",
                    avatarColors = listOf(Color(0xFFF472B6), Color(0xFFDB2777)),
                    name = "Biya Anjum",
                    role = "Co-founder & Android Developer",
                    pillText = "the Bi in Arbitify",
                    pillColor = Color(0xFFF472B6)
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0x0AFFFFFF))
                Spacer(modifier = Modifier.height(8.dp))

                AboutTeamMember(
                    initials = "AA",
                    avatarColors = listOf(Color(0xFF9333EA), Color(0xFFA855F7)),
                    name = "Areeba Abid",
                    role = "Co-founder & Android Developer",
                    pillText = "the Ar in Arbitify",
                    pillColor = Color(0xFFA855F7)
                )
            }
        }

        // 9. Fun footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0x47FFFFFF))) { append("made with ") }
                    withStyle(SpanStyle(color = Color(0xFFFBBF24))) { append("too much chai") }
                    withStyle(SpanStyle(color = Color(0x47FFFFFF))) { append(" and sleep deprivation") }
                },
                fontFamily = DmSansFontFamily,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "© 2026 Arbitify. All rights reserved.",
                fontFamily = DmSansFontFamily,
                fontSize = 11.sp,
                color = Color(0x28FFFFFF),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AboutInfoCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF13131F))
            .padding(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontFamily = SyneFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White
        )
        Text(
            text = subtitle,
            fontFamily = DmSansFontFamily,
            fontSize = 11.sp,
            color = Color(0x59FFFFFF),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun AboutFeatureRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = SyneFontFamily)
                Text(subtitle, color = Color.White.copy(alpha = 0.35f), fontSize = 11.sp, fontFamily = DmSansFontFamily)
            }
        }
        if (showDivider) {
            HorizontalDivider(color = Color(0x0EFFFFFF))
        }
    }
}

@Composable
private fun AboutTeamMember(
    initials: String,
    avatarColors: List<Color>,
    name: String,
    role: String,
    pillText: String,
    pillColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(brush = Brush.linearGradient(avatarColors)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = SyneFontFamily)
            Text(role, color = Color(0x59FFFFFF), fontSize = 11.sp, fontFamily = DmSansFontFamily)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(pillColor.copy(alpha = 0.08f))
                    .border(1.dp, pillColor.copy(alpha = 0.19f), RoundedCornerShape(100.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(pillText, color = pillColor, fontSize = 10.sp, fontFamily = DmSansFontFamily)
            }
        }
    }
}

// --- ADD TO PLAYLIST SHEET ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheet(
    song: Song,
    playlists: List<UserPlaylist>,
    onDismiss: () -> Unit,
    onAddToExisting: (String) -> Unit,
    onCreateNew: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF13131F)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Add to Playlist",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCreateDialog = true }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFA855F7).copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Add, null, tint = Color(0xFFA855F7))
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Create new playlist",
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFFC084FC)
                )
            }

            HorizontalDivider(
                color = Color(0x0EFFFFFF),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (playlists.isEmpty()) {
                Text(
                    "No playlists yet",
                    fontFamily = DmSansFontFamily,
                    fontSize = 13.sp,
                    color = Color(0x47FFFFFF),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    itemsIndexed(playlists) { _, playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAddToExisting(playlist.id)
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF9333EA).copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.QueueMusic, null, tint = Color(0xFF9333EA))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    playlist.name,
                                    fontFamily = SyneFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    "${playlist.songCount} songs",
                                    fontFamily = DmSansFontFamily,
                                    fontSize = 11.sp,
                                    color = Color(0x47FFFFFF)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = Color(0xFF13131F),
            title = {
                Text("New Playlist", fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Playlist name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPlaylistName.isNotBlank()) {
                        onCreateNew(newPlaylistName)
                        onDismiss()
                    }
                }) {
                    Text("Create", color = Color(0xFFC084FC))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = Color(0x47FFFFFF))
                }
            }
        )
    }
}

// --- MY PLAYLISTS SCREEN ---

@Composable
fun MyPlaylistsScreen(
    navController: NavController,
    playlistViewModel: PlaylistViewModel,
    favouritesViewModel: FavouritesViewModel
) {
    val playlists by playlistViewModel.playlists.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<UserPlaylist?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 52.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0x99FFFFFF)
                )
            }
            Text(
                text = "My Playlists",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Outlined.Add, contentDescription = "New playlist", tint = Color(0xFFC084FC))
            }
        }

        if (playlists.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Outlined.LibraryMusic,
                    contentDescription = null,
                    tint = Color(0x20FFFFFF),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text("No playlists yet", fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0x47FFFFFF))
                Text("Tap + to create your first playlist", fontFamily = DmSansFontFamily, fontSize = 13.sp, color = Color(0x28FFFFFF))
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                itemsIndexed(playlists) { _, playlist ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clickable {
                                val encodedName = java.net.URLEncoder.encode(playlist.name, "UTF-8")
                                navController.navigate("playlistdetail/${playlist.id}/$encodedName")
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131F))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(brush = Brush.linearGradient(listOf(Color(0xFF9333EA), Color(0xFFDB2777)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.QueueMusic, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(playlist.name, fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                Text("${playlist.songCount} songs", fontFamily = DmSansFontFamily, fontSize = 12.sp, color = Color(0x47FFFFFF))
                            }
                            IconButton(onClick = {
                                val encodedName = java.net.URLEncoder.encode(playlist.name, "UTF-8")
                                navController.navigate("playlistdetail/${playlist.id}/$encodedName")
                            }) {
                                Icon(Icons.Outlined.PlayArrow, contentDescription = "Open", tint = Color(0xFFA855F7))
                            }
                            IconButton(onClick = { deleteTarget = playlist }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color(0xFFF87171))
                            }
                        }
                    }
                }
            }
        }
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = Color(0xFF13131F),
            title = { Text("Delete Playlist", fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold, color = Color.White) },
            text = { Text("This cannot be undone.", color = Color(0x99FFFFFF)) },
            confirmButton = {
                TextButton(onClick = {
                    playlistViewModel.deletePlaylist(target.id)
                    deleteTarget = null
                }) {
                    Text("Delete", color = Color(0xFFF87171))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel", color = Color(0x47FFFFFF))
                }
            }
        )
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = Color(0xFF13131F),
            title = { Text("New Playlist", fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Playlist name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPlaylistName.isNotBlank()) {
                        playlistViewModel.createPlaylist(newPlaylistName)
                        newPlaylistName = ""
                        showCreateDialog = false
                    }
                }) {
                    Text("Create", color = Color(0xFFC084FC))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = Color(0x47FFFFFF))
                }
            }
        )
    }
}

// --- PLAYLIST DETAIL SCREEN ---

@Composable
fun PlaylistDetailScreen(
    navController: NavController,
    playlistId: String,
    playlistName: String,
    playlistViewModel: PlaylistViewModel,
    favouritesViewModel: FavouritesViewModel
) {
    val context = LocalContext.current
    val songs by playlistViewModel.currentPlaylistSongs.collectAsState()
    val isLoading by playlistViewModel.isLoading.collectAsState()
    val decodedName = remember(playlistName) { java.net.URLDecoder.decode(playlistName, "UTF-8") }

    LaunchedEffect(playlistId) {
        playlistViewModel.loadPlaylistSongs(playlistId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 52.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0x99FFFFFF)
                )
            }
            Spacer(Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(decodedName, fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                Text("${songs.size} songs", fontFamily = DmSansFontFamily, fontSize = 12.sp, color = Color(0x47FFFFFF))
            }
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFA855F7))
                }
            }
            songs.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No songs yet", fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0x47FFFFFF))
                    Text(
                        "Add songs using the playlist icon on any song",
                        fontFamily = DmSansFontFamily,
                        fontSize = 13.sp,
                        color = Color(0x28FFFFFF),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 40.dp, vertical = 4.dp)
                    )
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    itemsIndexed(songs) { _, song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFA855F7).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!song.imageUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = song.imageUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Default.MusicNote, null, tint = Color(0xFFA855F7), modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (song.imageUrl != null) {
                                            MediaManager.playYouTube(context, song)
                                        } else {
                                            val localSongs = SongRepository.getSongsByMood(song.mood)
                                            MediaManager.playLocal(context, song, localSongs)
                                        }
                                        favouritesViewModel.recordSongPlayed()
                                    }
                            ) {
                                Text(song.title, fontFamily = SyneFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(song.artist, fontFamily = DmSansFontFamily, fontSize = 11.sp, color = Color(0x59FFFFFF), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = {
                                playlistViewModel.removeSongFromPlaylist(playlistId, song.id)
                                Toast.makeText(context, "Removed from playlist", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Remove", tint = Color(0xFFF87171))
                            }
                        }
                        HorizontalDivider(color = Color(0x0EFFFFFF))
                    }
                }
            }
        }
    }
}

data class MoodItem(val name: String, val color: Color, val icon: ImageVector)
