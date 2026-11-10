package com.raulferrergarcia.leafly.features.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raulferrergarcia.leafly.R
import com.raulferrergarcia.leafly.designsystem.LeaflyBottomBar
import com.raulferrergarcia.leafly.designsystem.LeaflyCard
import com.raulferrergarcia.leafly.designsystem.LeaflyColors
import com.raulferrergarcia.leafly.designsystem.LeaflyMark
import com.raulferrergarcia.leafly.designsystem.LeaflyPrimaryButton
import com.raulferrergarcia.leafly.designsystem.LeaflyScreen
import com.raulferrergarcia.leafly.designsystem.LeaflySpacing
import com.raulferrergarcia.leafly.designsystem.LeaflyTheme

/**
 * Pantalla de bienvenida del onboarding.
 */
@Composable
fun WelcomeScreen(
    onAddPlant: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var hasAppeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { hasAppeared = true }

    val contentAlpha by animateFloatAsState(
        targetValue = if (hasAppeared) 1f else 0f,
        animationSpec = tween(durationMillis = 550),
        label = "welcomeAlpha",
    )
    val contentOffset by animateDpAsState(
        targetValue = if (hasAppeared) 0.dp else 14.dp,
        animationSpec = tween(durationMillis = 550),
        label = "welcomeOffset",
    )

    LeaflyScreen(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = LeaflySpacing.contentMaxWidth)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = LeaflySpacing.screenHorizontal)
                    .padding(top = LeaflySpacing.xxl, bottom = LeaflySpacing.lg)
                    .graphicsLayer {
                        alpha = contentAlpha
                        translationY = contentOffset.toPx()
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LeaflyMark()

                Spacer(modifier = Modifier.height(LeaflySpacing.md))

                Text(
                    text = stringResource(R.string.onboarding_welcome_eyebrow),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(LeaflySpacing.md))

                Text(
                    text = stringResource(R.string.onboarding_welcome_title),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(LeaflySpacing.sm))

                Text(
                    text = stringResource(R.string.onboarding_welcome_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.90f),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(LeaflySpacing.xl))

                BenefitsCard()
            }

            LeaflyBottomBar {
                LeaflyPrimaryButton(
                    text = stringResource(R.string.onboarding_add_first_plant),
                    onClick = onAddPlant,
                )

                Spacer(modifier = Modifier.height(LeaflySpacing.xs))

                Text(
                    text = stringResource(R.string.onboarding_cta_footnote),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LeaflyColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun BenefitsCard() {
    LeaflyCard(padding = LeaflySpacing.md) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(LeaflyColors.PrimarySoft, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Eco,
                    contentDescription = null,
                    tint = LeaflyColors.Secondary,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(LeaflySpacing.md))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_benefit_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = LeaflyColors.TextPrimary,
                )
                Text(
                    text = stringResource(R.string.onboarding_benefit_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LeaflyColors.TextSecondary,
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun WelcomeScreenPreview() {
    LeaflyTheme {
        WelcomeScreen(onAddPlant = {})
    }
}
