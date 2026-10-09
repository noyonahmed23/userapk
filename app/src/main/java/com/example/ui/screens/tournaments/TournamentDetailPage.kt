package com.example.ui.screens.tournaments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Tournament
import com.example.data.model.TournamentSlot
import com.example.data.model.TournamentStatus
import com.example.data.model.UserProfile
import com.example.ui.components.BengaliConfirmDialog
import com.example.ui.components.TournamentStatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailPage(
  tournament: Tournament,
  currentUser: UserProfile,
  onBack: () -> Unit,
  onJoinSlot: (tournamentId: String, slotNumber: Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val clipboardManager = LocalClipboardManager.current
  var selectedSlotNumber by remember {
    mutableStateOf(tournament.slots.firstOrNull { !it.isFilled }?.slotNumber ?: 1)
  }
  var showConfirmDialog by remember { mutableStateOf(false) }
  var copyToast by remember { mutableStateOf(false) }

  val isJoined = remember(tournament, currentUser.id) {
    tournament.joinedPlayerIds.contains(currentUser.id) || tournament.slots.any { it.playerId == currentUser.id }
  }
  val userBookedSlot = remember(tournament, currentUser.id) {
    tournament.slots.find { it.playerId == currentUser.id }?.slotNumber
  }

  val openSeatsCount = remember(tournament) {
    tournament.maxSlots - tournament.joinedSlots
  }

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
            .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBack) {
              Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "পিছনে যান", tint = TextPrimary)
            }
            Column {
              Text(
                text = tournament.title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "${tournament.mode} • ${tournament.matchType} • ${tournament.mapName}",
                color = TextSecondary,
                fontSize = 11.sp
              )
            }
          }

          TournamentStatusBadge(status = tournament.status)
        }
      }
    },
    bottomBar = {
      // Bottom Claim Seat Action Card
      Surface(
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "YOUR NEXT MATCH",
                color = CyberOrangeGlow,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )
              Text(
                text = if (isJoined) "SEAT CLAIMED: SLOT #${userBookedSlot ?: selectedSlotNumber}" else "CLAIM YOUR SEAT: SLOT #$selectedSlotNumber",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
              )
            }

            Text(
              text = "ফি: ৳${tournament.entryFee.toInt()}",
              color = CyberGreen,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          if (isJoined) {
            Surface(
              shape = RoundedCornerShape(2.dp),
              color = CyberGreen.copy(alpha = 0.15f),
              border = BorderStroke(1.dp, CyberGreen),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "আপনি সফলভাবে SLOT #${userBookedSlot ?: selectedSlotNumber} এ রেজিস্টার্ড হয়েছেন",
                  color = CyberGreen,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          } else {
            Button(
              onClick = { showConfirmDialog = true },
              shape = RoundedCornerShape(2.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6B8A29), // Match user screenshot olive green button
                contentColor = Color.White
              ),
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("claim_seat_btn")
            ) {
              Text(
                text = "CLAIM SEAT (SLOT #$selectedSlotNumber)",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
              )
            }
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
      contentPadding = PaddingValues(bottom = 20.dp)
    ) {
      // 1. BANNER & MATCH START TIME BAR
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Starts time banner bar
          Surface(
            color = Color(0xFF141A23),
            shape = RoundedCornerShape(0.dp),
            border = BorderStroke(1.dp, GamingCardBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(text = "STARTS SCHEDULE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                Text(text = tournament.startTime, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "MATCH STATUS", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                Text(
                  text = if (tournament.status == TournamentStatus.LIVE) "Starting now" else "Registration Open",
                  color = if (tournament.status == TournamentStatus.LIVE) CyberRed else CyberGreen,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Black
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(text = "REGION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Bangladesh time", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              }
            }
          }

          // Hero Banner
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(150.dp)
              .background(GamingDarkSurface)
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_esports_banner),
              contentDescription = tournament.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xDD0C1017))))
            )
          }
        }
      }

      // 2. 4-METRIC GRID (Prize Pool, Entry Fee, Per Kill, Open Seats)
      item {
        Surface(
          color = GamingDarkSurface,
          shape = RoundedCornerShape(0.dp),
          border = BorderStroke(1.dp, GamingCardBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              MetricItem(label = "PRIZE POOL", value = "৳${tournament.prizePool.toInt()}", valueColor = Color(0xFF6B8A29), modifier = Modifier.weight(1f))
              MetricItem(label = "ENTRY FEE", value = "৳${tournament.entryFee.toInt()}", valueColor = TextPrimary, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = GamingCardBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              MetricItem(label = "PER KILL", value = "৳${tournament.perKillPrize.toInt()}", valueColor = TextPrimary, modifier = Modifier.weight(1f))
              MetricItem(label = "OPEN SEATS", value = "$openSeatsCount / ${tournament.maxSlots}", valueColor = CyberGreen, modifier = Modifier.weight(1f))
            }
          }
        }
      }

      // 3. ROOM REVEAL DETAILS (if joined & match ready)
      if (isJoined && (tournament.status == TournamentStatus.LIVE || tournament.status == TournamentStatus.JOIN_CLOSED)) {
        item {
          Surface(
            shape = RoundedCornerShape(2.dp),
            color = Color(0xFF142416),
            border = BorderStroke(1.dp, CyberGreen),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(text = "রুম আইডি ও পাসওয়ার্ড", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "Room ID: ${tournament.roomId ?: "58522158"}", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 15.sp)
                Text(text = "Password: ${tournament.roomPassword ?: "121"}", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              }
              IconButton(
                onClick = {
                  clipboardManager.setText(AnnotatedString("${tournament.roomId ?: "58522158"} pass: ${tournament.roomPassword ?: "121"}"))
                  copyToast = true
                }
              ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "কপি করুন", tint = CyberGreen)
              }
            }
          }
        }
      }

      // 4. SEAT BOARD HEADER
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          Text(
            text = "SEAT BOARD",
            color = CyberOrangeGlow,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${tournament.mode} slots",
              color = TextPrimary,
              fontWeight = FontWeight.Black,
              fontSize = 18.sp
            )
            Text(
              text = "${tournament.maxSlots} slots • 1 player per slot",
              color = TextSecondary,
              fontSize = 11.sp
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Every player gets one slot. A 1v1 tournament uses two or more individual player slots. A filled seat shows the site username and small game UID.",
            color = TextMuted,
            fontSize = 11.sp,
            lineHeight = 16.sp
          )
        }
      }

      // 5. SEAT BOARD SLOTS LIST (Matches Image 2 exactly)
      val slotList = if (tournament.slots.isNotEmpty()) tournament.slots else {
        (1..tournament.maxSlots).map { num ->
          TournamentSlot(num, num <= tournament.joinedSlots)
        }
      }

      items(slotList) { slot ->
        SlotRowCard(
          slot = slot,
          isSelected = selectedSlotNumber == slot.slotNumber,
          isUserBooked = slot.playerId == currentUser.id,
          onClick = {
            if (!slot.isFilled && !isJoined) {
              selectedSlotNumber = slot.slotNumber
            }
          }
        )
      }

      // 6. BEFORE YOU QUEUE / MATCH RULES
      item {
        Surface(
          shape = RoundedCornerShape(0.dp),
          color = GamingDarkSurface,
          border = BorderStroke(1.dp, GamingCardBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "BEFORE YOU QUEUE",
              color = Color(0xFF6B8A29),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
            Text(
              text = "Match rules",
              color = TextPrimary,
              fontWeight = FontWeight.Black,
              fontSize = 18.sp
            )
            Text(
              text = "Match is live. Room details are visible to joined players only.",
              color = TextSecondary,
              fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Prize breakdown
            Text(text = "Prize breakdown", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
              shape = RoundedCornerShape(0.dp),
              color = GamingDarkSurfaceVariant,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PrizeRow(place = "Place #1", amount = "৳${tournament.prizePlace1.toInt()}.00")
                PrizeRow(place = "Place #2", amount = "৳${tournament.prizePlace2.toInt()}.00")
                PrizeRow(place = "Place #3", amount = "৳${tournament.prizePlace3.toInt()}.00")
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Numbered guidelines (01, 02, 03)
            RuleStepItem(
              stepNumber = "01",
              title = "Use the selected slot.",
              desc = "Your slot number is saved with your tournament entry."
            )
            Spacer(modifier = Modifier.height(10.dp))
            RuleStepItem(
              stepNumber = "02",
              title = "Player details stay visible in the seat.",
              desc = "Username, in-game name and small UID are shown after joining."
            )
            Spacer(modifier = Modifier.height(10.dp))
            RuleStepItem(
              stepNumber = "03",
              title = "Results update your wallet.",
              desc = "Published rewards are recorded in your transaction history."
            )
          }
        }
      }
    }
  }

  // Bengali Confirm Alert (as per user instruction: popup only for confirmation/alert)
  if (showConfirmDialog) {
    BengaliConfirmDialog(
      title = "স্লট বুকিং নিশ্চিতকরণ",
      message = "টুর্নামেন্ট: ${tournament.title}\nসিলেক্টেড সিট: SLOT #$selectedSlotNumber\nএন্ট্রি ফি: ৳${tournament.entryFee.toInt()} টাকা\n\nআপনার ওয়ালেট ব্যালেন্স থেকে ৳${tournament.entryFee.toInt()} টাকা কেটে নেওয়া হবে। আপনি কি এই সিটটি নিশ্চিত করতে চান?",
      confirmButtonText = "হ্যাঁ, বুক করুন",
      cancelButtonText = "বাতিল",
      onConfirm = {
        showConfirmDialog = false
        onJoinSlot(tournament.id, selectedSlotNumber)
      },
      onDismiss = { showConfirmDialog = false }
    )
  }
}

@Composable
fun MetricItem(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text(text = label, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
    Text(text = value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.Black)
  }
}

@Composable
fun SlotRowCard(
  slot: TournamentSlot,
  isSelected: Boolean,
  isUserBooked: Boolean,
  onClick: () -> Unit
) {
  val borderCol = when {
    isUserBooked -> CyberGreen
    isSelected -> CyberOrange
    slot.isFilled -> GamingCardBorder
    else -> Color(0xFF6B8A29).copy(alpha = 0.5f)
  }

  Surface(
    shape = RoundedCornerShape(0.dp),
    color = GamingDarkSurface,
    border = BorderStroke(1.dp, borderCol),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 4.dp)
      .clickable(enabled = !slot.isFilled) { onClick() }
      .testTag("slot_${slot.slotNumber}")
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "SLOT ${slot.slotNumber}",
          color = Color(0xFF6B8A29),
          fontWeight = FontWeight.Black,
          fontSize = 13.sp
        )

        Text(
          text = if (slot.isFilled) "1 / 1 FILLED" else "1 OPEN / 1",
          color = if (slot.isFilled) TextSecondary else Color(0xFF6B8A29),
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Inner seat box
      Surface(
        shape = RoundedCornerShape(0.dp),
        color = if (slot.isFilled) Color(0xFF161E28) else Color(0xFF141C10),
        border = BorderStroke(1.dp, if (slot.isFilled) GamingCardBorder else Color(0xFF6B8A29).copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(8.dp)) {
          if (slot.isFilled) {
            Text(
              text = slot.playerName ?: "Registered Player",
              color = if (isUserBooked) CyberGreen else TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
            Text(
              text = "UID: ${slot.playerUid ?: "548291048"}",
              color = TextSecondary,
              fontSize = 10.sp
            )
          } else {
            Text(
              text = if (isSelected) "SELECTED SEAT" else "OPEN SEAT",
              color = if (isSelected) CyberOrangeGlow else Color(0xFF8BAA3A),
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
            Text(
              text = "Select from join panel or click to claim",
              color = TextMuted,
              fontSize = 10.sp
            )
          }
        }
      }
    }
  }
}

@Composable
fun PrizeRow(place: String, amount: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = place, color = TextSecondary, fontSize = 12.sp)
    Text(text = amount, color = Color(0xFF6B8A29), fontWeight = FontWeight.Bold, fontSize = 12.sp)
  }
}

@Composable
fun RuleStepItem(stepNumber: String, title: String, desc: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.Top
  ) {
    Text(
      text = stepNumber,
      color = Color(0xFF6B8A29),
      fontWeight = FontWeight.Black,
      fontSize = 16.sp
    )
    Column {
      Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
      Text(text = desc, color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
    }
  }
}
