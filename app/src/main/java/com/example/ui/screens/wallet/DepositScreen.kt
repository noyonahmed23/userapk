package com.example.ui.screens.wallet

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(
  currentUser: UserProfile,
  myTeam: Team?,
  paymentSettings: PaymentMethodSettings,
  onBack: () -> Unit,
  onSubmitDeposit: (
    walletType: DepositWalletType,
    teamId: String?,
    teamName: String?,
    paymentMethod: String,
    senderNumber: String,
    amount: Double,
    transactionId: String
  ) -> Unit,
  modifier: Modifier = Modifier
) {
  val clipboardManager = LocalClipboardManager.current
  var selectedWalletType by remember { mutableStateOf(if (myTeam != null) DepositWalletType.TEAM_WALLET else DepositWalletType.REGULAR_WALLET) }
  var selectedPaymentMethod by remember { mutableStateOf("bKash") } // "bKash" or "Nagad"
  var senderNumber by remember { mutableStateOf(currentUser.phone) }
  var amountInput by remember { mutableStateOf("300") }
  var transactionIdInput by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var copiedToast by remember { mutableStateOf<String?>(null) }

  BackHandler {
    onBack()
  }

  val activeAdminNumber = if (selectedPaymentMethod == "bKash") paymentSettings.bkashNumber else paymentSettings.nagadNumber
  val activeInstruction = if (selectedPaymentMethod == "bKash") paymentSettings.bkashInstruction else paymentSettings.nagadInstruction

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "ডিপোজিট (Add Deposit)",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextPrimary
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = TextPrimary
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = GamingDarkSurface,
          titleContentColor = TextPrimary
        )
      )
    },
    containerColor = GamingDarkBackground,
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. SELECT DEPOSIT TYPE (TEAM WALLET / REGULAR WALLET)
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Select Deposit Type",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "ডিপোজিট গন্তব্য নির্বাচন করুন",
            color = TextSecondary,
            fontSize = 12.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Team Wallet Option
            val isTeamSel = selectedWalletType == DepositWalletType.TEAM_WALLET
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isTeamSel) CyberCyan.copy(alpha = 0.15f) else GamingDarkSurfaceVariant,
              border = BorderStroke(
                width = if (isTeamSel) 1.5.dp else 1.dp,
                color = if (isTeamSel) CyberCyan else GamingCardBorder
              ),
              modifier = Modifier
                .weight(1f)
                .clickable { selectedWalletType = DepositWalletType.TEAM_WALLET }
                .testTag("select_team_wallet")
            ) {
              Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(
                  imageVector = Icons.Default.Groups,
                  contentDescription = null,
                  tint = if (isTeamSel) CyberCyan else TextSecondary,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Team Wallet",
                  color = if (isTeamSel) CyberCyan else TextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
                Text(
                  text = "টিম স্কোয়াড ফান্ড",
                  color = TextSecondary,
                  fontSize = 10.sp
                )
              }
            }

            // Regular Wallet Option
            val isRegSel = selectedWalletType == DepositWalletType.REGULAR_WALLET
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isRegSel) CyberOrange.copy(alpha = 0.15f) else GamingDarkSurfaceVariant,
              border = BorderStroke(
                width = if (isRegSel) 1.5.dp else 1.dp,
                color = if (isRegSel) CyberOrange else GamingCardBorder
              ),
              modifier = Modifier
                .weight(1f)
                .clickable { selectedWalletType = DepositWalletType.REGULAR_WALLET }
                .testTag("select_regular_wallet")
            ) {
              Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(
                  imageVector = Icons.Default.AccountBalanceWallet,
                  contentDescription = null,
                  tint = if (isRegSel) CyberOrange else TextSecondary,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Regular Wallet",
                  color = if (isRegSel) CyberOrange else TextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
                Text(
                  text = "ব্যক্তিগত ওয়ালেট",
                  color = TextSecondary,
                  fontSize = 10.sp
                )
              }
            }
          }

          // Team Wallet Rules & Current Team Display
          if (selectedWalletType == DepositWalletType.TEAM_WALLET) {
            Spacer(modifier = Modifier.height(14.dp))
            if (myTeam != null) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = GamingDarkBackground,
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(44.dp)
                      .clip(CircleShape)
                      .background(Brush.linearGradient(listOf(CyberCyan, CyberBlue))),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = myTeam.name.take(2).uppercase(),
                      color = Color.Black,
                      fontWeight = FontWeight.Black,
                      fontSize = 16.sp
                    )
                  }
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = myTeam.name,
                      color = TextPrimary,
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp
                    )
                    Text(
                      text = "ট্যাগ: [${myTeam.tag}] • আইডি: ${myTeam.publicId}",
                      color = TextSecondary,
                      fontSize = 11.sp
                    )
                    Text(
                      text = "বর্তমান টিম ব্যালেন্স: ৳${myTeam.balance.toInt()}",
                      color = CyberGreen,
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 12.sp
                    )
                  }
                }
              }
            } else {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = CyberRed.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, CyberRed.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = CyberRed,
                    modifier = Modifier.size(20.dp)
                  )
                  Text(
                    text = "আপনি কোনো টিমের সদস্য নন! টিম ওয়ালেটে ডিপোজিট করার জন্য আপনাকে প্রথমে একটি টিমে যুক্ত থাকতে হবে। দয়া করে Regular Wallet নির্বাচন করুন অথবা একটি টিমে জয়েন করুন।",
                    color = CyberRed,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                  )
                }
              }
            }
          } else {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = GamingDarkBackground,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text(text = "আপনার ব্যক্তিগত অ্যাকাউন্ট", color = TextSecondary, fontSize = 11.sp)
                  Text(text = currentUser.fullName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text(text = "বর্তমান ব্যালেন্স", color = TextSecondary, fontSize = 11.sp)
                  Text(text = "৳${currentUser.walletBalance.toInt()} টাকা", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
              }
            }
          }
        }
      }

      // 2. PAYMENT METHODS (bKash & Nagad)
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Payment Method",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
          Text(
            text = "পেমেন্ট মাধ্যম বেছে নিন ও নির্ধারিত নম্বরে টাকা পাঠান",
            color = TextSecondary,
            fontSize = 12.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // bKash Button
            val isBkash = selectedPaymentMethod == "bKash"
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isBkash) Color(0xFFE2136E).copy(alpha = 0.2f) else GamingDarkSurfaceVariant,
              border = BorderStroke(
                width = if (isBkash) 1.5.dp else 1.dp,
                color = if (isBkash) Color(0xFFE2136E) else GamingCardBorder
              ),
              modifier = Modifier
                .weight(1f)
                .clickable { selectedPaymentMethod = "bKash" }
                .testTag("method_bkash")
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "bKash",
                  color = if (isBkash) Color(0xFFE2136E) else TextPrimary,
                  fontWeight = FontWeight.Black,
                  fontSize = 16.sp
                )
                Text(
                  text = "বিকাশ পার্সোনাল",
                  color = TextSecondary,
                  fontSize = 11.sp
                )
              }
            }

            // Nagad Button
            val isNagad = selectedPaymentMethod == "Nagad"
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isNagad) Color(0xFFF7931E).copy(alpha = 0.2f) else GamingDarkSurfaceVariant,
              border = BorderStroke(
                width = if (isNagad) 1.5.dp else 1.dp,
                color = if (isNagad) Color(0xFFF7931E) else GamingCardBorder
              ),
              modifier = Modifier
                .weight(1f)
                .clickable { selectedPaymentMethod = "Nagad" }
                .testTag("method_nagad")
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "Nagad",
                  color = if (isNagad) Color(0xFFF7931E) else TextPrimary,
                  fontWeight = FontWeight.Black,
                  fontSize = 16.sp
                )
                Text(
                  text = "নগদ পার্সোনাল",
                  color = TextSecondary,
                  fontSize = 11.sp
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Number Card with Copy Action
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = GamingDarkBackground,
            border = BorderStroke(1.dp, GamingCardBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = "$selectedPaymentMethod Personal Number",
                  color = TextSecondary,
                  fontSize = 11.sp
                )
                Text(
                  text = activeAdminNumber,
                  color = CyberGreen,
                  fontWeight = FontWeight.Black,
                  fontSize = 18.sp,
                  letterSpacing = 1.sp
                )
              }

              IconButton(
                onClick = {
                  clipboardManager.setText(AnnotatedString(activeAdminNumber))
                  copiedToast = "$selectedPaymentMethod নম্বর কপি করা হয়েছে!"
                },
                modifier = Modifier
                  .clip(CircleShape)
                  .background(GamingDarkSurfaceVariant)
              ) {
                Icon(
                  imageVector = Icons.Default.ContentCopy,
                  contentDescription = "নম্বর কপি করুন",
                  tint = CyberOrange,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }

          copiedToast?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "✓ $it", color = CyberGreen, fontSize = 11.sp)
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = activeInstruction,
            color = CyberCyan,
            fontSize = 11.sp,
            lineHeight = 16.sp
          )
        }
      }

      // 3. DEPOSIT SUBMISSION FORM
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Payment Information",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
          Text(
            text = "টাকা পাঠানোর পর প্রেরক নম্বর ও TrxID লিখে সাবমিট করুন",
            color = TextSecondary,
            fontSize = 12.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = senderNumber,
            onValueChange = { senderNumber = it },
            label = { Text("আপনার প্রেরক নম্বর (Sender Number)") },
            placeholder = { Text("01XXXXXXXXX") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("sender_number_input")
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = amountInput,
            onValueChange = { amountInput = it },
            label = { Text("ডিপোজিট পরিমাণ (Amount in BDT)") },
            placeholder = { Text("মিনিমাম ৫০ টাকা") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("deposit_amount_input")
          )

          // Preset amount chips
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf("100", "200", "300", "500", "1000").forEach { preset ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (amountInput == preset) CyberOrange else GamingDarkSurfaceVariant,
                modifier = Modifier
                  .weight(1f)
                  .clickable { amountInput = preset }
              ) {
                Text(
                  text = "৳$preset",
                  color = if (amountInput == preset) Color.Black else TextSecondary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.padding(vertical = 6.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = transactionIdInput,
            onValueChange = { transactionIdInput = it.uppercase() },
            label = { Text("ট্রানজেকশন আইডি (Transaction ID / TrxID)") },
            placeholder = { Text("যেমন: 9J382XPL") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("trx_id_input")
          )

          errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = CyberRed.copy(alpha = 0.15f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = it,
                color = CyberRed,
                fontSize = 11.sp,
                modifier = Modifier.padding(8.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          val canSubmitTeam = selectedWalletType != DepositWalletType.TEAM_WALLET || myTeam != null
          Button(
            onClick = {
              val amt = amountInput.toDoubleOrNull() ?: 0.0
              if (selectedWalletType == DepositWalletType.TEAM_WALLET && myTeam == null) {
                errorMessage = "টিম ওয়ালেটে ডিপোজিট করতে হলে আপনাকে কোনো টিমে থাকতে হবে!"
                return@Button
              }
              if (amt < 50.0) {
                errorMessage = "সর্বনিম্ন ডিপোজিট পরিমাণ ৫০ টাকা!"
                return@Button
              }
              if (senderNumber.length < 11) {
                errorMessage = "দয়া করে সঠিক ১১ ডিজিটের প্রেরক নম্বর দিন!"
                return@Button
              }
              if (transactionIdInput.trim().length < 6) {
                errorMessage = "দয়া করে সঠিক ট্রানজেকশন আইডি (TrxID) প্রদান করুন!"
                return@Button
              }

              errorMessage = null
              onSubmitDeposit(
                selectedWalletType,
                if (selectedWalletType == DepositWalletType.TEAM_WALLET) myTeam?.id else null,
                if (selectedWalletType == DepositWalletType.TEAM_WALLET) myTeam?.name else null,
                selectedPaymentMethod,
                senderNumber.trim(),
                amt,
                transactionIdInput.trim()
              )
            },
            enabled = canSubmitTeam,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (selectedWalletType == DepositWalletType.TEAM_WALLET) CyberCyan else CyberGreen,
              contentColor = Color.Black
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("submit_deposit_button")
          ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "ডিপোজিট রিকোয়েস্ট সাবমিট করুন",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
          }
        }
      }
    }
  }
}
