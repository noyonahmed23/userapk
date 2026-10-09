package com.example.ui.screens.tournaments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Tournament
import com.example.data.model.TournamentStatus
import com.example.data.model.UserProfile
import com.example.ui.components.TournamentStatusBadge
import com.example.ui.theme.*

@Composable
fun TournamentsScreen(
  tournaments: List<Tournament>,
  currentUser: UserProfile,
  onJoinClick: (Tournament) -> Unit,
  onOpenTournamentDetail: (Tournament) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Open, 1: Live, 2: Finished
  var selectedGameFilter by remember { mutableStateOf("ALL") }

  val tabs = listOf("ওপেন টুর্নামেন্ট", "লাইভ ম্যাচ", "হিস্ট্রি / সমাপ্ত")

  val filteredTournaments = remember(tournaments, selectedTab, selectedGameFilter) {
    val statusFiltered = when (selectedTab) {
      0 -> tournaments.filter { it.status == TournamentStatus.JOIN_OPEN || it.status == TournamentStatus.JOIN_CLOSED }
      1 -> tournaments.filter { it.status == TournamentStatus.LIVE }
      else -> tournaments.filter { it.status == TournamentStatus.FINISHED }
    }
    if (selectedGameFilter == "ALL") statusFiltered else statusFiltered.filter { it.gameId == selectedGameFilter }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(GamingDarkBackground)
  ) {
    // Top Tabs
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = GamingDarkSurface,
      contentColor = CyberOrange,
      divider = { HorizontalDivider(color = GamingCardBorder) }
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = {
            Text(
              text = title,
              fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
              fontSize = 13.sp,
              color = if (selectedTab == index) CyberOrange else TextSecondary
            )
          }
        )
      }
    }

    // Game Filter Chips
    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      val filters = listOf(
        "ALL" to "সব গেম",
        "freefire" to "Free Fire",
        "pubg" to "PUBG Mobile",
        "dls" to "DLS",
        "efootball" to "eFootball"
      )
      items(filters) { (id, label) ->
        val isSelected = selectedGameFilter == id
        FilterChip(
          selected = isSelected,
          onClick = { selectedGameFilter = id },
          label = { Text(text = label, fontSize = 12.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CyberOrange,
            selectedLabelColor = Color.Black,
            containerColor = GamingDarkSurface,
            labelColor = TextSecondary
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) CyberOrange else GamingCardBorder
          )
        )
      }
    }

    if (filteredTournaments.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.SportsEsports,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(54.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "এই ক্যাটাগরিতে কোনো টুর্নামেন্ট পাওয়া যায়নি",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
          )
        }
      }
    } else {
      LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(filteredTournaments) { tournament ->
          TournamentFullCard(
            tournament = tournament,
            isJoined = tournament.joinedPlayerIds.contains(currentUser.id),
            onCardClick = { onOpenTournamentDetail(tournament) },
            onJoinClick = { onJoinClick(tournament) }
          )
        }
      }
    }
  }
}

@Composable
fun TournamentFullCard(
  tournament: Tournament,
  isJoined: Boolean,
  onCardClick: () -> Unit,
  onJoinClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val remainingSlots = tournament.maxSlots - tournament.joinedSlots

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = GamingDarkSurface,
    border = BorderStroke(1.dp, GamingCardBorder),
    modifier = modifier
      .fillMaxWidth()
      .clickable { onCardClick() }
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = GamingDarkSurfaceVariant
          ) {
            Text(
              text = tournament.gameTitle,
              color = CyberCyan,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          if (isJoined) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = CyberGreen.copy(alpha = 0.2f)
            ) {
              Text(
                text = "✓ জয়েন করেছেন",
                color = CyberGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        TournamentStatusBadge(status = tournament.status)
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = tournament.title,
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 2.dp)
      ) {
        Text(text = "মোড: ${tournament.mode}", color = TextSecondary, fontSize = 12.sp)
        Text(text = "•", color = TextSecondary, fontSize = 12.sp)
        Text(text = "ম্যাপ: ${tournament.mapName}", color = TextSecondary, fontSize = 12.sp)
        Text(text = "•", color = TextSecondary, fontSize = 12.sp)
        Text(text = tournament.startTime, color = CyberOrangeGlow, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Financials row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(GamingDarkSurfaceVariant)
          .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(text = "এন্ট্রি ফি", color = TextSecondary, fontSize = 11.sp)
          Text(text = "৳${tournament.entryFee.toInt()} টাকা", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(text = "সর্বোচ্চ স্লট", color = TextSecondary, fontSize = 11.sp)
          Text(text = "${tournament.maxSlots} প্লেয়ার", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
          Text(text = "মোট প্রাইজপুল", color = TextSecondary, fontSize = 11.sp)
          Text(text = "৳${tournament.prizePool.toInt()} টাকা", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Room info if joined
      if (isJoined && (tournament.status == TournamentStatus.JOIN_CLOSED || tournament.status == TournamentStatus.LIVE)) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = CyberCyan.copy(alpha = 0.1f),
          border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(text = "রুম আইডি: ${tournament.roomId ?: "শীঘ্রই আসবে"}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text(text = "পাসওয়ার্ড: ${tournament.roomPassword ?: "অপেক্ষা করুন"}", color = CyberCyan, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
            Text(text = "রুম ইনফো", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      // Slot progress
      if (tournament.status == TournamentStatus.JOIN_OPEN) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "বাকি আছে $remainingSlots স্লট", color = TextSecondary, fontSize = 11.sp)
          Text(text = "${tournament.joinedSlots}/${tournament.maxSlots}", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
          progress = { (tournament.joinedSlots.toFloat() / tournament.maxSlots.toFloat()).coerceIn(0f, 1f) },
          color = CyberOrange,
          trackColor = GamingDarkSurfaceVariant,
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
          onClick = onJoinClick,
          enabled = !isJoined && remainingSlots > 0,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isJoined) CyberGreen.copy(alpha = 0.4f) else CyberOrange,
            contentColor = Color.Black
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = if (isJoined) "অলরেডি জয়েন করা আছে" else "জয়েন করুন (৳${tournament.entryFee.toInt()})",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        }
      }
    }
  }
}

@Composable
fun TournamentDetailsDialog(
  tournament: Tournament,
  isJoined: Boolean,
  userBalance: Double,
  onDismiss: () -> Unit,
  onJoinConfirm: () -> Unit
) {
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
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "টুর্নামেন্ট বিবরণ",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
          TournamentStatusBadge(status = tournament.status)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = tournament.title,
          color = CyberOrange,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )

        Text(
          text = "${tournament.gameTitle} • ${tournament.mode} • ${tournament.mapName}",
          color = TextSecondary,
          fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = GamingDarkSurfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(text = "এন্ট্রি ফি:", color = TextSecondary, fontSize = 13.sp)
              Text(text = "৳${tournament.entryFee.toInt()} টাকা", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(text = "মোট প্রাইজপুল:", color = TextSecondary, fontSize = 13.sp)
              Text(text = "৳${tournament.prizePool.toInt()} টাকা", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(text = "শুরুর সময়:", color = TextSecondary, fontSize = 13.sp)
              Text(text = tournament.startTime, color = CyberOrangeGlow, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(text = "আপনার ওয়ালেট ব্যালেন্স:", color = TextSecondary, fontSize = 13.sp)
              Text(text = "৳${userBalance.toInt()} টাকা", color = if (userBalance >= tournament.entryFee) CyberGreen else CyberRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "নিয়মাবলী ও শর্তাবলী:",
          color = TextPrimary,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = tournament.rules,
          color = TextSecondary,
          fontSize = 12.sp,
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text(text = "ফিরে যান", color = TextSecondary)
          }

          if (tournament.status == TournamentStatus.JOIN_OPEN && !isJoined) {
            Button(
              onClick = onJoinConfirm,
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = CyberOrange, contentColor = Color.Black),
              modifier = Modifier.weight(1f)
            ) {
              Text(text = "নিশ্চিত করুন", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
