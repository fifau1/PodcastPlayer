package com.example.audioplayer

import android.app.Notification
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audioplayer.ui.theme.AudioPlayerTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audioplayer.ui.theme.Black700
import com.example.audioplayer.ui.theme.Yellow500
import com.example.audioplayer.viewmodel.MediaPlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AudioPlayerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    HomeScreen()
                    MediaPlayerNotification(context = this)
                }
            }
        }
    }

        private fun createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "media_player_channel"
                val channelName = "Media Player Channel"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(channelId, channelName, importance)
                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
}

    @Composable
    fun HomeScreen() {


        val viewModel: MediaPlayerViewModel = viewModel()

        val context = LocalContext.current

        // store audio file
        var audioFile by remember { mutableStateOf<Uri?>(null) }

        // store instance of audio player
        var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }


        // for selecting audio file from storage
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                audioFile = uri
                mediaPlayer = MediaPlayer.create(context, audioFile)


                // Получение метаданных из выбранного аудиофайла с использованием MediaMetadataRetriever


                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, audioFile)


                try {
                    // Получение обложки трека в виде байтового массива
                    val albumArtBytes = retriever.embeddedPicture


// Если изображение доступно, вы можете преобразовать его в Bitmap, например, так:
                    val albumArtBitmap =
                        BitmapFactory.decodeByteArray(albumArtBytes, 0, albumArtBytes?.size ?: 0)
                            .asImageBitmap()

                    viewModel.setSongImage(albumArtBitmap)

                } catch (e: Exception) {

                }


                try {
                    viewModel.setSongTitle(
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                            ?: "Title"
                    )
                    viewModel.setSongArtist(
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                            ?: "Artist"
                    )

                    // Вы можете получать другие метаданные здесь, если это необходимо
                } catch (e: Exception) {
                    viewModel.setSongTitle("Title")
                    viewModel.setSongArtist("Artist")
                }


            } else {
                viewModel.setSongTitle("Title")
                viewModel.setSongArtist("Artist")
            }
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Black700
                )
                .padding(horizontal = 10.dp)
        ) {
            TopAppBar()
            if (mediaPlayer == null) {
                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    content = {
                        Text(
                            "Select Audio File",
                            fontSize = 15.sp
                        )
                    },
                    modifier = Modifier.padding(5.dp),
                    onClick = { launcher.launch("audio/*") }
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(1.dp))
                    if (viewModel.getSongImage() != null) {
                        Image(
                            bitmap = viewModel.getSongImage()!!,
                            contentDescription = "Image Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .sizeIn(maxWidth = 200.dp, maxHeight = 200.dp)
                                .aspectRatio(1f)
                                .weight(10f)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.music),
                            contentDescription = "Image Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .sizeIn(maxWidth = 200.dp, maxHeight = 200.dp)
                                .aspectRatio(1f)
                                .weight(10f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(10f)
                    ) {
                        PlayerSlider(mediaPlayer!!)
                        Spacer(modifier = Modifier.height(10.dp))
                        SongDescription(viewModel.getSongTitle(), viewModel.getSongArtist())
                        Spacer(modifier = Modifier.height(30.dp))
                        PlayerButtons(modifier = Modifier.padding(vertical = 8.dp), mediaPlayer!!)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }


    @Composable
    fun TopAppBar() {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { /*TODO*/ },
                modifier = Modifier.background(color = Yellow500)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back Icon",
                    tint = Black700
                )
            }

            Spacer(modifier = Modifier.weight(1f))


        }
    }


    @Composable
    fun SongDescription(
        title: String,
        name: String,

        ) {


        Text(
            text = title,
            style = MaterialTheme.typography.subtitle1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = name,
                style = MaterialTheme.typography.body2,
                maxLines = 1,
                color = Color.White
            )
        }
    }

    @Composable
    fun PlayerSlider(mediaPlayer: MediaPlayer) {
        val viewModel: MediaPlayerViewModel = viewModel()
        val currentMinutes = viewModel.currentMinutes.observeAsState()

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Slider(
                value = currentMinutes.value!!.toFloat(),
                modifier = Modifier
                    .background(color = Yellow500)
                    .size(500.dp, 8.dp),
                onValueChange = {},
                valueRange = 0f..mediaPlayer.duration.toFloat(),

                colors = SliderDefaults.colors(
                    thumbColor = Black700,
                    activeTrackColor = Black700
                ),

                )
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${currentMinutes.value!!.div(1000).toInt()} s",
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${mediaPlayer.duration.div(1000).toInt()} s",
                    color = Color.White
                )
            }
        }
    }

    @Composable
    fun PlayerButtons(
        modifier: Modifier = Modifier,
        mediaPlayer: MediaPlayer,
        playerButtonSize: Dp = 60.dp,
        sideButtonSize: Dp = 42.dp
    ) {
        val viewModel: MediaPlayerViewModel = viewModel()
        val scope = rememberCoroutineScope()
        val audioFinish = viewModel.audioFinish.observeAsState()
        val audioFlag = remember { mutableStateOf(true) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Yellow500)
        ) {
            val buttonModifier = Modifier
                .size(sideButtonSize)
                .semantics { role = Role.Button }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {


                Image(
                    imageVector = Icons.Filled.Replay10,
                    contentDescription = "Reply 10 Sec Icon",
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Black700),
                    modifier = buttonModifier
                )
                Image(
                    imageVector = if (audioFinish.value == false) {
                        if (audioFlag.value) {
                            Icons.Filled.PlayCircleFilled
                        } else {
                            Icons.Filled.PauseCircleFilled
                        }
                    } else {
                        Icons.Filled.PlayCircleFilled
                    },
                    contentDescription = "Play / Pause Icon",
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Black700),
                    modifier = Modifier
                        .size(playerButtonSize)
                        .semantics { role = Role.Button }
                        .clickable {
                            if (audioFlag.value) {
                                mediaPlayer.start()
                                scope.launch {
                                    delay(500)
                                    viewModel.getMediaDuration(mediaPlayer = mediaPlayer)
                                }
                                audioFlag.value = false
                            } else {
                                audioFlag.value = true
                                mediaPlayer.pause()
                            }
                        }
                )

                Image(
                    imageVector = Icons.Filled.Forward10,
                    contentDescription = "Forward Icon",
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Black700),
                    modifier = buttonModifier
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Image(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Heart Icon",
                    modifier = Modifier.padding(15.dp).size(30.dp)
                )
                Spacer(Modifier.weight(1f))
                Image(
                    imageVector = Icons.Rounded.Speed,
                    contentDescription = "Speed Icon",
                    modifier = Modifier.padding(15.dp).size(30.dp)
                )
            }


        }
    }

    @Composable
    fun MediaPlayerNotification(
        context: Context,
    ) {
        val channelId = "media_player_channel"
        val notificationId = 1

        // Create the notification using Compose
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(androidx.core.R.drawable.notification_bg)
            .setContentTitle("Media Player")
            .setContentText("Now Playing")
            .build()  // Use the older build() method

        // Display the notification using the system's notification manager
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, notification)
    }


@Composable
fun NotificationContent(title: String, artist: String) {
    Column {
        Text(text = title)
        Text(text = artist)
        // Add media player control buttons (play, pause, next, prev, etc.)
    }
}
