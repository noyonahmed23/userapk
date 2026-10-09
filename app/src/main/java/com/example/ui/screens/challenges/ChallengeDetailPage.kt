package com.example.ui.screens.challenges

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChallengeStatus
import com.example.data.model.PlayerChallenge
import com.example.data.model.UserProfile
import com.example.ui.components.BengaliConfirmDialog
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeDetailPage(
  challenge: PlayerChallenge,
  currentUser: UserProfile,
  onBack: () -> Unit,
  onAcceptChallenge: (PlayerChallenge) -> Unit,
  onCancelChallenge: (PlayerChallenge) -> Unit,
  onSubmitRoom: (roomId: String, roomPass: String) -> Unit,
  onSubmitProof: (isChallenger: Boolean) -> Unit,
  onPlayerClick: (String, String) -> Unit,
  modifier: Modifier = Modifier
) {
  val clipboardManager = LocalClipboardManager.current
  var showCancelConfirmDialog by remember { mutableStateOf(false) }
  var showAcceptConfirmDialog by remember { mutableStateOf(false) }
  var inputRoomId by remember { mutableStateOf(challenge.roomId ?: "") }
  var inputRoomPass by remember { mutableStateOf(challenge.roomPassword ?: "") }
  var copyToast by remember { mutableStateOf(false) }

  val isChallenger = challenge.challengerId == currentUser.id
  val isOpponent = challenge.opponentId == currentUser.id
  val isParticipant = isChallenger || isOpponent

  // Live countdown calculation
  val remainingTimeMillis by produceState(initialValue = (challenge.expiresAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)) {
    while (value > 0) {
      delay(1000)
      value = (challenge.expiresAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
    }
  }

  val minutes = (remainingTimeMillis / 1000) / 60
  val seconds = (remainingTimeMillis / 1000) % 60

  Scaffold(
    topBar = {
      Surface(
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        shape = RoundedCornerShape(0.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "পিছনে যান", tint = TextPrimary)
          }
          Text(
            text = "১v১ চ্যালেঞ্জ বিবরণ",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        }
      }
    },
    bottomBar = {
      if (challenge.status == ChallengeStatus.OPEN && !isChallenger) {
        Surface(
          color = GamingDarkSurface,
          border = BorderStroke(1.dp, GamingCardBorder),
          shape = RoundedCornerShape(0.dp),
          modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
        ) {
          Button(
            onClick = { showAcceptConfirmDialog = true },
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp)
              .height(46.dp)
              .testTag("accept_challenge_bottom_btn")
          ) {
            Text(text = "ACCEPT CHALLENGE (৳${challenge.amount.toInt()})", fontWeight = FontWeight.Black, fontSize = 14.sp)
          }
        }
      }
    },
    containerColor = GamingDarkBackground,
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. TOP CODE TAG BADGE
      item {
        Surface(
          shape = RoundedCornerShape(0.dp),
          color = Color(0xFF142416),
          border = BorderStroke(1.dp, Color(0xFF6B8A29).copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF8BAA3A), modifier = Modifier.size(16.dp))
            Text(
              text = "PLAYER CHALLENGE • ${challenge.codeTag}",
              color = Color(0xFF9BBB42),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          }
        }
      }

      // 2. CHALLENGER VS OPPONENT BOX (Matching Image 4)
      item {
        Surface(
          shape = RoundedCornerShape(0.dp),
          color = Color(0xFF161224),
          border = BorderStroke(1.dp, Color(0xFF2C2448)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column {
            // Challenger top box
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F1E2E))
                .clickable { onPlayerClick(challenge.challengerId, challenge.challengerName) }
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(46.dp)
                  .clip(RoundedCornerShape(4.dp))
                  .background(CyberCyan)
                  .border(2.dp, CyberCyan, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = challenge.challengerName.take(1).uppercase(),
                  color = Color.Black,
                  fontWeight = FontWeight.Black,
                  fontSize = 20.sp
                )
              }

              Column {
                Text(
                  text = "CHALLENGER • ${if (challenge.challengerIsOnline) "ONLINE" else "OFFLINE"}",
                  color = TextSecondary,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = challenge.challengerName,
                  color = TextPrimary,
                  fontWeight = FontWeight.Black,
                  fontSize = 16.sp
                )
              }
            }

            // Center VS Badge
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              contentAlignment = Alignment.Center
            ) {
              Surface(
                shape = RoundedCornerShape(2.dp),
                color = Color(0xFFFFA500),
                modifier = Modifier.size(36.dp, 28.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(text = "VS", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
              }
            }

            // Opponent bottom box
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161026))
                .clickable {
                  challenge.opponentId?.let { opId ->
                    challenge.opponentName?.let { opName ->
                      onPlayerClick(opId, opName)
                    }
                  }
                }
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(46.dp)
                  .clip(RoundedCornerShape(4.dp))
                  .background(if (challenge.opponentName != null) CyberOrange else GamingDarkSurfaceVariant)
                  .border(1.dp, GamingCardBorder, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = challenge.opponentName?.take(1)?.uppercase() ?: "?",
                  color = if (challenge.opponentName != null) Color.Black else TextMuted,
                  fontWeight = FontWeight.Black,
                  fontSize = 20.sp
                )
              }

              Column {
                Text(
                  text = "OPPONENT / প্রতিপক্ষ",
                  color = TextSecondary,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = challenge.opponentName ?: "যে কোনো Player",
                  color = if (challenge.opponentName != null) TextPrimary else TextMuted,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp
                )
              }
            }
          }
        }
      }

      // 3. TITLE & FLOW EXPLANATION
      item {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "PLAYER 1V1 • VS",
            color = TextPrimary,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "প্রথমে Challenge Accept হবে → Room ID/Password যাবে → ১০ মিনিট পরে Proof upload খুলবে → ৩০ মিনিটের মধ্যে Screenshot দিতে হবে।",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp,
            modifier = Modifier.padding(horizontal = 10.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))

          // 5 Tags Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            TagBadge(text = "● ${challenge.status.labelBn}", bg = Color(0xFF1B2B1B), fg = CyberGreen, modifier = Modifier.weight(1f))
            TagBadge(text = if (challenge.challengerIsOnline) "ONLINE" else "OFFLINE", bg = GamingDarkSurfaceVariant, fg = TextSecondary, modifier = Modifier.weight(1f))
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            TagBadge(text = "${challenge.mode.uppercase()} 1V1", bg = GamingDarkSurfaceVariant, fg = TextPrimary, modifier = Modifier.weight(1f))
            TagBadge(text = challenge.mapName.uppercase(), bg = GamingDarkSurfaceVariant, fg = TextPrimary, modifier = Modifier.weight(1f))
            TagBadge(text = challenge.rule.displayNameEn.uppercase(), bg = GamingDarkSurfaceVariant, fg = CyberCyan, modifier = Modifier.weight(1f))
          }
        }
      }

      // 4. ACCEPT করার সময় (COUNTDOWN BOX)
      item {
        Surface(
          shape = RoundedCornerShape(0.dp),
          color = Color(0xFF141C10),
          border = BorderStroke(1.dp, Color(0xFF6B8A29).copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "ACCEPT করার সময়",
              color = Color(0xFF8BAA3A),
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = String.format("%02d:%02d", minutes, seconds),
              color = Color(0xFFA6CC44),
              fontSize = 36.sp,
              fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "১০ মিনিটের মধ্যে Accept না হলে challenge auto Finished হবে।",
              color = TextMuted,
              fontSize = 11.sp
            )
          }
        }
      }

      // 5. FINANCIAL BREAKDOWN CARDS
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          FinancialRowCard(label = "ENTRY EACH", amount = "৳${challenge.amount.toInt()}.00", subtext = "Held from each player")
          FinancialRowCard(label = "TOTAL POT", amount = "৳${(challenge.amount * 2).toInt()}.00", subtext = "Both stakes combined")
          FinancialRowCard(
            label = "বিজয়ী PAYOUT",
            amount = "৳${(challenge.amount * 2 * 0.8).toInt()}.00",
            subtext = "80% of total pot • Fee ৳${(challenge.amount * 2 * 0.2).toInt()}.00",
            amountColor = Color(0xFF6B8A29)
          )
        }
      }

      // 6. WAITING FOR OPPONENT / STATUS
      item {
        Surface(
          shape = RoundedCornerShape(0.dp),
          color = GamingDarkSurface,
          border = BorderStroke(1.dp, GamingCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = if (challenge.status == ChallengeStatus.OPEN) "WAITING FOR OPPONENT" else "MATCH IN PROGRESS",
              color = Color(0xFF8BAA3A),
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
            Text(
              text = if (challenge.status == ChallengeStatus.OPEN) "Challenge পাঠানো হয়েছে" else "ম্যাচ একসেপ্ট হয়েছে",
              color = TextPrimary,
              fontSize = 17.sp,
              fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
              shape = RoundedCornerShape(0.dp),
              color = Color(0xFF261E14),
              border = BorderStroke(1.dp, CyberOrange.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "⏰ Expires in ${minutes}m ${seconds}s",
                color = CyberOrangeGlow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = "Your ৳${challenge.amount.toInt()}.00 is held. The opponent needs to accept before the expiry time.",
              color = TextSecondary,
              fontSize = 12.sp,
              lineHeight = 16.sp
            )
          }
        }
      }

      // 7. ROOM CREDENTIALS SECTION (if accepted)
      if (challenge.status != ChallengeStatus.OPEN && challenge.status != ChallengeStatus.CANCELLED) {
        item {
          Surface(
            shape = RoundedCornerShape(0.dp),
            color = GamingDarkSurface,
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(text = "রুম ইনফরমেশন (Room Details)", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Spacer(modifier = Modifier.height(8.dp))

              if (challenge.roomId != null && challenge.roomPassword != null) {
                Surface(
                  shape = RoundedCornerShape(0.dp),
                  color = GamingDarkSurfaceVariant,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text(text = "Room ID: ${challenge.roomId}", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 15.sp)
                      Text(text = "Password: ${challenge.roomPassword}", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Button(
                      onClick = {
                        clipboardManager.setText(AnnotatedString("${challenge.roomId} pass: ${challenge.roomPassword}"))
                        copyToast = true
                      },
                      shape = RoundedCornerShape(0.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                    ) {
                      Text(text = "COPY", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                  }
                }
              } else if (isChallenger) {
                OutlinedTextField(
                  value = inputRoomId,
                  onValueChange = { inputRoomId = it },
                  label = { Text("রুম আইডি দিন") },
                  modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                  value = inputRoomPass,
                  onValueChange = { inputRoomPass = it },
                  label = { Text("রুম পাসওয়ার্ড দিন") },
                  modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                  onClick = { onSubmitRoom(inputRoomId, inputRoomPass) },
                  shape = RoundedCornerShape(0.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(text = "রুম ইনফো সাবমিট করুন", fontWeight = FontWeight.Bold)
                }
              } else {
                Text(text = "চ্যালেঞ্জার রুম তৈরি করছে। কিছুক্ষণ অপেক্ষা করুন...", color = TextSecondary, fontSize = 12.sp)
              }

              Spacer(modifier = Modifier.height(10.dp))

              Button(
                onClick = { onSubmitProof(isChallenger) },
                enabled = isParticipant,
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B8A29), contentColor = Color.White),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "স্ক্রিনশট প্রুফ সাবমিট করুন", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // 8. CANCEL MATCH SECTION (Matching Image 4)
      if (isChallenger && (challenge.status == ChallengeStatus.OPEN || challenge.status == ChallengeStatus.ACCEPTED)) {
        item {
          Surface(
            shape = RoundedCornerShape(0.dp),
            color = Color(0xFF281418),
            border = BorderStroke(1.dp, CyberRed.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "CANCEL MATCH",
                color = CyberRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )
              Text(
                text = "এই Challenge Cancel করবেন?",
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
              )

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = "Accept করার আগে cancel করলে কোনো penalty নেই। Accept করার পরে cancel করলে আপনার wallet থেকে ৳20 penalty কাটা হবে, তার মধ্যে ৳10 প্রতিপক্ষের wallet-এ যোগ হবে। দুই জনের বাকি stake refund হবে।",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
              )

              Spacer(modifier = Modifier.height(12.dp))

              OutlinedButton(
                onClick = { showCancelConfirmDialog = true },
                shape = RoundedCornerShape(0.dp),
                border = BorderStroke(1.dp, CyberRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberRed),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(42.dp)
                  .testTag("cancel_challenge_btn")
              ) {
                Icon(imageVector = Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Challenge Cancel করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
            }
          }
        }
      }

      // 9. FOOTNOTES & RULES
      item {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
          Text(text = "Prize rule", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          Text(text = "Example: ৳50 + ৳50 = ৳100 total pot → winner ৳80.", color = TextSecondary, fontSize = 11.sp)
          Spacer(modifier = Modifier.height(6.dp))
          Text(text = "Admin fee", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          Text(text = "20% of the total pot is retained as platform fee.", color = TextSecondary, fontSize = 11.sp)
        }
      }
    }
  }

  // Cancel Confirmation Dialog (popup only for warning alert)
  if (showCancelConfirmDialog) {
    BengaliConfirmDialog(
      title = "চ্যালেঞ্জ বাতিলের সতর্কতা",
      message = "আপনি কি নিশ্চিত এই ১v১ চ্যালেঞ্জটি বাতিল করতে চান? একসেপ্ট করার পূর্বে বাতিল করলে সম্পূর্ণ টাকা রিফান্ড পাবেন।",
      confirmButtonText = "হ্যাঁ, বাতিল করুন",
      cancelButtonText = "না, ফিরে যান",
      isDanger = true,
      onConfirm = {
        showCancelConfirmDialog = false
        onCancelChallenge(challenge)
        onBack()
      },
      onDismiss = { showCancelConfirmDialog = false }
    )
  }

  // Accept Confirmation Dialog (popup only for alert)
  if (showAcceptConfirmDialog) {
    BengaliConfirmDialog(
      title = "চ্যালেঞ্জ একসেপ্ট নিশ্চিতকরণ",
      message = "${challenge.challengerName}-এর ৳${challenge.amount.toInt()} টাকার চ্যালেঞ্জ একসেপ্ট করতে যাচ্ছেন। আপনার ওয়ালেট থেকে ৳${challenge.amount.toInt()} টাকা হোল্ড করা হবে। আপনি কি নিশ্চিত?",
      confirmButtonText = "হ্যাঁ, একসেপ্ট করুন",
      cancelButtonText = "বাতিল",
      onConfirm = {
        showAcceptConfirmDialog = false
        onAcceptChallenge(challenge)
      },
      onDismiss = { showAcceptConfirmDialog = false }
    )
  }
}

@Composable
fun TagBadge(text: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
  Surface(
    shape = RoundedCornerShape(0.dp),
    color = bg,
    border = BorderStroke(1.dp, GamingCardBorder),
    modifier = modifier
  ) {
    Text(
      text = text,
      color = fg,
      fontWeight = FontWeight.Bold,
      fontSize = 10.sp,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp)
    )
  }
}

@Composable
fun FinancialRowCard(label: String, amount: String, subtext: String, amountColor: Color = TextPrimary) {
  Surface(
    shape = RoundedCornerShape(0.dp),
    color = GamingDarkSurface,
    border = BorderStroke(1.dp, GamingCardBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(text = label, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
      Text(text = amount, color = amountColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
      Text(text = subtext, color = TextSecondary, fontSize = 11.sp)
    }
  }
}
