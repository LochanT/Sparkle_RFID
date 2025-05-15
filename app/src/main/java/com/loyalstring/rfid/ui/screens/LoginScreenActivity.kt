package com.loyalstring.rfid.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loyalstring.rfid.R

import com.loyalstring.rfid.ui.theme.SparkleRFIDTheme
import com.loyalstring.rfid.ui.utils.BackgroundGradient

class LoginScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SparkleRFIDTheme {
                LoginScreen()
                }
            }
        }
    }



    @Preview(showSystemUi = true, showBackground = true)
    @Composable
    fun LoginScreen() {
        // State variables to store user input
        val userName = remember {
            mutableStateOf("")
        }
        val userPassword = remember {
            mutableStateOf("")
        }

        Column(modifier = Modifier
            .fillMaxHeight()
            .padding(0.dp)) {

            Text(text = "Welcome to Sparkle RFID", textAlign = TextAlign.Center,fontSize = 40.sp, color = Color.Blue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 50.dp, 20.dp, 20.dp)
            )
            Text(text = "Please Login to Continue", textAlign = TextAlign.Center,fontSize = 20.sp, color = Color.Blue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 50.dp, 0.dp, 70.dp)

            )


            // Username input field
            OutlinedTextField(value = userName.value, onValueChange = {
                userName.value = it
            },
                label = {
                    Text(text = "username")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 20.dp, 0.dp, 0.dp)
            )

            // Password input field
            OutlinedTextField(value = userPassword.value, onValueChange = {
                userPassword.value = it
            },
                label = {
                    Text(text = "password")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 20.dp, 0.dp, 10.dp),
                visualTransformation = PasswordVisualTransformation()

            )
            var isChecked by remember { mutableStateOf(false) }

            Row(verticalAlignment = Alignment.CenterVertically) {


                Checkbox()


                Text(text = "Forgot Password?", textAlign = TextAlign.Center,fontSize = 18.sp, color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5F)

                )
            }

            // Login button

            GradientButton(
                "Login", gradient = BackgroundGradient,
                onClick = { /* Handle login */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)

            )

                Box(
                    modifier = Modifier
                        .fillMaxSize()  // Box takes the full screen
                        .padding(16.dp)

                ) {
                    Row {
                        Text(
                            text = "Trouble Login?",
                            modifier = Modifier.weight(0.5F) // 👈 Align this Text to Bottom Center
                        )
                        Text(
                            text = "Contact Us",
                            modifier = Modifier.weight(0.5F) // 👈 Align this Text to Bottom Center
                        )
                    }

                }






        }


    }
@Composable
fun GradientButton(
    text: String,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(0.dp)
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = shape,
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp, 10.dp, 0.dp, 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = gradient, shape = shape)
                .clip(shape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, color = Color.White, fontSize = 20.sp, modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center )
        }
    }
}
@Composable
fun Checkbox() {
    var isChecked by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { isChecked = it }
        )
        Text(text = "Remember Me", fontSize = 18.sp)


    }
}






