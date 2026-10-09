package com.example.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserProfile
import com.example.data.model.WalletTransaction
import com.example.ui.components.BengaliConfirmDialog
import com.example.ui.components.OnlineStatusBadge
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
  currentUser: UserProfile,
  transactions: List<WalletTransaction>,
  isWithdrawAllowedTime: Boolean,
  onUpdateProfile: (name: String, phone: String, ffUid: String, pubgUid: String) -> Unit,
  onRequestDeposit: (method: String, amount: Double, trxId: String) -> Unit,
  onRequestWithdraw: (method: String, number: String, amount: Double) -> Unit,
  onNavigateToTournaments: () -> Unit,
  onNavigateToChallenges: () -> Unit,
  onNavigateToTeams: () -> Unit,
  onNavigateToNotifications: () -> Unit,
  onNavigateToDeposit: () -> Unit,
  onLogout: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showEditProfileDialog by remember { mutableStateOf(false) }
  var showWithdrawDialog by remember { mutableStateOf(false) }
  var showWithdrawTimeAlert by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(GamingDarkBackground),
    contentPadding = PaddingValues(bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. HERO PROFILE CARD
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = GamingDarkSurface,
          border = BorderStroke(1.dp, GamingCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column {
            // Header banner
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(
                  Brush.horizontalGradient(listOf(Color(0xFF31102F), Color(0xFF141E30)))
                )
                .padding(12.dp),
              contentAlignment = Alignment.TopEnd
            ) {
              IconButton(
                onClick = { showEditProfileDialog = true },
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(GamingDarkSurface.copy(alpha = 0.8f))
              ) {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "প্রোফাইল এডিট",
                  tint = CyberOrange,
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            // Avatar & Info
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .offset(y = (-45).dp)
                  .size(76.dp)
                  .clip(CircleShape)
                  .background(Brush.linearGradient(listOf(CyberOrange, CyberRed)))
                  .border(3.dp, CyberCyan, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = currentUser.fullName.take(1).uppercase(),
                  color = Color.White,
                  fontWeight = FontWeight.Black,
                  fontSize = 32.sp
                )
              }

              Column(
                modifier = Modifier.offset(y = (-30).dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = currentUser.fullName,
                  color = TextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 19.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = "@${currentUser.username} (${currentUser.userCode})",
                    color = TextSecondary,
                    fontSize = 13.sp
                  )
                  OnlineStatusBadge(isOnline = currentUser.isOnline)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Cards
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GamingDarkSurfaceVariant,
                    modifier = Modifier.weight(1f)
                  ) {
                    Column(
                      modifier = Modifier.padding(10.dp),
                      horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                      Text(text = "ম্যাচ", color = TextSecondary, fontSize = 11.sp)
                      Text(text = "${currentUser.matchesPlayed}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                  }

                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GamingDarkSurfaceVariant,
                    modifier = Modifier.weight(1f)
                  ) {
                    Column(
                      modifier = Modifier.padding(10.dp),
                      horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                      Text(text = "বিজয়", color = CyberGreen, fontSize = 11.sp)
                      Text(text = "${currentUser.wins}", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                  }

                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GamingDarkSurfaceVariant,
                    modifier = Modifier.weight(1f)
                  ) {
                    Column(
                      modifier = Modifier.padding(10.dp),
                      horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                      Text(text = "মোট আয়", color = CyberOrange, fontSize = 11.sp)
                      Text(text = "৳${currentUser.totalEarnings.toInt()}", color = CyberOrange, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // 2. WALLET & FUNDS CARD
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = GamingDarkSurface,
          border = BorderStroke(1.dp, CyberOrange.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(text = "ব্যক্তিগত ওয়ালেট ব্যালেন্স", color = TextSecondary, fontSize = 12.sp)
                Text(
                  text = "৳${currentUser.walletBalance.toInt()} টাকা",
                  color = CyberGreen,
                  fontWeight = FontWeight.Black,
                  fontSize = 24.sp
                )
              }

              Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = CyberOrange,
                modifier = Modifier.size(36.dp)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = onNavigateToDeposit,
                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("deposit_button")
              ) {
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "ডিপোজিট করুন", fontWeight = FontWeight.Bold)
              }

              Button(
                onClick = {
                  if (isWithdrawAllowedTime) {
                    showWithdrawDialog = true
                  } else {
                    showWithdrawTimeAlert = true
                  }
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isWithdrawAllowedTime) CyberOrange else GamingDarkSurfaceVariant,
                  contentColor = if (isWithdrawAllowedTime) Color.Black else TextMuted
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("withdraw_button")
              ) {
                Icon(imageVector = Icons.Default.CurrencyExchange, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "টাকা উত্তোলন", fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "উইথড্র করার নির্ধারিত সময়: সন্ধ্যা ৭:০০ টা থেকে রাত ১১:০০ টা পর্যন্ত।",
              color = TextMuted,
              fontSize = 11.sp,
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    }

    // 3. FAST SHORTCUTS
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Text(text = "এক ক্লিকে মেনু", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
          shape = RoundedCornerShape(16.dp),
          color = GamingDarkSurface,
          border = BorderStroke(1.dp, GamingCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column {
            ShortcutRow(
              icon = Icons.Default.EmojiEvents,
              title = "আমার টুর্নামেন্ট",
              subtitle = "অংশগ্রহণ করা সব টুর্নামেন্টের তথ্য",
              iconTint = CyberOrange,
              onClick = onNavigateToTournaments
            )
            HorizontalDivider(color = GamingCardBorder.copy(alpha = 0.5f))
            ShortcutRow(
              icon = Icons.Default.SportsKabaddi,
              title = "আমার ১v১ চ্যালেঞ্জ",
              subtitle = "ওপেন ও চলমান চ্যালেঞ্জ বোর্ড",
              iconTint = CyberCyan,
              onClick = onNavigateToChallenges
            )
            HorizontalDivider(color = GamingCardBorder.copy(alpha = 0.5f))
            ShortcutRow(
              icon = Icons.Default.Groups,
              title = "আমার স্কোয়াড / টিম",
              subtitle = "টিম ফান্ড, মেম্বারস ও টিম চ্যালেঞ্জ",
              iconTint = CyberGreen,
              onClick = onNavigateToTeams
            )
            HorizontalDivider(color = GamingCardBorder.copy(alpha = 0.5f))
            ShortcutRow(
              icon = Icons.Default.Notifications,
              title = "নোটিফিকেশন সেন্টার",
              subtitle = "ম্যাচ ও ট্রানজেকশন আপডেট",
              iconTint = CyberGold,
              onClick = onNavigateToNotifications
            )
          }
        }
      }
    }

    // 4. SUPPORT & ACCOUNT ACTIONS
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = GamingDarkSurface,
          border = BorderStroke(1.dp, GamingCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column {
            ShortcutRow(
              icon = Icons.Default.Headphones,
              title = "২৪/৭ কাস্টমার সাপোর্ট",
              subtitle = "যেকোনো সমস্যায় সরাসরি মেসেজ করুন",
              iconTint = CyberCyan,
              onClick = { }
            )
            HorizontalDivider(color = GamingCardBorder.copy(alpha = 0.5f))
            ShortcutRow(
              icon = Icons.Default.Security,
              title = "নিরাপত্তা ও শর্তাবলী",
              subtitle = "খেলো বিডি ফেয়ার প্লে নীতিমালা",
              iconTint = CyberGreen,
              onClick = { }
            )
            HorizontalDivider(color = GamingCardBorder.copy(alpha = 0.5f))
            ShortcutRow(
              icon = Icons.Default.ExitToApp,
              title = "লগআউট (Logout)",
              subtitle = "একাউন্ট থেকে প্রস্থান করুন",
              iconTint = CyberRed,
              onClick = onLogout
            )
          }
        }
      }
    }

    // 5. RECENT TRANSACTIONS
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Text(text = "সাম্প্রতিক লেনদেন হিস্ট্রি", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(10.dp))

        if (transactions.isEmpty()) {
          Text(text = "এখনো কোনো লেনদেন হয়নি", color = TextSecondary, fontSize = 13.sp)
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            transactions.take(5).forEach { tx ->
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = GamingDarkSurface,
                border = BorderStroke(1.dp, GamingCardBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (tx.isCredit) CyberGreen.copy(alpha = 0.15f) else CyberRed.copy(alpha = 0.15f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = if (tx.isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (tx.isCredit) CyberGreen else CyberRed,
                        modifier = Modifier.size(16.dp)
                      )
                    }

                    Column {
                      Text(text = tx.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                      Text(text = "${tx.paymentMethod} • ${tx.timestamp}", color = TextSecondary, fontSize = 11.sp)
                    }
                  }

                  Text(
                    text = "${if (tx.isCredit) "+" else "-"}৳${tx.amount.toInt()}",
                    color = if (tx.isCredit) CyberGreen else CyberRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // Withdraw Time Alert Dialog
  if (showWithdrawTimeAlert) {
    BengaliConfirmDialog(
      title = "উইথড্র করার সময় নয়",
      message = "টাকা তোলার নির্ধারিত সময় সন্ধ্যা ৭:০০ টা থেকে রাত ১১:০০ টা পর্যন্ত। এই সময়ের বাইরে উইথড্র বন্ধ থাকবে। অনুগ্রহ করে সন্ধ্যা ৭:০০ টার পর আবার চেষ্টা করুন।",
      confirmButtonText = "ঠিক আছে",
      cancelButtonText = "বন্ধ",
      onConfirm = { showWithdrawTimeAlert = false },
      onDismiss = { showWithdrawTimeAlert = false }
    )
  }

  // Edit Profile Dialog
  if (showEditProfileDialog) {
    var name by remember { mutableStateOf(currentUser.fullName) }
    var phone by remember { mutableStateOf(currentUser.phone) }
    var ffUid by remember { mutableStateOf(currentUser.freeFireUid) }
    var pubgUid by remember { mutableStateOf(currentUser.pubgUid) }

    Dialog(onDismissRequest = { showEditProfileDialog = false }) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.padding(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "প্রোফাইল আপডেট করুন", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("পূর্ণ নাম") }, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("ফোন নম্বর") }, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(value = ffUid, onValueChange = { ffUid = it }, label = { Text("Free Fire UID") }, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(value = pubgUid, onValueChange = { pubgUid = it }, label = { Text("PUBG UID") }, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(16.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { showEditProfileDialog = false }, modifier = Modifier.weight(1f)) {
              Text(text = "বাতিল", color = TextSecondary)
            }
            Button(
              onClick = {
                onUpdateProfile(name, phone, ffUid, pubgUid)
                showEditProfileDialog = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = CyberOrange, contentColor = Color.Black),
              modifier = Modifier.weight(1f)
            ) {
              Text(text = "সেভ করুন", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  // Withdraw Dialog (7PM to 11PM)
  if (showWithdrawDialog) {
    var withdrawMethod by remember { mutableStateOf("bKash") }
    var withdrawPhone by remember { mutableStateOf(currentUser.phone) }
    var withdrawAmount by remember { mutableStateOf("200") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = { showWithdrawDialog = false }) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.padding(16.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "টাকা উত্তোলন করুন (উইথড্র)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Spacer(modifier = Modifier.height(6.dp))
          Text(text = "আপনার বর্তমান ব্যালেন্স: ৳${currentUser.walletBalance.toInt()} টাকা", color = CyberGreen, fontSize = 12.sp)

          Spacer(modifier = Modifier.height(10.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("bKash", "Nagad", "Rocket").forEach { m ->
              val isSel = withdrawMethod == m
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSel) CyberOrange else GamingDarkSurfaceVariant,
                modifier = Modifier
                  .weight(1f)
                  .clickable { withdrawMethod = m }
              ) {
                Text(
                  text = m,
                  color = if (isSel) Color.Black else TextSecondary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.padding(vertical = 6.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = withdrawPhone,
            onValueChange = { withdrawPhone = it },
            label = { Text("মোবাইল নম্বর") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = withdrawAmount,
            onValueChange = { withdrawAmount = it },
            label = { Text("উত্তোলনের পরিমাণ (মিনিমাম ১০০)") },
            modifier = Modifier.fillMaxWidth()
          )

          errorMsg?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = it, color = CyberRed, fontSize = 11.sp)
          }

          Spacer(modifier = Modifier.height(16.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { showWithdrawDialog = false }, modifier = Modifier.weight(1f)) {
              Text(text = "বাতিল", color = TextSecondary)
            }
            Button(
              onClick = {
                val amt = withdrawAmount.toDoubleOrNull() ?: 0.0
                if (amt < 100.0) {
                  errorMsg = "সর্বনিম্ন ১০০ টাকা তুলতে পারবেন!"
                } else if (currentUser.walletBalance < amt) {
                  errorMsg = "আপনার ওয়ালেটে পর্যাপ্ত ব্যালেন্স নেই!"
                } else if (withdrawPhone.length < 11) {
                  errorMsg = "সঠিক মোবাইল নম্বর দিন!"
                } else {
                  onRequestWithdraw(withdrawMethod, withdrawPhone, amt)
                  showWithdrawDialog = false
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = CyberOrange, contentColor = Color.Black),
              modifier = Modifier.weight(1f)
            ) {
              Text(text = "উত্তোলন করুন", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
fun ShortcutRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  iconTint: Color,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(iconTint.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
      }

      Column {
        Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
      }
    }

    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
  }
}
