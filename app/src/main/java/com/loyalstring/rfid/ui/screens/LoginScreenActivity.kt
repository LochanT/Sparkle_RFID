
package com.loyalstring.rfid.ui.screens

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
//import androidx.compose.ui.graphics.drawscope.drawPath
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString

import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.loyalstring.rfid.ui.theme.SparkleRFIDTheme
import com.loyalstring.rfid.ui.utils.BackGroundLinerGradient
import com.loyalstring.rfid.ui.utils.BackgroundGradient



import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*

import androidx.compose.ui.text.input.VisualTransformation
import com.loyalstring.rfid.MainActivity
import com.loyalstring.rfid.ui.utils.poppins
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()

                    .padding(0.dp)
            ) {
                CurvedGradientHeader()
                Spacer(modifier = Modifier.height(0.dp))
                LoginScreen1() // Don't use fillMaxSize() inside this

            }
        }
    }


    @Composable
    fun LoginScreen1() {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var rememberMe by remember { mutableStateOf(false) }

        var passwordVisible by remember { mutableStateOf(false) }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /* Text(
                 text = "Login",
                 fontSize = 24.sp,
                 modifier = Modifier.padding(bottom = 8.dp)
             )*/

            // Username Field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true
            )

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(id = if (passwordVisible) R.drawable.ic_action_eye else R.drawable.ic_action_eye_off), // Use drawable for visibility toggle
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
            )

            // Remember Me and Forgot Password Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it }
                    )
                    Text(/*"Remember Me"*/
                        text = "Remember Me",
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold,)
                }

                Text(
                    text = "Forgot Password?",
                    color = Color.Blue,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        // Handle forgot password click
                    }
                )
            }

            // Gradient Login Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundGradient)
                    .clickable(
                        enabled = !isLoading
                    ) {
                        if (username.isEmpty() || password.isEmpty()) {
                            Toast.makeText(
                                this@LoginScreenActivity,
                                "Please enter username and password",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@clickable
                        }

                        isLoading = true

                        val intent = Intent(this@LoginScreenActivity, MainActivity::class.java)
                        startActivity(intent)

                        if (this@LoginScreenActivity is Activity) {
                            finish()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        "Login",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))
            TroubleLoginText {
                // Handle "Contact Us" click here
                // Example: open support screen or email intent
                Log.d("LoginScreen", "Contact Us clicked")
            }
        }
    }
    @Composable
    fun TroubleLoginText(onContactClick: () -> Unit) {
        val annotatedText = buildAnnotatedString {
            append("Trouble login? ")

            // Add annotation to "Contact US"
            pushStringAnnotation(tag = "contact", annotation = "contact")
            withStyle(
                style = SpanStyle(
                    color = Color.Blue,
                    fontWeight = FontWeight.SemiBold
                )
            ) {
                append("Contact US")
            }
            pop()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            ClickableText(
                text = annotatedText,
                onClick = { offset ->
                    annotatedText.getStringAnnotations(tag = "contact", start = offset, end = offset)
                        .firstOrNull()?.let {
                            onContactClick()
                        }
                },
                style = TextStyle(
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }


    @Composable
    fun CurvedGradientHeader(modifier: Modifier = Modifier) {
        val headerHeight = 250.dp // You can change this to your desired height

        Box(
            modifier = modifier
                .width(550.13666.dp)
                .height(300.8201.dp)
        ) {
            // Background curved gradient
            Canvas(
                Modifier
                    .width(550.13666.dp)
                    .height(300.8201.dp)
            ) {
                val width = size.width
                val height = size.height

                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(0f, height * 0.3f) // Start curve lower on left
                    quadraticBezierTo(
                        width * -0.3f, height * 1.3f, // Control point far left and deep
                        width, height * 0.75f         // End point remains right
                    )
                    lineTo(width, 0.56f)
                    close()
                }

                drawPath(
                    path = path,
                    BackGroundLinerGradient
                )
            }

            // Centered text on top of Canvas
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Welcome to", color = Color.White, fontSize = 35.sp, fontFamily = poppins,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Sparkle RFID", color = Color.White, fontSize = 35.sp, fontFamily = poppins,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(40.dp))
                Text("Please log in to continue", color = Color.White, fontSize = 14.sp,fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,)
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
                Text(
                    text = text, color = Color.White, fontSize = 20.sp, modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                )
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
}
