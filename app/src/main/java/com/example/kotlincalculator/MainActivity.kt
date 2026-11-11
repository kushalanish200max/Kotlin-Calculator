package com.example.kotlincalculator




import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                CalculatorApp()
            }
        }
    }
}

@Composable
fun CalculatorApp() {

    // Calculator states
    var display by rememberSaveable {
        mutableStateOf("0")
    }

    var storedNumber by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var selectedOperator by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var isNewInput by rememberSaveable {
        mutableStateOf(false)
    }

    // Enter numbers and decimal point
    fun enterDigit(value: String) {

        if (display == "Error") {
            display = "0"
        }

        if (isNewInput) {
            display = if (value == ".") "0." else value
            isNewInput = false
        } else if (value == ".") {
            if (!display.contains(".")) {
                display += "."
            }
        } else {
            display = if (display == "0") {
                value
            } else {
                display + value
            }
        }
    }

    // Handle button clicks
    fun handleButton(value: String) {

        when {
            value in listOf(
                "0", "1", "2", "3", "4",
                "5", "6", "7", "8", "9", "."
            ) -> enterDigit(value)

            value == "AC" -> {
                display = "0"
                storedNumber = null
                selectedOperator = null
                isNewInput = false
            }

            value == "±" -> {
                val number = display.toBigDecimalOrNull()
                if (number != null) {
                    display = formatNumber(number.negate())
                    isNewInput = false
                }
            }

            value == "%" -> {
                val number = display.toBigDecimalOrNull()
                if (number != null) {
                    display = formatNumber(
                        number.divide(
                            BigDecimal("100"),
                            8,
                            RoundingMode.HALF_UP
                        )
                    )
                    isNewInput = true
                }
            }

            value in listOf("+", "−", "×", "÷") -> {

                if (display.toBigDecimalOrNull() == null) return

                if (
                    storedNumber != null &&
                    selectedOperator != null &&
                    !isNewInput
                ) {
                    val result = calculate(
                        storedNumber!!,
                        display,
                        selectedOperator!!
                    )

                    if (result == null) {
                        display = "Error"
                        storedNumber = null
                        selectedOperator = null
                        isNewInput = true
                        return
                    }

                    display = result
                    storedNumber = result

                } else {
                    storedNumber = display
                }

                selectedOperator = value
                isNewInput = true
            }

            value == "=" -> {

                val first = storedNumber ?: return
                val operator = selectedOperator ?: return

                val result = calculate(
                    first,
                    display,
                    operator
                )

                display = result ?: "Error"

                storedNumber = null
                selectedOperator = null
                isNewInput = true
            }
        }
    }

    // Button layout
    val buttonRows = listOf(
        listOf("AC", "±", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "−"),
        listOf("1", "2", "3", "+"),
        listOf("0", ".", "=")
    )

    // Main calculator UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF12151C))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        // Display area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {

            Text(
                text = "KOTLIN CALCULATOR",
                color = Color.LightGray,
                fontSize = 14.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = display,
                color = Color.White,
                fontSize = if (display.length > 10) 38.sp else 56.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Calculator buttons
        buttonRows.forEach { row ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                row.forEach { button ->

                    val buttonWeight =
                        if (button == "0") 2f else 1f

                    CalculatorButton(
                        label = button,
                        modifier = Modifier
                            .weight(buttonWeight)
                            .height(68.dp)
                    ) {
                        handleButton(button)
                    }
                }
            }
        }
    }
}

// Reusable calculator button
@Composable
fun CalculatorButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    val operators = listOf("÷", "×", "−", "+", "=")
    val utilities = listOf("AC", "±", "%")

    val backgroundColor = when {
        label in operators -> Color(0xFFFF9F0A)
        label in utilities -> Color(0xFFD7DBE2)
        else -> Color(0xFF30343B)
    }

    val textColor = if (label in utilities) {
        Color(0xFF12151C)
    } else {
        Color.White
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {

        Text(
            text = label,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Actual arithmetic logic
fun calculate(
    first: String,
    second: String,
    operator: String
): String? {

    val a = first.toBigDecimalOrNull() ?: return null
    val b = second.toBigDecimalOrNull() ?: return null

    val result = when (operator) {

        "+" -> a.add(b)

        "−" -> a.subtract(b)

        "×" -> a.multiply(b)

        "÷" -> {
            if (b.compareTo(BigDecimal.ZERO) == 0) {
                return null
            }

            a.divide(
                b,
                8,
                RoundingMode.HALF_UP
            )
        }

        else -> return null
    }

    return formatNumber(result)
}

// Remove unnecessary decimal zeroes
fun formatNumber(number: BigDecimal): String {

    if (number.compareTo(BigDecimal.ZERO) == 0) {
        return "0"
    }

    return number.stripTrailingZeros().toPlainString()
}
