package me.ash.reader.ui.theme
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Shapes
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(2.0.dp),
    small = RoundedCornerShape(4.0.dp),
    medium = RoundedCornerShape(6.0.dp),
    large = RoundedCornerShape(8.0.dp),
    extraLarge = RoundedCornerShape(12.0.dp)
)

@Stable
val Shape20 = RoundedCornerShape(12.0.dp)
@Stable
val Shape24 = RoundedCornerShape(12.0.dp)
@Stable
val Shape32 = RoundedCornerShape(16.0.dp)
@Stable
val ShapeTop32 = RoundedCornerShape(16.0.dp, 16.0.dp, 0.0.dp, 0.0.dp)
@Stable
val ShapeBottom32 = RoundedCornerShape(0.0.dp, 0.0.dp, 16.0.dp, 16.0.dp)
