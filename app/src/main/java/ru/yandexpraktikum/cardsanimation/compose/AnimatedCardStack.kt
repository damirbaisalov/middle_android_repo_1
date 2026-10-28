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
    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }

    fun handleDragEnd() {
        val isHorizontalDrag = abs(horizontalDragOffset) > abs(verticalDragOffset)
        if (isHorizontalDrag) {
            if (abs(horizontalDragOffset) > threshold) {
                animationState = CardSwapAnimationState(
                    isAnimating = true,
                    animationStep = 1
                )
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
                val finalIndex = if (i == 0) cardCount - 1 else i - 1
                val finalRotation = calculateCardRotation(
                    cardIndex = finalIndex,
                    cardCount = cardCount,
                    isRotated = isRotated
                )

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    cardData = cardData,
                    isAnimating = animationState.isAnimating && i == 0,
                    animationStep = animationState.animationStep,
                    finalRotation = finalRotation,
                    onAnimationStepComplete = { completedStep ->
                        handleAnimationStepComplete(
                            step = completedStep,
                            cardIndex = i,
                            onStepChange = { newStep ->
                                animationState = animationState.copy(
                                    isAnimating = true,
                                    animationStep = newStep
                                )
                            },
                            onAnimationComplete = {
                                displayedCards = reorderCards(displayedCards)
                                animationState = CardSwapAnimationState()
                            }
                        )
                    }
                )
            }
        }
    }
}

private fun handleAnimationStepComplete(
    step: Int,
    cardIndex: Int,
    onStepChange: (Int) -> Unit,
    onAnimationComplete: () -> Unit
) {
    if (cardIndex == 0) {
        when (step) {
            1 -> onStepChange(2)
            2 -> onStepChange(3)
            3 -> onAnimationComplete()
        }
    }
}

// Простая функция перестановки карт
fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}