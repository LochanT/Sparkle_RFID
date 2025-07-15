package com.loyalstring.rfid.ui.screens



import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.sparklepos.models.loginclasses.customerBill.EmployeeList
import com.loyalstring.rfid.R
import com.loyalstring.rfid.data.local.entity.OrderItem
import com.loyalstring.rfid.data.model.login.Employee
import com.loyalstring.rfid.data.model.order.BranchResponse
import com.loyalstring.rfid.ui.utils.GradientButtonIcon
import com.loyalstring.rfid.ui.utils.UserPreferences
import com.loyalstring.rfid.viewmodel.OrderViewModel
import com.loyalstring.rfid.viewmodel.SingleProductViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


// Sample OrderDetails data class
data class OrderDetailsData(
    val productName:String,
    val itemCode:String,
    val sku:String,
    val NetWt:String,
    val totalWt:String,
    val packingWt:String,
    val GrossWt:String,
    val StoneWT:String,
    val dimondWt:String,
    val ratePerGram:String,
    val finePer:String,
    val wastagePer:String,
    val qty:String,
    val hallMarkAmt:String,
    val mrp:String,


    val branch: String,
    val exhibition: String,
    val remark: String,
    val purity: String,
    val size: String,
    val length: String,
    val typeOfColors: String,
    val screwType: String,
    val polishType: String,
    val finePercentage: String,
    val wastage: String
)

@Composable
fun OrderDetailsDialogEditAndDisplay(

    selectedCustomerId: Int?,
    selectedCustomer: EmployeeList?,
    selectedItem: OrderItem?,
    branchList: List<BranchResponse>,
    onDismiss: () -> Unit,
    onSave: (OrderDetailsData) -> Unit,
    viewModel: SingleProductViewModel = hiltViewModel(),

    ) {
    Log.e("TAG", "RFID Code: ${selectedItem?.rfidCode+" image url"+selectedItem?.image.toString()}")

    val orderViewModel: OrderViewModel = hiltViewModel()
    val singleProductViewModel: SingleProductViewModel = hiltViewModel()
    // Form state
    var branch by remember { mutableStateOf("") }
    var exhibition by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }
    var purity by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var typeOfColors by remember { mutableStateOf("") }
    var screwType by remember { mutableStateOf("") }
    var polishType by remember { mutableStateOf("") }
    var finePercentage by remember { mutableStateOf("") }
    var wastage by remember { mutableStateOf("") }
    var orderDate by remember { mutableStateOf("") }
    var deliverDate by remember { mutableStateOf("") }

    var productName by remember { mutableStateOf("") }
    var itemCode by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var NetWt by remember { mutableStateOf("") }
    var totalWt by remember { mutableStateOf("") }
    var dimondWt by remember { mutableStateOf("") }
    var packingWt by remember { mutableStateOf("") }
    var grossWT by remember { mutableStateOf("") }
    var stoneWt by remember { mutableStateOf("") }
    var ratePerGRam by remember { mutableStateOf("") }
    var finePer by remember { mutableStateOf("") }
    var wastagePer by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var hallMarkAmt by remember { mutableStateOf("") }
    var mrp by remember { mutableStateOf("") }
    var stoneAmt by remember { mutableStateOf("") }

    branch=selectedItem?.branchName.toString()
    productName =selectedItem?.productName.toString()
    itemCode=selectedItem?.itemCode.toString()
    totalWt=selectedItem?.totalWt.toString()
    stoneWt=selectedItem?.stoneWt.toString()
    dimondWt=selectedItem?.dimondWt.toString()
    NetWt=selectedItem?.nWt.toString()
    exhibition=selectedItem?.exhibition.toString()
    sku=selectedItem?.sku.toString()
    purity= selectedItem?.purity.toString()
    size=selectedItem?.size.toString()
    length=selectedItem?.length.toString()
    stoneAmt=selectedItem?.stoneAmt.toString()
    packingWt=selectedItem?.packingWt.toString()
    NetWt=selectedItem?.nWt.toString()
    remark=selectedItem?.remark.toString()
    typeOfColors=selectedItem?.typeOfColor.toString()
    screwType=selectedItem?.screwType.toString()
    polishType=selectedItem?.polishType.toString()
    finePercentage=selectedItem?.finePer.toString()
    wastagePer=selectedItem?.wastage.toString()
    orderDate=selectedItem?.orderDate.toString()
    deliverDate=selectedItem?.deliverDate.toString()
    qty=selectedItem?.qty.toString()
    hallMarkAmt=selectedItem?.hallmarkAmt.toString()
    mrp=selectedItem?.mrp.toString()

    val purityList by singleProductViewModel.purityResponse1.collectAsState()
    val skuList by singleProductViewModel.skuResponse1.collectAsState()

    val colorsList = listOf(
        "Yellow Gold",
        "White Gold",
        "Rose Gold",
        "Green Gold",
        "Black Gold",
        "Blue Gold",
        "Purple Gold"
    )
    val screwList = listOf("Type 1", "Type 2", "Type 3")
    val polishList = listOf("High Polish", "Matte Finish", "Satin Finish", "Hammered")

    var expandedBranch by remember { mutableStateOf(false) }
    var expandedExhibition by remember { mutableStateOf(false) }
    var expandedPurity by remember { mutableStateOf(false) }
    var expandedSize by remember { mutableStateOf(false) }
    var expandedLength by remember { mutableStateOf(false) }
    var expandedColors by remember { mutableStateOf(false) }
    var expandedScrew by remember { mutableStateOf(false) }
    var expandedPolish by remember { mutableStateOf(false) }
    var expandedSKU by remember { mutableStateOf(false) }

    // Inside @Composable

    val calendar = Calendar.getInstance()
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val context = LocalContext.current
    val employee = UserPreferences.getInstance(context).getEmployee(Employee::class.java)
    /*  LaunchedEffect(Unit) {
          employee?.clientCode?.let {
              orderViewModel.getAllBranchList(ClientCodeRequest(it))
          }
      }*/


    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()

                    .verticalScroll(rememberScrollState())
            ) {
                // Title
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp) // Toolbar-like height
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center   // or Alignment.Center for centered text
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.order_edit_icon), // or use any Material icon you prefer
                            contentDescription = "Custom Order Icon",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Custom Order Fields",
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                    // .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp) // Set the height you need
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center // Center horizontally
                    ) {
                        AsyncImage(
                            model =selectedItem?.image,
                            contentDescription = "Image from URL",
                            placeholder = painterResource(R.drawable.add_photo), // Optional
                            error = painterResource(R.drawable.add_photo),       // Optional
                            modifier = Modifier.size(100.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // Use your DropdownMenuField & input rows here
                    // Example: Branch dropdown
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        color = Color(0xFFF2F2F3),
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            if (branchList.isEmpty()) {
                                //Text("Loading branches...", modifier = Modifier.padding(8.dp))
                            } else {
                                DropdownMenuFieldDisplay(
                                    label = "Branch",
                                    options = branchList,
                                    selectedValue = branch,
                                    expanded = expandedBranch,
                                    onValueChange = { branch = it },
                                    onExpandedChange = { expandedBranch = it },
                                    labelColor = Color.Black,
                                    getOptionLabel = { it.BranchName },
                                    enabled = false
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Product Name",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (productName.isEmpty()) {
                                Text(
                                    text = "Enter Product",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = productName,
                                readOnly = true,
                                onValueChange = { productName = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ItemCode",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (itemCode.isEmpty()) {
                                Text(
                                    text = "Enter Itemcode",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = itemCode,
                                readOnly = true,
                                onValueChange = { itemCode = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Weight",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (totalWt.isEmpty()) {
                                Text(
                                    text = "Enter total wt",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = totalWt,
                                onValueChange = { totalWt = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Packing Wt",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (packingWt.isEmpty()) {
                                Text(
                                    text = "Enter packing wt",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = packingWt,
                                onValueChange = { packingWt = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Stone Weight",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (stoneWt.isEmpty()) {
                                Text(
                                    text = "Enter stone wt",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = stoneWt,
                                onValueChange = { stoneWt = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dimond Weight",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (dimondWt.isEmpty()) {
                                Text(
                                    text = "Enter dimond wt",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = dimondWt,
                                onValueChange = { dimondWt = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Net Weight",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (NetWt.isEmpty()) {
                                Text(
                                    text = "Enter net wt",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = NetWt,
                                onValueChange = { NetWt = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Exhibition",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (exhibition.isEmpty()) {
                                Text(
                                    text = "Enter exhibition",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = exhibition,
                                onValueChange = { exhibition = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Remark",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (remark.isEmpty()) {
                                Text(
                                    text = "Enter remark",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = remark,
                                onValueChange = { remark = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        color = Color(0xFFF2F2F3),
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            DropdownMenuFieldDisplay(
                                "SKU",
                                skuList,
                                sku,
                                expandedSKU,
                                { sku = it },
                                { expandedSKU = it },
                                getOptionLabel = { it.PurityName.toString() },
                                enabled = false
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        color = Color(0xFFF2F2F3),
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            DropdownMenuFieldDisplay(
                                "Purity",
                                purityList,
                                purity,
                                expandedPurity,
                                { purity = it },
                                { expandedPurity = it },
                                getOptionLabel = { it.PurityName.toString() },
                                enabled = false
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Size",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (size.isEmpty()) {
                                Text(
                                    text = "Enter size",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = size,
                                onValueChange = { size = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Length",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (length.isEmpty()) {
                                Text(
                                    text = "Enter length",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = length,
                                onValueChange = { length = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))


                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        color = Color(0xFFF2F2F3),
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            DropdownMenuFieldDisplay(
                                "Colors",
                                colorsList,
                                typeOfColors,
                                expandedColors,
                                { typeOfColors = it },
                                { expandedColors = it },
                                getOptionLabel = { it.toString() },
                                enabled = true
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        color = Color(0xFFF2F2F3),
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            DropdownMenuFieldDisplay(
                                "Screw Type",
                                screwList,
                                screwType,
                                expandedScrew,
                                { screwType = it },
                                { expandedScrew = it },
                                getOptionLabel = { it.toString() },
                                enabled = true
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        color = Color(0xFFF2F2F3),
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            DropdownMenuFieldDisplay(
                                "Polish Type",
                                polishList,
                                polishType,
                                expandedPolish,
                                { polishType = it },
                                { expandedPolish = it },
                                getOptionLabel = { it.toString() },
                                enabled = true
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fine %",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (finePercentage.isEmpty()) {
                                Text(
                                    text = "Enter Fine %",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = finePercentage,
                                onValueChange = { finePercentage = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Wastage",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (wastage.isEmpty()) {
                                Text(
                                    text = "Enter wastage",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = wastage,
                                onValueChange = { wastage = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Order Date",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .clickable {
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            val selectedDate = Calendar.getInstance().apply {
                                                set(year, month, dayOfMonth)
                                            }
                                            orderDate = dateFormatter.format(selectedDate.time)
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (orderDate.isEmpty()) "Enter Order Date" else orderDate,
                                    fontSize = 13.sp,
                                    color = if (orderDate.isEmpty()) Color.Gray else Color.Black,
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    painter = painterResource(id = R.drawable.ic_calender), // replace with your calendar icon
                                    contentDescription = "Calendar",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Deliver Date",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .clickable {
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            val selectedDate = Calendar.getInstance().apply {
                                                set(year, month, dayOfMonth)
                                            }
                                            deliverDate = dateFormatter.format(selectedDate.time)
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (deliverDate.isEmpty()) "Enter Deliver Date" else orderDate,
                                    fontSize = 13.sp,
                                    color = if (deliverDate.isEmpty()) Color.Gray else Color.Black,
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    painter = painterResource(id = R.drawable.ic_calender), // replace with your calendar icon
                                    contentDescription = "Calendar",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quantity",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (qty.isEmpty()) {
                                Text(
                                    text = "Enter quantity",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = qty,
                                onValueChange = { qty = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hallmark Amt",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (hallMarkAmt.isEmpty()) {
                                Text(
                                    text = "Enter hallmark amt",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = hallMarkAmt,
                                onValueChange = { hallMarkAmt = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp), // only inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mrp",
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(start = 2.dp),
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .padding(start = 6.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                                .height(32.dp)

                                .background(Color.White, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (mrp.isEmpty()) {
                                Text(
                                    text = "Enter mrp",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            BasicTextField(
                                value = mrp,
                                onValueChange = { mrp = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp) // minimal inner padding for cursor spacing
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))





                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        GradientButtonIcon(
                            text = "Cancel",
                            onClick = {
                                println("Form Reset")
                                onDismiss()
                                // showAddCustomerDialog = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp) // reduce height here
                                .padding(start = 8.dp, bottom = 16.dp),
                            icon = painterResource(id = R.drawable.ic_cancel),
                            iconDescription = "Check Icon",
                            fontSize = 12
                        )
                        Spacer(modifier = Modifier.width(8.dp)) // Add space between buttons

                        val netWt: Double = (selectedItem?.grWt?.toDoubleOrNull()
                            ?: 0.0) - (selectedItem?.stoneWt?.toDoubleOrNull() ?: 0.0)

                        val finePercent = selectedItem?.finePer?.toDoubleOrNull() ?: 0.0
                        val wastagePercent = selectedItem?.wastage?.toDoubleOrNull() ?: 0.0


                        val finewt: Double =
                            ((finePercent / 100.0) * netWt) + ((wastagePercent / 100.0) * netWt)
                        val metalAmt: Double = (selectedItem?.nWt?.toDoubleOrNull()
                            ?: 0.0) * (selectedItem?.todaysRate?.toDoubleOrNull() ?: 0.0)

                        val makingPerGram =
                            selectedItem?.makingPerGram?.toDoubleOrNull() ?: 0.0
                        val fixMaking = selectedItem?.makingFixedAmt?.toDoubleOrNull() ?: 0.0
                        val extraMakingPercent =
                            selectedItem?.makingPercentage?.toDoubleOrNull() ?: 0.0
                        val fixWastage = selectedItem?.makingFixedWastage?.toDoubleOrNull() ?: 0.0

                        val makingAmt: Double =
                            ((makingPerGram / 100.0) * netWt) +
                                    fixMaking +
                                    ((extraMakingPercent / 100.0) * netWt) +
                                    fixWastage

                        val totalStoneAmount =
                            selectedItem?.stoneAmt?.toDoubleOrNull() ?: 0.0
                        val diamondAmount =
                            selectedItem?.diamondAmt?.toDoubleOrNull() ?: 0.0
                        val safeMetalAmt = metalAmt ?: 0.0
                        val safeMakingAmt = makingAmt ?: 0.0

                        val itemAmt: Double =
                            totalStoneAmount + diamondAmount + safeMetalAmt + safeMakingAmt
                        GradientButtonIcon(
                            text = "OK",
                            onClick = {

                                val orderItem = OrderItem(
                                    branchId = selectedItem?.branchId.toString(),
                                    branchName = branch,
                                    exhibition = exhibition,
                                    remark = remark,
                                    purity = purity,
                                    size = size,
                                    length = length,
                                    typeOfColor = typeOfColors,
                                    screwType = screwType,
                                    polishType = polishType,
                                    finePer = finePercentage,
                                    wastage = wastage,
                                    orderDate = orderDate,
                                    deliverDate = deliverDate,
                                    productName = productName,
                                    itemCode = itemCode,
                                    rfidCode = selectedItem?.rfidCode.toString(),
                                    itemAmt = itemAmt.toString(),
                                    grWt = grossWT,
                                    nWt = NetWt,
                                    stoneAmt =stoneAmt ,
                                    finePlusWt = selectedItem?.finePlusWt,
                                    packingWt = selectedItem?.packingWt.toString(),
                                    totalWt = selectedItem?.totalWt.toString(),
                                    stoneWt = selectedItem?.stoneWt.toString(),
                                    dimondWt = selectedItem?.dimondWt.toString(),
                                    sku = selectedItem?.sku.toString(),
                                    qty = selectedItem?.qty.toString(),
                                    hallmarkAmt = selectedItem?.hallmarkAmt.toString(),
                                    mrp = selectedItem?.mrp.toString(),
                                    image=selectedItem?.image.toString(),
                                    netAmt="",
                                    diamondAmt=selectedItem?.diamondAmt.toString(),
                                    categoryId = selectedItem?.categoryId!!,
                                    categoryName = selectedItem?.categoryName!!,
                                    productId = selectedItem?.productId!!,
                                    productCode = selectedItem?.productName!!,
                                    skuId =selectedItem?.skuId!!,
                                    designid = selectedItem?.designid!!,
                                    designName =selectedItem?.designName!!,
                                    purityid = selectedItem?.purityid!!,
                                    counterId = selectedItem?.counterId!!,
                                    counterName ="",
                                    companyId = 0,
                                    epc = selectedItem?.epc!!,
                                    tid = selectedItem?.tid!!,
                                    todaysRate = selectedItem?.todaysRate.toString(),
                                    makingPercentage = extraMakingPercent.toString(),
                                    makingFixedAmt = fixMaking.toString(),
                                    makingFixedWastage = fixWastage.toString(),
                                    makingPerGram = makingPerGram.toString()


                                )
                                orderViewModel.insertOrderItemToRoom(orderItem)
                                onDismiss()

                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp) // Adjust height as needed
                                .padding(end = 8.dp, bottom = 16.dp),
                            icon = painterResource(id = R.drawable.check_circle),
                            iconDescription = "Check Icon",
                            fontSize = 12
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun <T> DropdownMenuFieldDisplay(
    label: String,
    options: List<T>,
    selectedValue: String,
    expanded: Boolean,
    onValueChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    labelColor: Color = Color.Black,
    getOptionLabel: (T) -> String,
    enabled: Boolean
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier
                    .weight(0.4f)
                    .padding(start = 8.dp),
                fontSize = 12.sp,
                color = labelColor
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 2.dp, end = 0.dp)
                    .height(50.dp)
                    .clickable { onExpandedChange(true) }
                    .padding(horizontal = 10.dp, vertical = 0.dp)
                    .background(Color.White, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedValue.isEmpty()) "Select $label" else selectedValue,
                        style = TextStyle(fontSize = 12.sp, color = Color.Black),
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Dropdown"
                    )
                }
            }
            if (enabled) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(getOptionLabel(option)) },
                            onClick = {
                                onValueChange(getOptionLabel(option))
                                onExpandedChange(false)
                            }
                        )
                    }
                }
            }
        }
    }
}


