package app.flujo.launcher.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.flujo.launcher.data.AppEntry

@Composable
fun AppIcon(app: AppEntry, iconSize: Dp) {
    val bitmap = app.icon
    if (bitmap != null) {
        Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(iconSize))
    } else {
        Spacer(Modifier.size(iconSize))
    }
}

@Composable
fun SearchGlyph(color: Color, glyphSize: Dp = 16.dp) {
    Canvas(Modifier.size(glyphSize)) {
        val s = this.size.minDimension
        val w = s * 0.09f
        drawCircle(color, radius = s * 0.32f, center = Offset(s * 0.42f, s * 0.42f), style = Stroke(width = w))
        drawLine(color, Offset(s * 0.66f, s * 0.66f), Offset(s * 0.92f, s * 0.92f), strokeWidth = w, cap = StrokeCap.Round)
    }
}

@Composable
fun SearchPill(onClick: () -> Unit) {
    val c = LocalFlujo.current
    val shape = RoundedCornerShape(24.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(c.card)
            .border(1.dp, c.line, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchGlyph(c.dim)
        Spacer(Modifier.width(10.dp))
        Text("Escribe para buscar o abrir", color = c.dim, fontSize = 15.sp)
    }
}

@Composable
fun FlujoSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    val c = LocalFlujo.current
    val x by animateDpAsState(if (checked) 21.dp else 3.dp, label = "thumb")
    Box(
        Modifier
            .size(44.dp, 26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (checked) c.accent else c.line)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
    ) {
        Box(
            Modifier
                .offset(x = x, y = 3.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(if (checked) c.bg else c.fg),
        )
    }
}
