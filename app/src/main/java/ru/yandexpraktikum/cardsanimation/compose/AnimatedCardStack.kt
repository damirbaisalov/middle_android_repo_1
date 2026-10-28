package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.model.CardSwapAnimationState
import kotlin.math.abs

/**
 * Метод для вычисления поворота карты в конкретной позиции
 */
fun calculateCardRotation(
    cardIndex: Int,
    cardCount: Int,
    isRotated: Boolean
): Float {
    if (cardCount <= 1) return 0f

    return if (isRotated) {
        val angleStep = 180f / (cardCount - 1)
        90f - (cardIndex * angleStep)
    } else {
        val angleStep = 45f / (cardCount - 1)
        22.5f - (cardIndex * angleStep)
    }
}

private const val CARDS_ANIMATIONS_COMPOSE_TAG = "CardsAnimationsCompose"

@Composable
fun AnimatedCardStack(cards: List<CardData>) {
    val cardCount = cards.size
    var displayedCards by remember(cards) { mutableStateOf(cards) }
    var isRotated by remember { mutableStateOf(false) }
    var verticalDragOffset by remember { mutableFloatStateOf(0f) }
    var horizontalDragOffset by remember { mutableFloatStateOf(0f) }
    val threshold = 100f
    // TODO: [Задание 2] Добавьте обработку жестов
    // Подсказка: Используйте Modifier.pointerInput() с методом detectDragGestures()

    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }

    fun handleDragEnd() {
        val isHorizontalDrag = abs(horizontalDragOffset) > abs(verticalDragOffset)
        if (isHorizontalDrag) {
            if (abs(horizontalDragOffset) > threshold) {
                displayedCards = reorderCards(displayedCards)
            }
        } else {
            when {
                verticalDragOffset < -threshold -> isRotated = true
                verticalDragOffset > threshold -> isRotated = false
            }
        }
        verticalDragOffset = 0f
        horizontalDragOffset = 0f
    }

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        verticalDragOffset = 0f
                        horizontalDragOffset = 0f
                    },
                    onDragCancel = {
                        verticalDragOffset = 0f
                        horizontalDragOffset = 0f
                    },
                    onDragEnd = {
                        if (!animationState.isAnimating) {
                            handleDragEnd()
                        } else {
                            verticalDragOffset = 0f
                            horizontalDragOffset = 0f
                        }
                    }
                ) { _, dragAmount ->
                    verticalDragOffset += dragAmount.y
                    horizontalDragOffset += dragAmount.x
                }
            },
        contentAlignment = Alignment.Center
    ) {
        displayedCards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(i, cardCount, isRotated)

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    cardData = cardData
                    // TODO: [Задание 5] Здесь добавьте параметры анимации карты
                )
            }
        }
    }
}

// Простая функция перестановки карт
fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}