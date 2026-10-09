package com.example.ui.screens.challenges

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.OnlineStatusBadge
import com.example.ui.screens.home.HomeChallengeCard
import com.example.ui.theme.*

@Composable
fun UserChallengeScreen(
  challenges: List<PlayerChallenge>,
  currentUser: UserProfile,
  onCreateChallenge: (game: String, mode: String, map: String, rule: ChallengeRule, amount: Double, note: String) -> Unit,
  onAcceptChallenge: (PlayerChallenge) -> Unit,
  onCancelChallenge: (PlayerChallenge) -> Unit,
  onOpenChallengeDetail: (PlayerChallenge) -> Unit,
  onPlayerClick: (String, String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Open Board, 1: My Active & Accepted, 2: Finished
  var showCreateDialog by remember { mutableStateOf(false) }

  val openChallenges = remember(challenges) {
    challenges.filter { it.status == ChallengeStatus.OPEN }
  }
  val activeChallenges = remember(challenges, currentUser.id) {
    challenges.filter {
      (it.challengerId == currentUser.id || it.opponentId == currentUser.id) &&
        it.status != ChallengeStatus.OPEN && it.status != ChallengeStatus.FINISHED &&
        it.status != ChallengeStatus.CANCELLED && it.status != ChallengeStatus.EXPIRED
    }
  }
  val finishedChallenges = remember(challenges) {
    challenges.filter {
      it.status == ChallengeStatus.FINISHED || it.status == ChallengeStatus.CANCELLED || it.status == ChallengeStatus.EXPIRED
    }
  }

  Scaffold(
    containerColor = GamingDarkBackground,
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { showCreateDialog = true },
        containerColor = CyberOrange,
        contentColor = Color.Black,
        icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
        text = { Text(text = "নতুন চ্যালেঞ্জ দিন", fontWeight = FontWeight.Bold) },
        modifier = Modifier
          .padding(bottom = 60.dp)
          .testTag("create_challenge_fab")
      )
    },
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = GamingDarkSurface,
        contentColor = CyberCyan,
        divider = { HorizontalDivider(color = GamingCardBorder) }
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = {
            Text(
              text = "ওপেন বোর্ড (${openChallenges.size})",
              fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
              fontSize = 13.sp,
              color = if (selectedTab == 0) CyberCyan else TextSecondary
            )
          }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = {
            Text(
              text = "চলমান ম্যাচ (${activeChallenges.size})",
              fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
              fontSize = 13.sp,
              color = if (selectedTab == 1) CyberOrange else TextSecondary
            )
          }
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = {
            Text(
              text = "হিস্ট্রি (${finishedChallenges.size})",
              fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
              fontSize = 13.sp,
              color = if (selectedTab == 2) TextPrimary else TextSecondary
            )
          }
        )
      }

      val displayList = when (selectedTab) {
        0 -> openChallenges
        1 -> activeChallenges
        else -> finishedChallenges
      }

      if (displayList.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.SportsKabaddi,
              contentDescription = null,
              tint = TextMuted,
              modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = if (selectedTab == 0) "এখন কোনো ওপেন চ্যালেঞ্জ নেই। নিচে চাপ দিয়ে আপনি নতুন চ্যালেঞ্জ পোস্ট করতে পারেন!" else "কোনো ম্যাচ পাওয়া যায়নি।",
              color = TextSecondary,
              fontSize = 14.sp,
              textAlign = TextAlign.Center
            )
          }
        }
      } else {
        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(displayList) { challenge ->
            if (selectedTab == 0) {
              HomeChallengeCard(
                challenge = challenge,
                isOwnPost = challenge.challengerId == currentUser.id,
                onPlayerClick = { onPlayerClick(challenge.challengerId, challenge.challengerName) },
                onAcceptClick = { onAcceptChallenge(challenge) },
                onCancelClick = { onCancelChallenge(challenge) },
                onCardClick = { onOpenChallengeDetail(challenge) }
              )
            } else {
              ActiveChallengeCard(
                challenge = challenge,
                currentUserId = currentUser.id,
                onClick = { onOpenChallengeDetail(challenge) }
              )
            }
          }
        }
      }
    }
  }

  // Create Challenge Dialog
  if (showCreateDialog) {
    CreateChallengeDialog(
      userBalance = currentUser.walletBalance,
      onDismiss = { showCreateDialog = false },
      onSubmit = { game, mode, map, rule, amount, note ->
        showCreateDialog = false
        onCreateChallenge(game, mode, map, rule, amount, note)
      }
    )
  }
}

@Composable
fun ActiveChallengeCard(
  challenge: PlayerChallenge,
  currentUserId: String,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = GamingDarkSurface,
    border = BorderStroke(1.dp, if (challenge.status == ChallengeStatus.FINISHED) GamingCardBorder else CyberCyan.copy(alpha = 0.5f)),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "${challenge.challengerName} VS ${challenge.opponentName ?: "খুঁজছে..."}",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        }
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = when (challenge.status) {
            ChallengeStatus.ACCEPTED -> CyberOrange.copy(alpha = 0.2f)
            ChallengeStatus.ROOM_SUBMITTED -> CyberCyan.copy(alpha = 0.2f)
            ChallengeStatus.FINISHED -> CyberGreen.copy(alpha = 0.2f)
            else -> GamingDarkSurfaceVariant
          }
        ) {
          Text(
            text = challenge.status.labelBn,
            color = when (challenge.status) {
              ChallengeStatus.ACCEPTED -> CyberOrange
              ChallengeStatus.ROOM_SUBMITTED -> CyberCyan
              ChallengeStatus.FINISHED -> CyberGreen
              else -> TextSecondary
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "${challenge.game} • ${challenge.mode} • ${challenge.mapName} (${challenge.rule.displayNameBn})",
        color = TextSecondary,
        fontSize = 12.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "মোট পট: ৳${(challenge.amount * 2).toInt()} (স্টেক: ৳${challenge.amount.toInt()})", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(text = "বিস্তারিত দেখুন >", color = CyberOrange, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
      }
    }
  }
}

@Composable
fun CreateChallengeDialog(
  userBalance: Double,
  onDismiss: () -> Unit,
  onSubmit: (game: String, mode: String, map: String, rule: ChallengeRule, amount: Double, note: String) -> Unit
) {
  var selectedGame by remember { mutableStateOf("Free Fire") }
  var selectedMode by remember { mutableStateOf("Lone Wolf") }
  var selectedMap by remember { mutableStateOf("Iron Cage") }
  var selectedRule by remember { mutableStateOf(ChallengeRule.REGULAR) }
  var amountInput by remember { mutableStateOf("100") }
  var noteInput by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = GamingDarkSurface,
      border = BorderStroke(1.dp, GamingCardBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Text(
          text = "১ বনাম ১ চ্যালেঞ্জ পোস্ট করুন",
          color = TextPrimary,
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Game selector chips
        Text(text = "গেম নির্বাচন করুন:", color = TextSecondary, fontSize = 12.sp)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("Free Fire", "PUBG Mobile", "eFootball").forEach { game ->
            val isSelected = selectedGame == game
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) CyberOrange else GamingDarkSurfaceVariant,
              modifier = Modifier
                .clickable {
                  selectedGame = game
                  selectedMode = if (game == "Free Fire") "Lone Wolf" else if (game == "PUBG Mobile") "TDM Warehouse" else "1v1"
                }
            ) {
              Text(
                text = game,
                color = if (isSelected) Color.Black else TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Rule selector
        Text(text = "ম্যাচের নিয়ম:", color = TextSecondary, fontSize = 12.sp)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          ChallengeRule.values().forEach { rule ->
            val isSelected = selectedRule == rule
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) CyberCyan else GamingDarkSurfaceVariant,
              modifier = Modifier
                .weight(1f)
                .clickable { selectedRule = rule }
            ) {
              Text(
                text = rule.displayNameBn,
                color = if (isSelected) Color.Black else TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Amount Selection (50 to 200 BDT)
        Text(text = "চ্যালেঞ্জের পরিমাণ (৫০ থেকে ২০০ টাকা):", color = TextSecondary, fontSize = 12.sp)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("50", "100", "150", "200").forEach { preset ->
            val isSelected = amountInput == preset
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) CyberGreen else GamingDarkSurfaceVariant,
              modifier = Modifier
                .weight(1f)
                .clickable { amountInput = preset }
            ) {
              Text(
                text = "৳$preset",
                color = if (isSelected) Color.Black else TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 6.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = noteInput,
          onValueChange = { noteInput = it },
          label = { Text("নোট / শর্ত (যেমন: নো গ্রেনেড)") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyberOrange,
            unfocusedBorderColor = GamingCardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Bengali stake warning
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFF2E2412),
          border = BorderStroke(1.dp, CyberOrange.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "⚠️ সতর্কতা: পোস্ট করার সাথে সাথে আপনার ওয়ালেট থেকে ৳$amountInput টাকা হোল্ড করা হবে। ১০ মিনিটে একসেপ্ট না হলে বা বাতিল করলে টাকা সম্পূর্ণ ফেরত পাবেন।",
            color = CyberOrangeGlow,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(8.dp)
          )
        }

        errorMessage?.let {
          Spacer(modifier = Modifier.height(6.dp))
          Text(text = it, color = CyberRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text(text = "বাতিল", color = TextSecondary)
          }

          Button(
            onClick = {
              val amt = amountInput.toDoubleOrNull() ?: 0.0
              if (amt < 50.0 || amt > 200.0) {
                errorMessage = "পরিমাণ ৫০ থেকে ২০০ টাকার মধ্যে হতে হবে!"
              } else if (userBalance < amt) {
                errorMessage = "আপনার ওয়ালেটে পর্যাপ্ত টাকা নেই! ব্যালেন্স: ৳${userBalance.toInt()}"
              } else {
                onSubmit(selectedGame, selectedMode, selectedMap, selectedRule, amt, noteInput)
              }
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyberOrange, contentColor = Color.Black),
            modifier = Modifier.weight(1f)
          ) {
            Text(text = "পোস্ট করুন", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun ChallengeDetailDialog(
  challenge: PlayerChallenge,
  currentUserId: String,
  isAdmin: Boolean,
  onDismiss: () -> Unit,
  onAccept: () -> Unit,
  onSubmitRoom: (roomId: String, roomPass: String) -> Unit,
  onSubmitProof: (isChallenger: Boolean) -> Unit,
  onSettle: (winnerId: String) -> Unit,
  onPlayerClick: (String, String) -> Unit
) {
  val clipboardManager = LocalClipboardManager.current
  var inputRoomId by remember { mutableStateOf(challenge.roomId ?: "") }
  var inputRoomPass by remember { mutableStateOf(challenge.roomPassword ?: "") }
  var copyFeedback by remember { mutableStateOf(false) }

  val isChallenger = challenge.challengerId == currentUserId
  val isOpponent = challenge.opponentId == currentUserId
  val isParticipant = isChallenger || isOpponent

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = GamingDarkSurface,
      border = BorderStroke(1.dp, GamingCardBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        // VS Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Player 1
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .clickable { onPlayerClick(challenge.challengerId, challenge.challengerName) }
              .weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(CyberCyan, CyberOrange))),
              contentAlignment = Alignment.Center
            ) {
              Text(text = challenge.challengerName.take(1).uppercase(), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = challenge.challengerName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            OnlineStatusBadge(isOnline = challenge.challengerIsOnline)
          }

          // VS badge
          Surface(
            shape = CircleShape,
            color = CyberRed.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, CyberRed),
            modifier = Modifier.size(34.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(text = "VS", color = CyberRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
          }

          // Player 2
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .clickable {
                challenge.opponentId?.let { opId ->
                  challenge.opponentName?.let { opName ->
                    onPlayerClick(opId, opName)
                  }
                }
              }
              .weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (challenge.opponentName != null) Brush.linearGradient(listOf(CyberOrange, CyberGreen)) else SolidColor(GamingDarkSurfaceVariant)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = challenge.opponentName?.take(1)?.uppercase() ?: "?",
                color = if (challenge.opponentName != null) Color.Black else TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = challenge.opponentName ?: "প্রতিপক্ষ অপেক্ষা...",
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            OnlineStatusBadge(isOnline = challenge.opponentIsOnline ?: false)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Match Info Summary
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = GamingDarkSurfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(text = "গেম ও মোড:", color = TextSecondary, fontSize = 12.sp)
              Text(text = "${challenge.game} (${challenge.mode})", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(text = "নিয়ম ও ম্যাপ:", color = TextSecondary, fontSize = 12.sp)
              Text(text = "${challenge.rule.displayNameBn} • ${challenge.mapName}", color = CyberCyan, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(text = "চ্যালেঞ্জ স্টেক:", color = TextSecondary, fontSize = 12.sp)
              Text(text = "৳${challenge.amount.toInt()} টাকা (মোট পট: ৳${(challenge.amount * 2).toInt()})", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ROOM SECTION
        if (challenge.status != ChallengeStatus.OPEN && challenge.status != ChallengeStatus.CANCELLED) {
          Text(text = "রুম ইনফরমেশন:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          Spacer(modifier = Modifier.height(6.dp))

          if (challenge.roomId != null && challenge.roomPassword != null) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = GamingDarkSurfaceVariant,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(text = "রুম আইডি: ${challenge.roomId}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  Text(text = "পাসওয়ার্ড: ${challenge.roomPassword}", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                IconButton(
                  onClick = {
                    clipboardManager.setText(AnnotatedString("${challenge.roomId} pass: ${challenge.roomPassword}"))
                    copyFeedback = true
                  }
                ) {
                  Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "কপি করুন", tint = CyberOrange)
                }
              }
            }
            if (copyFeedback) {
              Text(text = "✓ ক্লিপবোর্ডে কপি করা হয়েছে!", color = CyberGreen, fontSize = 11.sp)
            }
          } else if (isChallenger) {
            // Challenger input room ID & pass
            OutlinedTextField(
              value = inputRoomId,
              onValueChange = { inputRoomId = it },
              label = { Text("রুম আইডি লিখুন") },
              colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
              value = inputRoomPass,
              onValueChange = { inputRoomPass = it },
              label = { Text("রুম পাসওয়ার্ড লিখুন") },
              colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Button(
              onClick = { onSubmitRoom(inputRoomId, inputRoomPass) },
              colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(text = "রুম ইনফো সাবমিট করুন", fontWeight = FontWeight.Bold)
            }
          } else {
            Text(text = "চ্যালেঞ্জার এখনো রুম প্রস্তুত করেনি। অনুগ্রহ করে অপেক্ষা করুন...", color = TextSecondary, fontSize = 12.sp)
          }

          Spacer(modifier = Modifier.height(10.dp))

          // PROOF SECTION
          Text(text = "স্ক্রিনশট প্রুফ সাবমিট:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { onSubmitProof(isChallenger) },
              enabled = isParticipant && challenge.status != ChallengeStatus.FINISHED,
              colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "প্রুফ আপলোড", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        // ADMIN SETTLEMENT (or simulation)
        if (isAdmin || challenge.status == ChallengeStatus.PROOF_SUBMITTED) {
          Spacer(modifier = Modifier.height(12.dp))
          Text(text = "ফলাফল নিষ্পত্তি (এডমিন):", color = CyberOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { onSettle(challenge.challengerId) },
              colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text(text = "${challenge.challengerName} জয়ী", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            challenge.opponentId?.let { opId ->
              Button(
                onClick = { onSettle(opId) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text(text = "${challenge.opponentName} জয়ী", fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
            Text(text = "বন্ধ করুন", color = TextSecondary)
          }
        }
      }
    }
  }
}
