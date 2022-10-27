package com.kapps.pickeryt



import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kapps.pickeryt.ui.theme.PickerYTTheme
import com.kapps.pickeryt.ui.theme.gray
import com.kapps.pickeryt.ui.theme.orange
import com.kapps.pickeryt.ui.theme.white
import kotlin.math.abs
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var day by remember{
                mutableStateOf("16")
            }
            var month by remember {
                mutableStateOf("Jul")
            }
            var year by remember {
                mutableStateOf("1980")
            }
            PickerYTTheme {
                Box(
                    modifier = Modifier
                        .background(gray)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Column(
                        modifier = Modifier,
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Picker(
                            list = (1..31).toList(),
                            onValueChanged = {
                                day = it.toString()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            showAmount = 10
                        )
                        Picker(
                            listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"),
                        onValueChanged = {
                            month = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        showAmount = 6
                        )
                        Picker(
                            list = (1950..2010).toList(),
                            onValueChanged = {
                                year = it.toString()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            showAmount = 8
                        )
                    }
                    Rechtangle(
                        modifier = Modifier
                            .height(260.dp)
                            .width(50.dp)
                    )
                    Text(
                        "Date of Birth: $month $day $year",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 130.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                }
            }
        }
    }
}

@Composable
fun Rechtangle(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ){
        drawRoundRect(
            size = Size(size.width,size.height),
            style = Stroke(width = 10f, join = StrokeJoin.Round),
            color = orange,
            cornerRadius = CornerRadius(25f,25f)
        )
    }
}

@Composable
fun <T>Picker(
    list:List<T>,
    showAmount:Int = 10,
    modifier: Modifier = Modifier,
    style:PickerStyle = PickerStyle(),
    onValueChanged:(T)->Unit
) {

    val listCount by remember {
        mutableStateOf(list.size)
    }

    val correctionValue by remember {
        if(list.size%2 == 0){
            mutableStateOf(1)
        }else{
            mutableStateOf(0)
        }
    }

    var dragStartedX by remember {
        mutableStateOf(0f)
    }

    var currentDragX by remember {
        mutableStateOf(0f)
    }

    var oldX by remember {
        mutableStateOf(0f)
    }

    Canvas(
        modifier = modifier
            .pointerInput(true){
                detectDragGestures(
                    onDragStart = { offset ->
                        dragStartedX = offset.x
                    },
                    onDragEnd = {
                        val spacePerItem = size.width/showAmount
                        val rest = currentDragX % spacePerItem

                        val roundUp = abs(rest/spacePerItem).roundToInt() == 1
                        val newX = if(roundUp){
                            if(rest<0){
                                currentDragX + abs(rest) - spacePerItem
                            }else{
                                currentDragX - rest + spacePerItem
                            }
                        }else{
                            if(rest < 0){
                                currentDragX + abs(rest)
                            }else{
                                currentDragX - rest
                            }
                        }
                        currentDragX = newX.coerceIn(
                            minimumValue = -(listCount/2f)*spacePerItem,
                            maximumValue = (listCount/2f-correctionValue)*spacePerItem
                        )
                        val index = (listCount/2)+(currentDragX/spacePerItem).toInt()
                        onValueChanged(list[index])
                        oldX = currentDragX
                    },
                    onDrag = {change,_ ->
                        val changeX = change.position.x
                        val newX = oldX + (dragStartedX-changeX)
                        val spacePerItem = size.width/showAmount
                        currentDragX = newX.coerceIn(
                            minimumValue = -(listCount/2f)*spacePerItem,
                            maximumValue = (listCount/2f-correctionValue)*spacePerItem
                        )
                        val index = (listCount/2)+(currentDragX/spacePerItem).toInt()
                        onValueChanged(list[index])
                    }
                )
            }
    ){

        val top = 0f
        val bot = size.height

        drawContext.canvas.nativeCanvas.apply {
            drawRect(
                Rect(-2000,top.toInt(),size.width.toInt()+2000,bot.toInt()),
                Paint().apply {
                    color = white.copy(alpha = 0.8f).toArgb()
                    setShadowLayer(
                        30f,
                        0f,
                        0f,
                        android.graphics.Color.argb(50,0,0,0)
                    )
                }
            )
        }
        val spaceForEachItem = size.width/showAmount
        for(i in 0 until listCount){
            val currentX = i * spaceForEachItem - currentDragX -
                    ((listCount-1+correctionValue - showAmount)/2*spaceForEachItem)

            val lineStart = Offset(
                x = currentX ,
                y = 0f
            )

            val lineEnd = Offset(
                x = currentX,
                y = style.lineLength
            )

            drawLine(
                color = style.lineColor,
                strokeWidth = 1.5.dp.toPx(),
                start = lineStart,
                end = lineEnd
            )

            drawContext.canvas.nativeCanvas.apply {
                val y = style.lineLength + 5.dp.toPx() + style.textSize.toPx()

                drawText(
                    list[i].toString(),
                    currentX,
                    y,
                    Paint().apply {
                        textSize = style.textSize.toPx()
                        textAlign = Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                )
            }

        }


    }

}


data class PickerStyle(
    val lineColor:Color = orange,
    val lineLength:Float = 45f,
    val textSize:TextUnit = 16.sp
)