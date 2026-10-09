package com.example.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
  tournaments: List<Tournament>,
  challenges: List<PlayerChallenge>,
  gameCategories: List<GameCategory>,
  topPlayers: List<LeaderboardItem>,
  topTeams: List<Team>,
  banners: List<BannerSlide>,
  currentUserId: String,
  onNavigateToTournaments: () -> Unit,
  onNavigateToChallenges: () -> Unit,
  onNavigateToTeams: () -> Unit,
  onNavigateToLeaderboard: () -> Unit,
  onJoinTournamentClick: (Tournament) -> Unit,
  onAcceptChallengeClick: (PlayerChallenge) -> Unit,
  onCancelChallengeClick: (PlayerChallenge) -> Unit,
  onChallengeDetailClick: (PlayerChallenge) -> Unit,
  onPlayerClick: (String, String) -> Unit, // userId, name
  onTeamClick: (Team) -> Unit,
  onSelectGameCategory: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  // Filter tournaments: Finished tournaments MUST NOT appear on Home feed!
  val openTournaments = remember(tournaments) {
    tournaments.filter { it.status == TournamentStatus.JOIN_OPEN }
  }
  val liveTournaments = remember(tournaments) {
    tournaments.filter { it.status == TournamentStatus.LIVE }
  }
  val openChallenges = remember(challenges) {
    challenges.filter { it.status == ChallengeStatus.OPEN }
  }

  // Active banner index
  var activeBannerIndex by remember { mutableIntStateOf(0) }
  LaunchedEffect(banners.size) {
    if (banners.isNotEmpty()) {
      while (true) {
        delay(4000)
        activeBannerIndex = (activeBannerIndex + 1) % banners.size
      }
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(GamingDarkBackground),
    contentPadding = PaddingValues(bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // 1. BANNER SLIDER
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        val currentBanner = banners.getOrNull(activeBannerIndex) ?: banners.firstOrNull()
        if (currentBanner != null) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = GamingDarkSurface,
            border = BorderStroke(1.dp, CyberOrange.copy(alpha = 0.4f)),
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp)
              .clickable {
                when (currentBanner.targetDestination) {
                  "tournaments" -> onNavigateToTournaments()
                  "challenges" -> onNavigateToChallenges()
                  "teams" -> onNavigateToTeams()
                  else -> onNavigateToTournaments()
                }
              }
              .testTag("home_banner_card")
          ) {
            Box(modifier = Modifier.fillMaxSize()) {
              // Background esports image
              Image(
                painter = painterResource(id = R.drawable.img_esports_banner),
                contentDescription = currentBanner.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )

              // Dark gradient overlay
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(
                        Color.Black.copy(alpha = 0.35f),
                        Color.Black.copy(alpha = 0.85f)
                      )
                    )
                  )
              )

              Column(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
              ) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = CyberOrange.copy(alpha = 0.9f)
                ) {
                  Text(
                    text = "HOT EVENT",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }

                Column {
                  Text(
                    text = currentBanner.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = currentBanner.subtitle,
                    color = TextPrimary.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          }

          // Indicator dots
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
          ) {
            banners.forEachIndexed { index, _ ->
              Box(
                modifier = Modifier
                  .padding(horizontal = 3.dp)
                  .size(if (index == activeBannerIndex) 16.dp else 6.dp, 6.dp)
                  .clip(CircleShape)
                  .background(if (index == activeBannerIndex) CyberOrange else TextMuted.copy(alpha = 0.4f))
              )
            }
          }
        }
      }
    }

    // 2. GAME CATEGORIES (2-Column Mobile-Friendly Grid)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "গেম ক্যাটাগরি",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          )
          Text(
            text = "সব টুর্নামেন্ট",
            color = CyberOrange,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onNavigateToTournaments() }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2-column mobile friendly grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          gameCategories.chunked(2).forEach { rowCategories ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              rowCategories.forEach { category ->
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = GamingDarkSurface,
                  border = BorderStroke(1.dp, GamingCardBorder),
                  modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectGameCategory(category.id) }
                    .testTag("game_cat_${category.id}")
                ) {
                  Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GamingDarkSurfaceVariant),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(text = category.iconEmoji, fontSize = 20.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = category.name,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      Text(
                        text = "${category.modes.firstOrNull() ?: ""} • ${category.activeTournamentsCount} ম্যাচ",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // 3. JOIN OPEN TOURNAMENT (Green badge, Home feed never shows finished tournaments)
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(CyberGreen)
            )
            Text(
              text = "ওপেন টুর্নামেন্ট (স্লট বুকিং)",
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            )
          }

          Text(
            text = "সব দেখুন (${openTournaments.size})",
            color = CyberOrange,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onNavigateToTournaments() }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (openTournaments.isEmpty()) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = GamingDarkSurface,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp)
          ) {
            Text(
              text = "বর্তমানে কোনো ওপেন টুর্নামেন্ট নেই। খুব শীঘ্রই নতুন ম্যাচ শুরু হবে।",
              color = TextSecondary,
              fontSize = 13.sp,
              modifier = Modifier.padding(20.dp),
              textAlign = TextAlign.Center
            )
          }
        } else {
          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            items(openTournaments) { tournament ->
              HomeTournamentCard(
                tournament = tournament,
                onJoinClick = { onJoinTournamentClick(tournament) }
              )
            }
          }
        }
      }
    }

    // 4. LIVE TOURNAMENTS (Red badge, live matches)
    if (liveTournaments.isNotEmpty()) {
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(CyberRed)
            )
            Text(
              text = "লাইভ ম্যাচ (চলমান)",
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            liveTournaments.forEach { tournament ->
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = GamingDarkSurface,
                border = BorderStroke(1.dp, CyberRed.copy(alpha = 0.5f)),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { onJoinTournamentClick(tournament) }
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberRed.copy(alpha = 0.15f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = CyberRed
                      )
                    }

                    Column {
                      Text(
                        text = tournament.title,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      Text(
                        text = "${tournament.gameTitle} • ${tournament.mode} • ${tournament.mapName}",
                        color = TextSecondary,
                        fontSize = 11.sp
                      )
                    }
                  }

                  TournamentStatusBadge(status = TournamentStatus.LIVE)
                }
              }
            }
          }
        }
      }
    }

    // 5. PLAYER 1V1 CHALLENGE POSTS (Challenger image clickable, 10-min countdown, amount 50-200 BDT)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(CyberCyan)
            )
            Text(
              text = "১v১ প্লেয়ার চ্যালেঞ্জ পোস্ট",
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            )
          }

          Text(
            text = "সব দেখুন",
            color = CyberOrange,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onNavigateToChallenges() }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (openChallenges.isEmpty()) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = GamingDarkSurface,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "কোনো ওপেন চ্যালেঞ্জ নেই। আপনি নিজেই একটি চ্যালেঞ্জ পোস্ট করুন!",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(8.dp))
              Button(
                onClick = onNavigateToChallenges,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(text = "চ্যালেঞ্জ দিন", fontWeight = FontWeight.Bold)
              }
            }
          }
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            openChallenges.take(4).forEach { challenge ->
              HomeChallengeCard(
                challenge = challenge,
                isOwnPost = challenge.challengerId == currentUserId,
                onPlayerClick = { onPlayerClick(challenge.challengerId, challenge.challengerName) },
                onAcceptClick = { onAcceptChallengeClick(challenge) },
                onCancelClick = { onCancelChallengeClick(challenge) },
                onCardClick = { onChallengeDetailClick(challenge) }
              )
            }
          }
        }
      }
    }

    // 6. TOP PLAYERS (Leaderboard) - Clickable Profiles
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.EmojiEvents,
              contentDescription = null,
              tint = CyberGold,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "সেরা প্লেয়ার্স (লিডারবোর্ড)",
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            )
          }

          Text(
            text = "র‌্যাঙ্কিং দেখুন",
            color = CyberOrange,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onNavigateToLeaderboard() }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
          shape = RoundedCornerShape(16.dp),
          color = GamingDarkSurface,
          border = BorderStroke(1.dp, GamingCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            topPlayers.take(3).forEach { player ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { onPlayerClick(player.userId, player.name) }
                  .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  // Rank badge
                  Box(
                    modifier = Modifier
                      .size(26.dp)
                      .clip(CircleShape)
                      .background(
                        when (player.rank) {
                          1 -> CyberGold
                          2 -> Color(0xFFC0C0C0)
                          3 -> Color(0xFFCD7F32)
                          else -> GamingDarkSurfaceVariant
                        }
                      ),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "${player.rank}",
                      color = Color.Black,
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.sp
                    )
                  }

                  // Avatar
                  Box(
                    modifier = Modifier
                      .size(34.dp)
                      .clip(CircleShape)
                      .background(Brush.linearGradient(listOf(CyberCyan, CyberOrange))),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = player.name.take(1).uppercase(),
                      color = Color.Black,
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp
                    )
                  }

                  Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                      Text(
                        text = player.name,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                      )
                      OnlineStatusBadge(isOnline = player.isOnline)
                    }
                    Text(
                      text = "জয়: ${player.wins} • উইনরেট: ${player.winRate}",
                      color = TextSecondary,
                      fontSize = 11.sp
                    )
                  }
                }

                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = GamingDarkSurfaceVariant
                ) {
                  Text(
                    text = "${player.score} PTS",
                    color = CyberOrangeGlow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }
          }
        }
      }
    }

    // 7. TOP SQUADS / TOP TEAMS - Clickable Team Cards
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Groups,
              contentDescription = null,
              tint = CyberCyan,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "টপ স্কোয়াড ও টিম",
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            )
          }

          Text(
            text = "সব টিম দেখুন",
            color = CyberOrange,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onNavigateToTeams() }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          topTeams.take(2).forEach { team ->
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = GamingDarkSurface,
              border = BorderStroke(1.dp, GamingCardBorder),
              modifier = Modifier
                .weight(1f)
                .clickable { onTeamClick(team) }
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(34.dp)
                      .clip(CircleShape)
                      .background(Brush.linearGradient(listOf(CyberOrange, CyberRed))),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = team.tag,
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 10.sp
                    )
                  }

                  Column {
                    Text(
                      text = team.name,
                      color = TextPrimary,
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = "${team.members.size} মেম্বার",
                      color = TextSecondary,
                      fontSize = 11.sp
                    )
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(text = "রেটিং: ${team.rating}", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                  Text(text = "জয়: ${team.wins}", color = CyberGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun HomeTournamentCard(
  tournament: Tournament,
  onJoinClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val remainingSlots = tournament.maxSlots - tournament.joinedSlots

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = GamingDarkSurface,
    border = BorderStroke(1.dp, GamingCardBorder),
    modifier = modifier
      .width(260.dp)
      .testTag("tour_card_${tournament.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
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
        TournamentStatusBadge(status = tournament.status)
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = tournament.title,
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Text(
        text = "${tournament.mode} • ${tournament.mapName}",
        color = TextSecondary,
        fontSize = 12.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Prize and Fee
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(GamingDarkSurfaceVariant)
          .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(text = "এন্ট্রি ফি", color = TextSecondary, fontSize = 10.sp)
          Text(text = "৳${tournament.entryFee.toInt()}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
          Text(text = "প্রাইজপুল", color = TextSecondary, fontSize = 10.sp)
          Text(text = "৳${tournament.prizePool.toInt()}", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Slots progress
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
          .height(5.dp)
          .clip(RoundedCornerShape(3.dp))
      )

      Spacer(modifier = Modifier.height(12.dp))

      Button(
        onClick = onJoinClick,
        colors = ButtonDefaults.buttonColors(containerColor = CyberOrange, contentColor = Color.Black),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(38.dp)
          .testTag("join_btn_${tournament.id}")
      ) {
        Text(text = "জয়েন করুন ৳${tournament.entryFee.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }
    }
  }
}

@Composable
fun HomeChallengeCard(
  challenge: PlayerChallenge,
  isOwnPost: Boolean,
  onPlayerClick: () -> Unit,
  onAcceptClick: () -> Unit,
  onCancelClick: () -> Unit,
  onCardClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  // Live countdown timer calculation
  val remainingTimeMillis by produceState(initialValue = challenge.expiresAtMillis - System.currentTimeMillis()) {
    while (value > 0) {
      delay(1000)
      value = (challenge.expiresAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
    }
  }

  val minutes = (remainingTimeMillis / 1000) / 60
  val seconds = (remainingTimeMillis / 1000) % 60

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = GamingDarkSurface,
    border = BorderStroke(1.dp, GamingCardBorder),
    modifier = modifier
      .fillMaxWidth()
      .clickable { onCardClick() }
      .testTag("challenge_card_${challenge.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Challenger profile info (CLICKABLE)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.clickable { onPlayerClick() }
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(Brush.linearGradient(listOf(CyberCyan, CyberOrange))),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = challenge.challengerName.take(1).uppercase(),
              color = Color.Black,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }

          Column {
            Text(
              text = challenge.challengerName,
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
            OnlineStatusBadge(isOnline = challenge.challengerIsOnline)
          }
        }

        // 10-Minute Countdown Badge
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = CyberOrange.copy(alpha = 0.15f),
          border = BorderStroke(1.dp, CyberOrange.copy(alpha = 0.4f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Timer,
              contentDescription = null,
              tint = CyberOrange,
              modifier = Modifier.size(12.dp)
            )
            Text(
              text = String.format("%02d:%02d", minutes, seconds),
              color = CyberOrange,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Challenge Details Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(GamingDarkSurfaceVariant)
          .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(text = "গেম ও মোড", color = TextSecondary, fontSize = 10.sp)
          Text(text = "${challenge.game} • ${challenge.mode}", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
        Column {
          Text(text = "ম্যাপ ও নিয়ম", color = TextSecondary, fontSize = 10.sp)
          Text(text = "${challenge.mapName} • ${challenge.rule.displayNameBn}", color = CyberCyan, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
          Text(text = "চ্যালেঞ্জ স্টেক", color = TextSecondary, fontSize = 10.sp)
          Text(text = "৳${challenge.amount.toInt()}", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }

      if (challenge.challengerNote.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "\"${challenge.challengerNote}\"",
          color = TextSecondary,
          fontSize = 11.sp,
          fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        if (isOwnPost) {
          OutlinedButton(
            onClick = onCancelClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberRed),
            border = BorderStroke(1.dp, CyberRed.copy(alpha = 0.5f)),
            modifier = Modifier.height(34.dp)
          ) {
            Text(text = "ক্যান্সেল করুন (রিফান্ড)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        } else {
          Button(
            onClick = onAcceptClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
            modifier = Modifier
              .height(34.dp)
              .testTag("accept_btn_${challenge.id}")
          ) {
            Text(text = "চ্যালেঞ্জ একসেপ্ট করুন (৳${challenge.amount.toInt()})", fontWeight = FontWeight.Bold, fontSize = 11.sp)
          }
        }
      }
    }
  }
}
