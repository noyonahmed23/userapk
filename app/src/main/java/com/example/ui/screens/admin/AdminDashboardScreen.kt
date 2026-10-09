package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.TournamentStatusBadge
import com.example.ui.theme.*

@Composable
fun AdminDashboardScreen(
  tournaments: List<Tournament>,
  challenges: List<PlayerChallenge>,
  onCreateTournament: (gameId: String, title: String, mode: String, map: String, fee: Double, prize: Double, slots: Int, time: String, rules: String) -> Unit,
  onUpdateTournamentStatus: (tourId: String, status: TournamentStatus, roomId: String?, roomPass: String?) -> Unit,
  onSettleChallenge: (challengeId: String, winnerId: String) -> Unit,
  onExitAdminMode: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Tournaments, 1: Challenges
  var showCreateTourDialog by remember { mutableStateOf(false) }
  var tournamentToUpdate by remember { mutableStateOf<Tournament?>(null) }

  Scaffold(
    containerColor = GamingDarkBackground,
    topBar = {
      Surface(
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, CyberRed.copy(alpha = 0.3f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = CyberRed)
            Text(text = "এডমিন কন্ট্রোল প্যানেল", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }

          Button(
            onClick = onExitAdminMode,
            colors = ButtonDefaults.buttonColors(containerColor = GamingDarkSurfaceVariant, contentColor = TextPrimary),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
          ) {
            Text(text = "ইউজার ভিউ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    },
    floatingActionButton = {
      if (selectedTab == 0) {
        ExtendedFloatingActionButton(
          onClick = { showCreateTourDialog = true },
          containerColor = CyberRed,
          contentColor = Color.White,
          icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
          text = { Text(text = "নতুন টুর্নামেন্ট", fontWeight = FontWeight.Bold) },
          modifier = Modifier
            .padding(bottom = 60.dp)
            .testTag("admin_add_tournament_fab")
        )
      }
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
        contentColor = CyberRed,
        divider = { HorizontalDivider(color = GamingCardBorder) }
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = {
            Text(
              text = "টুর্নামেন্ট নিয়ন্ত্রণ (${tournaments.size})",
              fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
              fontSize = 13.sp,
              color = if (selectedTab == 0) CyberRed else TextSecondary
            )
          }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = {
            Text(
              text = "১v১ চ্যালেঞ্জ সেটেলমেন্ট (${challenges.size})",
              fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
              fontSize = 13.sp,
              color = if (selectedTab == 1) CyberOrange else TextSecondary
            )
          }
        )
      }

      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        if (selectedTab == 0) {
          items(tournaments) { tour ->
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = GamingDarkSurface,
              border = BorderStroke(1.dp, GamingCardBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(text = tour.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                  TournamentStatusBadge(status = tour.status)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${tour.gameTitle} • ${tour.mode} • স্লট: ${tour.joinedSlots}/${tour.maxSlots}", color = TextSecondary, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Button(
                    onClick = { tournamentToUpdate = tour },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                  ) {
                    Text(text = "স্ট্যাটাস ও রুম সেট", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }

                  if (tour.status != TournamentStatus.FINISHED) {
                    Button(
                      onClick = {
                        onUpdateTournamentStatus(tour.id, TournamentStatus.FINISHED, tour.roomId, tour.roomPassword)
                      },
                      colors = ButtonDefaults.buttonColors(containerColor = CyberOrange, contentColor = Color.Black),
                      shape = RoundedCornerShape(8.dp),
                      modifier = Modifier.weight(1f)
                    ) {
                      Text(text = "ম্যাচ ফিনিশ করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            }
          }
        } else {
          // Challenges settlement
          items(challenges) { challenge ->
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = GamingDarkSurface,
              border = BorderStroke(1.dp, GamingCardBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "${challenge.challengerName} VS ${challenge.opponentName ?: "খালি"}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                  Text(text = challenge.status.labelBn, color = CyberOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${challenge.game} • স্টেক: ৳${challenge.amount.toInt()} (পট: ৳${(challenge.amount * 2).toInt()})", color = TextSecondary, fontSize = 12.sp)

                if (challenge.status != ChallengeStatus.FINISHED && challenge.status != ChallengeStatus.CANCELLED && challenge.opponentId != null) {
                  Spacer(modifier = Modifier.height(10.dp))
                  Text(text = "বিজয়ী নির্বাচন করুন (২০% প্ল্যাটফর্ম ফি কর্তন হবে):", color = CyberGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  Spacer(modifier = Modifier.height(6.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Button(
                      onClick = { onSettleChallenge(challenge.id, challenge.challengerId) },
                      colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                      shape = RoundedCornerShape(8.dp),
                      modifier = Modifier.weight(1f)
                    ) {
                      Text(text = "${challenge.challengerName} জয়ী", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                      onClick = { onSettleChallenge(challenge.id, challenge.opponentId) },
                      colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
                      shape = RoundedCornerShape(8.dp),
                      modifier = Modifier.weight(1f)
                    ) {
                      Text(text = "${challenge.opponentName} জয়ী", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Create Tournament Dialog
  if (showCreateTourDialog) {
    var gameId by remember { mutableStateOf("freefire") }
    var title by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("Solo BR") }
    var mapName by remember { mutableStateOf("Bermuda") }
    var entryFee by remember { mutableStateOf("50") }
    var prizePool by remember { mutableStateOf("2000") }
    var maxSlots by remember { mutableStateOf("48") }
    var startTime by remember { mutableStateOf("আজ রাত ৯:০০ টা") }
    var rules by remember { mutableStateOf("১. কোনো হ্যাক বা স্ক্রিপ্ট ব্যবহার করা যাবে না।\n২. ম্যাচ শুরুর ১৫ মিনিট আগে রুম কোড দেওয়া হবে।") }

    Dialog(onDismissRequest = { showCreateTourDialog = false }) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.padding(14.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "নতুন টুর্নামেন্ট তৈরি করুন", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("টুর্নামেন্ট শিরোনাম") }, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(value = mode, onValueChange = { mode = it }, label = { Text("গেম মোড (যেমন: Solo BR)") }, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(6.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = entryFee, onValueChange = { entryFee = it }, label = { Text("ফি (টাকা)") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = prizePool, onValueChange = { prizePool = it }, label = { Text("প্রাইজপুল") }, modifier = Modifier.weight(1f))
          }
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("শুরুর সময়") }, modifier = Modifier.fillMaxWidth())

          Spacer(modifier = Modifier.height(14.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { showCreateTourDialog = false }, modifier = Modifier.weight(1f)) {
              Text(text = "বাতিল", color = TextSecondary)
            }
            Button(
              onClick = {
                val fee = entryFee.toDoubleOrNull() ?: 50.0
                val prize = prizePool.toDoubleOrNull() ?: 2000.0
                val slots = maxSlots.toIntOrNull() ?: 48
                onCreateTournament(gameId, title.ifBlank { "নতুন টুর্নামেন্ট" }, mode, mapName, fee, prize, slots, startTime, rules)
                showCreateTourDialog = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = CyberRed, contentColor = Color.White),
              modifier = Modifier.weight(1f)
            ) {
              Text(text = "পাবলিশ করুন", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  // Update Status & Room info Dialog
  tournamentToUpdate?.let { tour ->
    var currentStatus by remember { mutableStateOf(tour.status) }
    var roomIdInput by remember { mutableStateOf(tour.roomId ?: "") }
    var roomPassInput by remember { mutableStateOf(tour.roomPassword ?: "") }

    Dialog(onDismissRequest = { tournamentToUpdate = null }) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.padding(14.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "টুর্নামেন্ট স্ট্যাটাস ও রুম আইডি", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Spacer(modifier = Modifier.height(10.dp))

          Text(text = "স্ট্যাটাস নির্বাচন:", color = TextSecondary, fontSize = 12.sp)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TournamentStatus.values().forEach { st ->
              val isSel = currentStatus == st
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSel) CyberOrange else GamingDarkSurfaceVariant,
                modifier = Modifier
                  .weight(1f)
                  .clickable { currentStatus = st }
              ) {
                Text(
                  text = st.name.take(4),
                  color = if (isSel) Color.Black else TextSecondary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(vertical = 6.dp),
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(value = roomIdInput, onValueChange = { roomIdInput = it }, label = { Text("রুম আইডি") }, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(value = roomPassInput, onValueChange = { roomPassInput = it }, label = { Text("রুম পাসওয়ার্ড") }, modifier = Modifier.fillMaxWidth())

          Spacer(modifier = Modifier.height(16.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { tournamentToUpdate = null }, modifier = Modifier.weight(1f)) {
              Text(text = "বাতিল", color = TextSecondary)
            }
            Button(
              onClick = {
                onUpdateTournamentStatus(tour.id, currentStatus, roomIdInput.ifBlank { null }, roomPassInput.ifBlank { null })
                tournamentToUpdate = null
              },
              colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
              modifier = Modifier.weight(1f)
            ) {
              Text(text = "আপডেট করুন", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
