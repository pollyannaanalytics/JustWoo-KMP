package com.pollyannawu.justwoo.android.ui.task

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooShapes
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.design.DesignTokens
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import com.pollyannawu.justwoo.android.ui.common.componentViewModelStoreOwner
import com.pollyannawu.justwoo.ui.nav.tasks.TaskListComponent
import org.koin.androidx.compose.koinViewModel


@Composable
fun TaskExplorationScreen(
    currentUserId: Long,
    currentHouseId: Long,
    component: TaskListComponent,
    onOpenProfile: () -> Unit,
    viewModel: TaskExplorationViewModel = koinViewModel(viewModelStoreOwner = componentViewModelStoreOwner(component)),
) {
    LaunchedEffect(currentUserId) { viewModel.bind(currentUserId) }
    val state by viewModel.uiState.collectAsState()

    TaskExplorationContent(
        state = state,
        onClose = component::onClose,
        onOpenProfile = onOpenProfile,
        onAccept = { taskId -> viewModel.accept(currentHouseId, currentUserId, taskId) },
        onDecline = { taskId -> viewModel.decline(currentHouseId, currentUserId, taskId) }
    )
}

@Composable
private fun TaskExplorationContent(
    state: TaskExplorationViewModel.UiState,
    onClose: () -> Unit,
    onOpenProfile: () -> Unit,
    onAccept: (Long) -> Unit,
    onDecline: (Long) -> Unit,
) {
    Scaffold(
        containerColor = JustWooColors.Cream,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JustWooColors.Cream)
                    .padding(horizontal = JustWooSpacing.Small, vertical = JustWooSpacing.Medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = JustWooColors.TextPrimary,
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Task Exploration",
                    color = JustWooColors.TextPrimary,
                    fontWeight = JustWooFontWeight.Bold,
                    fontSize = DesignTokens.FontSize.Title.sp,
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .padding(end = JustWooSpacing.Medium)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(JustWooColors.TextPrimary)
                        .clickable { onOpenProfile() },
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = JustWooSpacing.XLarge, vertical = JustWooSpacing.Default),
        ) {
            val current = state.current
            when {
                state.tasks.isEmpty() -> EmptyDeck()
                current == null -> AllCaughtUp()
                else -> SwipeDeck(
                    task = current,
                    indexLabel = "${state.currentIndex + 1}/${state.total}",
                    submitting = state.submitting,
                    onAccept = { onAccept(current.id) },
                    onDecline = { onDecline(current.id) },
                )
            }
            state.error?.let { err ->
                Text(
                    text = err,
                    color = JustWooColors.Error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = JustWooSpacing.Medium),
                )
            }
        }
    }
}

@Composable
private fun SwipeDeck(
    task: Task,
    indexLabel: String,
    submitting: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    val palette = task.deckPalette()
    val scope = rememberCoroutineScope()
    val offsetX = remember(task.id) { Animatable(0f) }
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx = with(density) { maxWidth.toPx() }
        val threshold = widthPx * 0.30f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = offsetX.value
                    rotationZ = (offsetX.value / widthPx) * 18f
                }
                .pointerInput(task.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val current = offsetX.value
                            when {
                                current > threshold -> scope.launch {
                                    offsetX.animateTo(widthPx * 1.4f, tween(220))
                                    onAccept()
                                    offsetX.snapTo(0f)
                                }
                                current < -threshold -> scope.launch {
                                    offsetX.animateTo(-widthPx * 1.4f, tween(220))
                                    onDecline()
                                    offsetX.snapTo(0f)
                                }
                                else -> scope.launch {
                                    offsetX.animateTo(0f, tween(220))
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch { offsetX.animateTo(0f, tween(220)) }
                        },
                    ) { _, dragAmount ->
                        scope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
                    }
                },
        ) {
            TaskDeckCard(
                task = task,
                indexLabel = indexLabel,
                palette = palette,
                submitting = submitting,
                onAccept = {
                    scope.launch {
                        offsetX.animateTo(widthPx * 1.4f, tween(220))
                        onAccept()
                        offsetX.snapTo(0f)
                    }
                },
                onDecline = {
                    scope.launch {
                        offsetX.animateTo(-widthPx * 1.4f, tween(220))
                        onDecline()
                        offsetX.snapTo(0f)
                    }
                },
            )

            SwipeOverlay(
                text = "ACCEPT",
                color = JustWooColors.UrgencyGreen,
                alpha = (offsetX.value / threshold).coerceIn(0f, 1f),
                alignment = Alignment.TopEnd,
            )
            SwipeOverlay(
                text = "DECLINE",
                color = JustWooColors.UrgencyRed,
                alpha = (-offsetX.value / threshold).coerceIn(0f, 1f),
                alignment = Alignment.TopStart,
            )
        }
    }
}

@Composable
private fun BoxScope.SwipeOverlay(
    text: String,
    color: Color,
    alpha: Float,
    alignment: Alignment,
) {
    if (alpha <= 0.01f) return
    Box(
        modifier = Modifier
            .align(alignment)
            .padding(JustWooSpacing.Large)
            .graphicsLayer { this.alpha = alpha }
            .clip(JustWooShapes.Small)
            .border(3.dp, color, JustWooShapes.Small)
            .padding(horizontal = JustWooSpacing.Medium, vertical = JustWooSpacing.XSmall),
    ) {
        Text(
            text = text,
            color = color,
            fontWeight = JustWooFontWeight.Black,
            fontSize = DesignTokens.FontSize.Heading.sp,
        )
    }
}

private data class DeckPalette(
    val background: Color,
    val onBackground: Color,
    val label: String,
)

private fun Task.deckPalette(): DeckPalette {
    val zone = TimeZone.currentSystemDefault()
    val today: LocalDate = Clock.System.now().toLocalDateTime(zone).date
    val due: LocalDate = dueTime.toLocalDateTime(zone).date
    val diff = today.daysUntil(due)
    return when {
        diff <= 0 -> DeckPalette(JustWooColors.UrgencyRed, Color.White, "Today")
        diff == 1 -> DeckPalette(JustWooColors.UrgencyYellow, Color.White, "Tomorrow")
        else -> DeckPalette(JustWooColors.UrgencyGreen, Color.White, "${due.monthNumber}/${due.dayOfMonth}")
    }
}

@Composable
private fun TaskDeckCard(
    task: Task,
    indexLabel: String,
    palette: DeckPalette,
    submitting: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    val due = task.dueTime.toLocalDateTime(TimeZone.currentSystemDefault())
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(JustWooShapes.XXLarge)
            .background(palette.background)
            .padding(JustWooSpacing.XLarge),
        verticalArrangement = Arrangement.spacedBy(JustWooSpacing.Default),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = palette.label,
                color = palette.onBackground,
                fontWeight = JustWooFontWeight.Black,
                fontSize = DesignTokens.FontSize.Display.sp,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = palette.onBackground,
            )
            Spacer(Modifier.size(JustWooSpacing.XSmall))
            Text(
                text = indexLabel,
                color = palette.onBackground,
                fontWeight = JustWooFontWeight.SemiBold,
                fontSize = DesignTokens.FontSize.Label.sp,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(palette.onBackground.copy(alpha = 0.6f))
        )

        Text(
            text = task.title,
            color = palette.onBackground,
            fontWeight = JustWooFontWeight.Black,
            fontSize = DesignTokens.FontSize.Hero.sp,
            lineHeight = 44.sp,
        )

        Column(verticalArrangement = Arrangement.spacedBy(JustWooSpacing.Small)) {
            BulletLine(text = "Assigner: #${task.ownerId}", color = palette.onBackground)
            BulletLine(
                text = "Due Date: ${due.monthNumber}/${due.dayOfMonth} " +
                    "%02d:%02d".format(due.hour, due.minute),
                color = palette.onBackground,
            )
            if (task.description.isNotBlank()) {
                BulletLine(text = "Note: ${task.description}", color = palette.onBackground)
            }
        }

        Spacer(Modifier.height(JustWooSpacing.Small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(JustWooSpacing.Medium),
        ) {
            DeckActionButton(
                text = "Decline",
                outlined = true,
                color = palette.onBackground,
                enabled = !submitting,
                onClick = onDecline,
                modifier = Modifier.weight(1f),
            )
            DeckActionButton(
                text = "Accept",
                outlined = false,
                color = palette.onBackground,
                enabled = !submitting,
                onClick = onAccept,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BulletLine(text: String, color: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Text("•  ", color = color, fontWeight = JustWooFontWeight.Bold)
        Text(
            text = text,
            color = color,
            fontSize = DesignTokens.FontSize.BodyLarge.sp,
            fontWeight = JustWooFontWeight.SemiBold,
        )
    }
}

@Composable
private fun DeckActionButton(
    text: String,
    outlined: Boolean,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (outlined) Color.Transparent else Color.White
    val fg = if (outlined) color else JustWooColors.TextPrimary
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(JustWooShapes.XXLarge)
            .background(bg)
            .border(2.dp, if (outlined) color else Color.Transparent, JustWooShapes.XXLarge)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = JustWooSpacing.Default),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = fg,
            fontWeight = JustWooFontWeight.ExtraBold,
            fontSize = DesignTokens.FontSize.Title.sp,
        )
    }
}

@Composable
private fun EmptyDeck() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No pending tasks. You're all set!",
            color = JustWooColors.TextSecondary,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun AllCaughtUp() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "All caught up — no more tasks to review.",
            color = JustWooColors.TextSecondary,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
