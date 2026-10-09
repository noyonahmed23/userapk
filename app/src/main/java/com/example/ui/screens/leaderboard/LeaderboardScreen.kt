package com.example.ui.screens.leaderboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardItem
import com.example.data.model.Team
import com.example.ui.components.OnlineStatusBadge
import com.example.ui.theme.*

@Composable
fun LeaderboardScreen(
  topPlayers: List<LeaderboardItem>,
  topTeams: List<Team>,
  onPlayerClick: (String, String) -> Unit,
  onTeamClick: (Team) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Players, 1: Teams

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(GamingDarkBackground)
  ) {
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = GamingDarkSurface,
      contentColor = CyberGold,
      divider = { HorizontalDivider(color = GamingCardBorder) }
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(
              text = "টপ প্লেয়ার্স",
              fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
              fontSize = 13.sp,
              color = if (selectedTab == 0) CyberGold else TextSecondary
            )
          }
        }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(imageVector = Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(
              text = "টপ স্কোয়াড ও টিম",
              fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
              fontSize = 13.sp,
              color = if (selectedTab == 1) CyberCyan else TextSecondary
            )
          }
        }
      )
    }

    LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      if (selectedTab == 0) {
        // Top 3 Podium Cards
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
          ) {
            // #2 Rank
            topPlayers.getOrNull(1)?.let { p2 ->
              PodiumCard(
                player = p2,
                badgeColor = Color(0xFFC0C0C0),
                height = 130.dp,
                onClick = { onPlayerClick(p2.userId, p2.name) },
                modifier = Modifier.weight(1f)
              )
            }

            // #1 Rank
            topPlayers.getOrNull(0)?.let { p1 ->
              PodiumCard(
                player = p1,
                badgeColor = CyberGold,
                height = 150.dp,
                onClick = { onPlayerClick(p1.userId, p1.name) },
                modifier = Modifier.weight(1.1f)
              )
            }

            // #3 Rank
            topPlayers.getOrNull(2)?.let { p3 ->
              PodiumCard(
                player = p3,
                badgeColor = Color(0xFFCD7F32),
                height = 120.dp,
                onClick = { onPlayerClick(p3.userId, p3.name) },
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        // Remaining list
        items(topPlayers.drop(3)) { player ->
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = GamingDarkSurface,
            border = BorderStroke(1.dp, GamingCardBorder),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onPlayerClick(player.userId, player.name) }
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
                Text(
                  text = "#${player.rank}",
                  color = TextSecondary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )

                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(CyberCyan, CyberOrange))),
                  contentAlignment = Alignment.Center
                ) {
                  Text(text = player.name.take(1).uppercase(), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Column {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = player.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OnlineStatusBadge(isOnline = player.isOnline)
                  }
                  Text(text = "ম্যাচ: ${player.matches} • জয়: ${player.wins}", color = TextSecondary, fontSize = 11.sp)
                }
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(text = "${player.score} PTS", color = CyberOrangeGlow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "আয়: ৳${player.earnings.toInt()}", color = CyberGreen, fontSize = 11.sp)
              }
            }
          }
        }
      } else {
        // Teams Leaderboard
        items(topTeams) { team ->
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = GamingDarkSurface,
            border = BorderStroke(1.dp, GamingCardBorder),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onTeamClick(team) }
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(CyberOrange, CyberRed))),
                  contentAlignment = Alignment.Center
                ) {
                  Text(text = team.tag, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Column {
                  Text(text = team.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  Text(text = "${team.members.size} মেম্বার • জয়: ${team.wins} ম্যাচ", color = TextSecondary, fontSize = 11.sp)
                }
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = GamingDarkSurfaceVariant
              ) {
                Text(
                  text = "★ ${team.rating}",
                  color = CyberCyan,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun PodiumCard(
  player: LeaderboardItem,
  badgeColor: Color,
  height: androidx.compose.ui.unit.Dp,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = GamingDarkSurface,
    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f)),
    modifier = modifier
      .height(height)
      .clickable { onClick() }
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceAround
    ) {
      Box(
        modifier = Modifier
          .size(24.dp)
          .clip(CircleShape)
          .background(badgeColor),
        contentAlignment = Alignment.Center
      ) {
        Text(text = "${player.rank}", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
      }

      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(Brush.linearGradient(listOf(CyberOrange, CyberRed))),
        contentAlignment = Alignment.Center
      ) {
        Text(text = player.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = player.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
        Text(text = "${player.score} PTS", color = CyberOrangeGlow, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
      }
    }
  }
}
