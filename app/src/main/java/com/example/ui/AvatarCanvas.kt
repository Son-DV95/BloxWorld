package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.AvatarItem
import com.example.data.PlayerProfileEntity

@Composable
fun AvatarCanvas(
    profile: PlayerProfileEntity?,
    modifier: Modifier = Modifier
) {
    val defaultProfile = profile ?: PlayerProfileEntity()

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Colors
        val skinColor = Color(0xFFFDD835) // Golden yellow Roblox skin
        val shirtColor = parseColor(AvatarItem.getItemById(defaultProfile.equippedShirt)?.colorHex ?: "#212121")
        val pantsColor = parseColor(AvatarItem.getItemById(defaultProfile.equippedPants)?.colorHex ?: "#1565C0")

        // Coordinates & sizes proportional to canvas size
        val headSize = w * 0.32f
        val headX = (w - headSize) / 2f
        val headY = h * 0.12f

        val torsoW = w * 0.5f
        val torsoH = h * 0.38f
        val torsoX = (w - torsoW) / 2f
        val torsoY = headY + headSize

        val armW = w * 0.12f
        val armH = h * 0.34f
        val leftArmX = torsoX - armW - (w * 0.02f)
        val rightArmX = torsoX + torsoW + (w * 0.02f)
        val armsY = torsoY

        val legW = w * 0.2f
        val legH = h * 0.32f
        val leftLegX = torsoX + (w * 0.03f)
        val rightLegX = torsoX + torsoW - legW - (w * 0.03f)
        val legsY = torsoY + torsoH

        // 1. Draw Legs (Pants)
        drawRoundRect(
            color = pantsColor,
            topLeft = Offset(leftLegX, legsY),
            size = Size(legW, legH),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = pantsColor,
            topLeft = Offset(rightLegX, legsY),
            size = Size(legW, legH),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // Draw leg outlines / shoes (grey/black shoes)
        drawRect(
            color = Color(0xFF263238),
            topLeft = Offset(leftLegX, legsY + legH - (h * 0.06f)),
            size = Size(legW, h * 0.06f)
        )
        drawRect(
            color = Color(0xFF263238),
            topLeft = Offset(rightLegX, legsY + legH - (h * 0.06f)),
            size = Size(legW, h * 0.06f)
        )

        // 2. Draw Arms (Skin top, Sleeves matching shirt color)
        drawRoundRect(
            color = shirtColor,
            topLeft = Offset(leftArmX, armsY),
            size = Size(armW, armH * 0.7f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = skinColor,
            topLeft = Offset(leftArmX, armsY + armH * 0.7f),
            size = Size(armW, armH * 0.3f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        drawRoundRect(
            color = shirtColor,
            topLeft = Offset(rightArmX, armsY),
            size = Size(armW, armH * 0.7f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = skinColor,
            topLeft = Offset(rightArmX, armsY + armH * 0.7f),
            size = Size(armW, armH * 0.3f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // 3. Draw Torso (Shirt)
        drawRoundRect(
            color = shirtColor,
            topLeft = Offset(torsoX, torsoY),
            size = Size(torsoW, torsoH),
            cornerRadius = CornerRadius(10f, 10f)
        )
        
        // Draw some details on torso (e.g. Roblox logo or collar)
        drawCircle(
            color = Color(0x33FFFFFF),
            radius = torsoW * 0.25f,
            center = Offset(w / 2f, torsoY + torsoH * 0.4f)
        )

        // 4. Draw Head (Yellow skin)
        drawRoundRect(
            color = skinColor,
            topLeft = Offset(headX, headY),
            size = Size(headSize, headSize),
            cornerRadius = CornerRadius(14f, 14f)
        )

        // Neck
        drawRect(
            color = skinColor,
            topLeft = Offset((w - (w * 0.12f)) / 2f, headY + headSize - 4f),
            size = Size(w * 0.12f, h * 0.03f)
        )

        // 5. Draw Face Expression
        drawFace(defaultProfile.equippedFace, headX, headY, headSize)

        // 6. Draw Hat Accessory
        drawHat(defaultProfile.equippedHat, headX, headY, headSize)
    }
}

private fun DrawScope.drawFace(faceId: String, hX: Float, hY: Float, hSize: Float) {
    val eyeY = hY + hSize * 0.35f
    val leftEyeX = hX + hSize * 0.3f
    val rightEyeX = hX + hSize * 0.7f
    val mouthY = hY + hSize * 0.68f

    when (faceId) {
        "smile" -> {
            // Simple happy dot eyes
            drawCircle(Color.Black, radius = hSize * 0.06f, center = Offset(leftEyeX, eyeY))
            drawCircle(Color.Black, radius = hSize * 0.06f, center = Offset(rightEyeX, eyeY))
            
            // Big simple happy smile
            val path = Path().apply {
                moveTo(hX + hSize * 0.35f, mouthY)
                quadraticTo(
                    hX + hSize * 0.5f, mouthY + hSize * 0.18f,
                    hX + hSize * 0.65f, mouthY
                )
            }
            drawPath(path, Color.Black, style = Stroke(width = hSize * 0.05f))
        }
        "chilled" -> {
            // Draw cool sunglasses
            val glassY = eyeY - hSize * 0.04f
            drawRoundRect(
                color = Color(0xFF212121),
                topLeft = Offset(hX + hSize * 0.18f, glassY),
                size = Size(hSize * 0.3f, hSize * 0.18f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color(0xFF212121),
                topLeft = Offset(hX + hSize * 0.52f, glassY),
                size = Size(hSize * 0.3f, hSize * 0.18f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Sunglasses bridge
            drawLine(
                color = Color(0xFF212121),
                start = Offset(hX + hSize * 0.45f, glassY + hSize * 0.05f),
                end = Offset(hX + hSize * 0.55f, glassY + hSize * 0.05f),
                strokeWidth = hSize * 0.04f
            )
            // Smirk grin
            drawLine(
                color = Color.Black,
                start = Offset(hX + hSize * 0.4f, mouthY + hSize * 0.04f),
                end = Offset(hX + hSize * 0.6f, mouthY),
                strokeWidth = hSize * 0.05f
            )
        }
        "beast" -> {
            // Angry red glowing eyes
            val pathL = Path().apply {
                moveTo(leftEyeX - hSize * 0.08f, eyeY - hSize * 0.06f)
                lineTo(leftEyeX + hSize * 0.08f, eyeY + hSize * 0.02f)
            }
            val pathR = Path().apply {
                moveTo(rightEyeX + hSize * 0.08f, eyeY - hSize * 0.06f)
                lineTo(rightEyeX - hSize * 0.08f, eyeY + hSize * 0.02f)
            }
            drawPath(pathL, Color(0xFFD50000), style = Stroke(width = hSize * 0.06f))
            drawPath(pathR, Color(0xFFD50000), style = Stroke(width = hSize * 0.06f))

            drawCircle(Color(0xFFFF1744), radius = hSize * 0.05f, center = Offset(leftEyeX, eyeY + 2f))
            drawCircle(Color(0xFFFF1744), radius = hSize * 0.05f, center = Offset(rightEyeX, eyeY + 2f))

            // Beast roar mouth (black rectangle/polygon with red teeth)
            drawRect(
                color = Color(0xFF37474F),
                topLeft = Offset(hX + hSize * 0.32f, mouthY - hSize * 0.06f),
                size = Size(hSize * 0.36f, hSize * 0.14f)
            )
            // Teeth
            drawLine(Color.White, Offset(hX + hSize * 0.36f, mouthY - hSize * 0.06f), Offset(hX + hSize * 0.42f, mouthY + hSize * 0.02f), strokeWidth = 3f)
            drawLine(Color.White, Offset(hX + hSize * 0.42f, mouthY + hSize * 0.02f), Offset(hX + hSize * 0.48f, mouthY - hSize * 0.06f), strokeWidth = 3f)
            drawLine(Color.White, Offset(hX + hSize * 0.52f, mouthY - hSize * 0.06f), Offset(hX + hSize * 0.58f, mouthY + hSize * 0.02f), strokeWidth = 3f)
            drawLine(Color.White, Offset(hX + hSize * 0.58f, mouthY + hSize * 0.02f), Offset(hX + hSize * 0.64f, mouthY - hSize * 0.06f), strokeWidth = 3f)
        }
        "winning" -> {
            // Friendly wide eyes
            drawCircle(Color.Black, radius = hSize * 0.07f, center = Offset(leftEyeX, eyeY))
            drawCircle(Color.Black, radius = hSize * 0.07f, center = Offset(rightEyeX, eyeY))
            drawCircle(Color.White, radius = hSize * 0.02f, center = Offset(leftEyeX - 2f, eyeY - 2f))
            drawCircle(Color.White, radius = hSize * 0.02f, center = Offset(rightEyeX - 2f, eyeY - 2f))

            // confindent wide open mouth smile
            val path = Path().apply {
                moveTo(hX + hSize * 0.3f, mouthY - hSize * 0.05f)
                quadraticTo(
                    hX + hSize * 0.5f, mouthY + hSize * 0.22f,
                    hX + hSize * 0.7f, mouthY - hSize * 0.05f
                )
                close()
            }
            drawPath(path, Color.Black)
            // Tongue line
            drawCircle(Color(0xFFFF8A80), radius = hSize * 0.06f, center = Offset(hX + hSize * 0.5f, mouthY + hSize * 0.1f))
        }
    }
}

private fun DrawScope.drawHat(hatId: String, hX: Float, hY: Float, hSize: Float) {
    when (hatId) {
        "fedora" -> {
            val brimY = hY + hSize * 0.08f
            val brimW = hSize * 1.34f
            val brimH = hSize * 0.09f
            val brimX = hX - (brimW - hSize) / 2f

            // Fedora Brim
            drawRoundRect(
                color = Color(0xFF3E2723),
                topLeft = Offset(brimX, brimY),
                size = Size(brimW, brimH),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Crown Base
            val crownW = hSize * 0.78f
            val crownH = hSize * 0.32f
            val crownX = hX + (hSize - crownW) / 2f
            val crownY = brimY - crownH

            drawRoundRect(
                color = Color(0xFF3E2723),
                topLeft = Offset(crownX, crownY),
                size = Size(crownW, crownH),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // Fedora Red Band
            drawRect(
                color = Color(0xFFD50000),
                topLeft = Offset(crownX, brimY - (hSize * 0.08f)),
                size = Size(crownW, hSize * 0.08f)
            )
        }
        "valkyrie" -> {
            // Golden circular helm base
            val crownY = hY - hSize * 0.08f
            val crownW = hSize * 0.8f
            val crownX = hX + (hSize - crownW) / 2f
            drawArc(
                color = Color(0xFFFFD54F),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(crownX, crownY),
                size = Size(crownW, hSize * 0.4f)
            )

            // Valkyrie Golden Crest Peak
            val pathCrest = Path().apply {
                moveTo(hX + hSize * 0.5f, hY - hSize * 0.12f)
                lineTo(hX + hSize * 0.44f, hY - hSize * 0.02f)
                lineTo(hX + hSize * 0.56f, hY - hSize * 0.02f)
                close()
            }
            drawPath(pathCrest, Color(0xFFFFB300))

            // Left wing
            val wingL = Path().apply {
                moveTo(crownX, hY + hSize * 0.15f)
                lineTo(crownX - hSize * 0.28f, hY - hSize * 0.22f)
                lineTo(crownX - hSize * 0.04f, hY - hSize * 0.08f)
                close()
            }
            drawPath(wingL, Color.White)
            drawPath(wingL, Color(0xFFFFD54F), style = Stroke(width = 3f))

            // Right wing
            val wingR = Path().apply {
                moveTo(crownX + crownW, hY + hSize * 0.15f)
                lineTo(crownX + crownW + hSize * 0.28f, hY - hSize * 0.22f)
                lineTo(crownX + crownW + hSize * 0.04f, hY - hSize * 0.08f)
                close()
            }
            drawPath(wingR, Color.White)
            drawPath(wingR, Color(0xFFFFD54F), style = Stroke(width = 3f))
        }
        "crown" -> {
            val crownW = hSize * 0.86f
            val crownH = hSize * 0.35f
            val crownX = hX + (hSize - crownW) / 2f
            val crownY = hY - crownH * 0.8f

            val path = Path().apply {
                moveTo(crownX, crownY + crownH)
                lineTo(crownX, crownY) // Left peak
                lineTo(crownX + crownW * 0.25f, crownY + crownH * 0.5f)
                lineTo(crownX + crownW * 0.5f, crownY) // Center peak
                lineTo(crownX + crownW * 0.75f, crownY + crownH * 0.5f)
                lineTo(crownX + crownW, crownY) // Right peak
                lineTo(crownX + crownW, crownY + crownH)
                close()
            }
            drawPath(path, Color(0xFFFFC107))

            // Royal jewels (red & blue dots) on crown
            drawCircle(Color(0xFFD50000), radius = hSize * 0.03f, center = Offset(crownX, crownY))
            drawCircle(Color(0xFF1976D2), radius = hSize * 0.03f, center = Offset(crownX + crownW * 0.5f, crownY))
            drawCircle(Color(0xFFD50000), radius = hSize * 0.03f, center = Offset(crownX + crownW, crownY))
            
            // Royal base velvet band
            drawRect(
                color = Color(0xFFC62828),
                topLeft = Offset(crownX, crownY + crownH - (hSize * 0.07f)),
                size = Size(crownW, hSize * 0.07f)
            )
        }
        "cap" -> {
            val capW = hSize * 0.86f
            val capH = hSize * 0.28f
            val capX = hX + (hSize - capW) / 2f
            val capY = hY - capH * 0.6f

            // Red cap dome
            drawRoundRect(
                color = Color(0xFFD84315),
                topLeft = Offset(capX, capY),
                size = Size(capW, capH),
                cornerRadius = CornerRadius(14f, 14f)
            )

            // Cap Visor (Brim facing right)
            drawRoundRect(
                color = Color(0xFFBF360C),
                topLeft = Offset(capX + capW * 0.3f, capY + capH - (hSize * 0.06f)),
                size = Size(capW * 0.9f, hSize * 0.08f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // White badge on front
            drawCircle(
                color = Color.White,
                radius = hSize * 0.08f,
                center = Offset(capX + capW * 0.35f, capY + capH * 0.5f)
            )
            // Tiny blue star on badge
            drawCircle(
                color = Color(0xFF1565C0),
                radius = hSize * 0.03f,
                center = Offset(capX + capW * 0.35f, capY + capH * 0.5f)
            )
        }
    }
}

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}
