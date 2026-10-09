package com.example.ui.screens.teams

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
import com.example.ui.components.BengaliConfirmDialog
import com.example.ui.components.OnlineStatusBadge
import com.example.ui.theme.*

@Composable
fun TeamsScreen(
  teams: List<Team>,
  teamChallenges: List<TeamChallenge>,
  currentUser: UserProfile,
  isWithdrawAllowedTime: Boolean,
  onCreateTeam: (name: String, tag: String, slogan: String, game: String) -> Unit,
  onSendJoinRequest: (teamId: String) -> Unit,
  onAcceptJoinRequest: (teamId: String, reqId: String) -> Unit,
  onRejectJoinRequest: (teamId: String, reqId: String) -> Unit,
  onJoinByCode: (code: String) -> Unit,
  onInvitePlayer: (teamId: String, playerId: String, playerName: String) -> Unit,
  onRemoveMember: (teamId: String, memberId: String) -> Unit,
  onUpdateMemberRole: (teamId: String, memberId: String, newRole: String) -> Unit,
  onTeamDeposit: (teamId: String, amount: Double) -> Unit,
  onTeamWithdraw: (teamId: String, amount: Double) -> Unit,
  onSendTeamChallenge: (challengerTeamId: String, opponentTeamId: String, game: String, amount: Double) -> Unit,
  onPlayerClick: (String, String) -> Unit,
  onNavigateToDeposit: () -> Unit,
  modifier: Modifier = Modifier
) {
  val clipboardManager = LocalClipboardManager.current

  // Find user's own team (where user is member or captain)
  val myTeam = remember(teams, currentUser.id) {
    teams.find { team -> team.members.any { it.id == currentUser.id } }
  }

  // Active Tab state:
  // If user has a team -> Default tab 0 ("আমার টিম / Your Team"), tab 1 ("অন্যান্য টিম / Browse"), tab 2 ("চ্যালেঞ্জ / Challenges")
  // If user has NO team -> Default tab 0 ("টিম খুঁজুন ও জয়েন / Browse"), tab 1 ("টিম তৈরি / Create")
  var selectedTab by remember(myTeam != null) {
    mutableIntStateOf(0)
  }

  var showCreateTeamDialog by remember { mutableStateOf(false) }
  var showJoinByIdDialog by remember { mutableStateOf(false) }
  var showInvitePlayerDialog by remember { mutableStateOf(false) }
  var showDepositDialog by remember { mutableStateOf(false) }
  var showWithdrawDialog by remember { mutableStateOf(false) }
  var showSendChallengeDialog by remember { mutableStateOf(false) }
  var showWithdrawTimeWarning by remember { mutableStateOf(false) }
  var memberToRemove by remember { mutableStateOf<TeamMember?>(null) }
  var memberToRoleChange by remember { mutableStateOf<TeamMember?>(null) }
  var showTeamIdCopiedToast by remember { mutableStateOf(false) }

  Scaffold(
    containerColor = GamingDarkBackground,
    floatingActionButton = {
      if (myTeam == null) {
        ExtendedFloatingActionButton(
          onClick = { showCreateTeamDialog = true },
          containerColor = CyberOrange,
          contentColor = Color.Black,
          icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
          text = { Text("নতুন টিম তৈরি করুন", fontWeight = FontWeight.Bold) },
          modifier = Modifier.padding(bottom = 60.dp).testTag("create_team_fab")
        )
      }
    },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // 1. TOP TAB NAVIGATION
      if (myTeam != null) {
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
                text = "আমার টিম (${myTeam.tag})",
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
                text = "অন্যান্য টিম (${teams.size - 1})",
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
                text = "টিম চ্যালেঞ্জ (${teamChallenges.size})",
                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp,
                color = if (selectedTab == 2) CyberGreen else TextSecondary
              )
            }
          )
        }
      } else {
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
                text = "টিম খুঁজুন ও জয়েন (${teams.size})",
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
                text = "নতুন টিম তৈরি",
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp,
                color = if (selectedTab == 1) CyberOrange else TextSecondary
              )
            }
          )
        }
      }

      // 2. CONTENT AREA
      if (myTeam != null) {
        when (selectedTab) {
          0 -> {
            // YOUR TEAM PAGE (COMPLETE PROFILE, ADMIN CONTROLS, INVITATION, ETC.)
            YourTeamView(
              team = myTeam,
              currentUser = currentUser,
              isWithdrawAllowedTime = isWithdrawAllowedTime,
              onCopyTeamId = {
                clipboardManager.setText(AnnotatedString(myTeam.publicId))
                showTeamIdCopiedToast = true
              },
              onAddMemberClick = { showInvitePlayerDialog = true },
              onDepositClick = onNavigateToDeposit,
              onWithdrawClick = {
                if (isWithdrawAllowedTime) showWithdrawDialog = true else showWithdrawTimeWarning = true
              },
              onChallengeClick = { showSendChallengeDialog = true },
              onPlayerClick = onPlayerClick,
              onAcceptJoinReq = { reqId -> onAcceptJoinRequest(myTeam.id, reqId) },
              onRejectJoinReq = { reqId -> onRejectJoinRequest(myTeam.id, reqId) },
              onRemoveMemberClick = { member -> memberToRemove = member },
              onChangeRoleClick = { member -> memberToRoleChange = member }
            )
          }
          1 -> {
            // BROWSE OTHER TEAMS
            BrowseTeamsView(
              teams = teams.filter { it.id != myTeam.id },
              currentUser = currentUser,
              onJoinRequest = onSendJoinRequest,
              onJoinByCodeClick = { showJoinByIdDialog = true },
              onChallengeTeamClick = { showSendChallengeDialog = true }
            )
          }
          else -> {
            // TEAM CHALLENGES VIEW
            TeamChallengesView(
              challenges = teamChallenges,
              myTeamId = myTeam.id,
              onSendChallengeClick = { showSendChallengeDialog = true }
            )
          }
        }
      } else {
        // NO TEAM JOINED YET
        when (selectedTab) {
          0 -> {
            BrowseTeamsView(
              teams = teams,
              currentUser = currentUser,
              onJoinRequest = onSendJoinRequest,
              onJoinByCodeClick = { showJoinByIdDialog = true },
              onChallengeTeamClick = null
            )
          }
          else -> {
            CreateTeamInlineView(
              currentUser = currentUser,
              onCreate = { name, tag, slogan, game ->
                onCreateTeam(name, tag, slogan, game)
                selectedTab = 0
              }
            )
          }
        }
      }
    }
  }

  // DIALOGS & ACTION POPUPS
  if (showCreateTeamDialog) {
    CreateTeamDialog(
      onDismiss = { showCreateTeamDialog = false },
      onSubmit = { name, tag, slogan, game ->
        onCreateTeam(name, tag, slogan, game)
        showCreateTeamDialog = false
      }
    )
  }

  if (showJoinByIdDialog) {
    JoinTeamByCodeDialog(
      onDismiss = { showJoinByIdDialog = false },
      onSubmit = { code ->
        onJoinByCode(code)
        showJoinByIdDialog = false
      }
    )
  }

  if (showInvitePlayerDialog && myTeam != null) {
    InvitePlayerDialog(
      team = myTeam,
      onDismiss = { showInvitePlayerDialog = false },
      onSubmit = { playerId, playerName ->
        onInvitePlayer(myTeam.id, playerId, playerName)
        showInvitePlayerDialog = false
      }
    )
  }

  if (showDepositDialog && myTeam != null) {
    DepositDialog(
      currentBalance = currentUser.walletBalance,
      onDismiss = { showDepositDialog = false },
      onSubmit = { amount ->
        onTeamDeposit(myTeam.id, amount)
        showDepositDialog = false
      }
    )
  }

  if (showWithdrawDialog && myTeam != null) {
    WithdrawDialog(
      teamBalance = myTeam.balance,
      onDismiss = { showWithdrawDialog = false },
      onSubmit = { amount ->
        onTeamWithdraw(myTeam.id, amount)
        showWithdrawDialog = false
      }
    )
  }

  if (showSendChallengeDialog && myTeam != null) {
    SendChallengeDialog(
      myTeam = myTeam,
      otherTeams = teams.filter { it.id != myTeam.id },
      onDismiss = { showSendChallengeDialog = false },
      onSubmit = { oppTeamId, game, amt ->
        onSendTeamChallenge(myTeam.id, oppTeamId, game, amt)
        showSendChallengeDialog = false
      }
    )
  }

  if (showWithdrawTimeWarning) {
    BengaliConfirmDialog(
      title = "উইথড্র সময়সীমা সতর্কতা",
      message = "টিম ও ব্যক্তিগত ওয়ালেট উইথড্র করার নির্ধারিত সময় সন্ধ্যা ৭:০০ টা থেকে রাত ১১:০০ টা পর্যন্ত। এই সময়ের বাইরে ট্রানজেকশন প্রটেকশনের জন্য উইথড্র সাময়িকভাবে বন্ধ থাকে।",
      confirmButtonText = "বুঝতে পেরেছি",
      cancelButtonText = "বন্ধ করুন",
      onConfirm = { showWithdrawTimeWarning = false },
      onDismiss = { showWithdrawTimeWarning = false }
    )
  }

  // Remove Member Confirm Dialog
  memberToRemove?.let { mem ->
    BengaliConfirmDialog(
      title = "মেম্বার রিমুভ নিশ্চিতকরণ",
      message = "আপনি কি নিশ্চিত '${mem.name}'-কে টিম থেকে রিমুভ করতে চান?",
      confirmButtonText = "হ্যাঁ, রিমুভ করুন",
      cancelButtonText = "বাতিল",
      isDanger = true,
      onConfirm = {
        if (myTeam != null) {
          onRemoveMember(myTeam.id, mem.id)
        }
        memberToRemove = null
      },
      onDismiss = { memberToRemove = null }
    )
  }

  // Change Role Dialog
  memberToRoleChange?.let { mem ->
    RoleChangeDialog(
      member = mem,
      onDismiss = { memberToRoleChange = null },
      onSelectRole = { newRole ->
        if (myTeam != null) {
          onUpdateMemberRole(myTeam.id, mem.id, newRole)
        }
        memberToRoleChange = null
      }
    )
  }

  // Toast indicator for Team ID copy
  if (showTeamIdCopiedToast) {
    LaunchedEffect(Unit) {
      kotlinx.coroutines.delay(2000)
      showTeamIdCopiedToast = false
    }
  }
}

// -------------------------------------------------------------
// YOUR TEAM VIEW (Captain / Member complete UI)
// -------------------------------------------------------------
@Composable
private fun YourTeamView(
  team: Team,
  currentUser: UserProfile,
  isWithdrawAllowedTime: Boolean,
  onCopyTeamId: () -> Unit,
  onAddMemberClick: () -> Unit,
  onDepositClick: () -> Unit,
  onWithdrawClick: () -> Unit,
  onChallengeClick: () -> Unit,
  onPlayerClick: (String, String) -> Unit,
  onAcceptJoinReq: (String) -> Unit,
  onRejectJoinReq: (String) -> Unit,
  onRemoveMemberClick: (TeamMember) -> Unit,
  onChangeRoleClick: (TeamMember) -> Unit
) {
  val isCaptain = team.adminId == currentUser.id

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. TEAM HERO & ID BANNER
    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          // Banner Background
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(110.dp)
              .background(
                Brush.horizontalGradient(
                  listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                )
              )
              .padding(14.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Top
            ) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = CyberGreen.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, CyberGreen)
              ) {
                Text(
                  text = "● ${team.game.uppercase()} TEAM",
                  color = CyberGreen,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }

              // Clickable Public ID Badge with Copy
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = GamingDarkSurface.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, CyberCyan),
                modifier = Modifier.clickable { onCopyTeamId() }
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(13.dp))
                  Text(
                    text = "ID: ${team.publicId}",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }

          // Team Info Row
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(56.dp)
                  .clip(CircleShape)
                  .background(Brush.linearGradient(listOf(CyberOrange, CyberRed)))
                  .border(2.dp, CyberCyan, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = team.tag,
                  color = Color.White,
                  fontWeight = FontWeight.Black,
                  fontSize = 16.sp
                )
              }

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = team.name,
                  color = TextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 19.sp
                )
                Text(
                  text = "\"${team.slogan}\"",
                  color = CyberOrangeGlow,
                  fontSize = 12.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "ক্যাপ্টেন: ${team.adminName} ${if (isCaptain) "(আপনি)" else ""}",
                  color = TextSecondary,
                  fontSize = 12.sp
                )
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = GamingDarkSurfaceVariant
              ) {
                Text(
                  text = "★ ${team.rating} ELO",
                  color = CyberGold,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stats row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              StatCard(title = "মোট ম্যাচ", value = "${team.matches}", color = TextPrimary, modifier = Modifier.weight(1f))
              StatCard(title = "বিজয় (Wins)", value = "${team.wins}", color = CyberGreen, modifier = Modifier.weight(1f))
              val winRate = if (team.matches > 0) (team.wins * 100 / team.matches) else 0
              StatCard(title = "উইনরেট", value = "$winRate%", color = CyberOrange, modifier = Modifier.weight(1f))
            }
          }
        }
      }
    }

    // 2. CAPTAIN ACTION BUTTONS (INVITE / CHALLENGE / SHARE ID)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = onAddMemberClick,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
          modifier = Modifier.weight(1f).testTag("invite_player_btn")
        ) {
          Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "মেম্বার যোগ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Button(
          onClick = onChallengeClick,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CyberOrange, contentColor = Color.Black),
          modifier = Modifier.weight(1f).testTag("challenge_team_btn")
        ) {
          Icon(imageVector = Icons.Default.SportsKabaddi, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "চ্যালেঞ্জ পাঠান", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        OutlinedButton(
          onClick = onCopyTeamId,
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, GamingCardBorder),
          modifier = Modifier.weight(0.9f)
        ) {
          Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "টিম আইডি", color = TextPrimary, fontSize = 12.sp)
        }
      }
    }

    // 3. TEAM WALLET BOX
    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(text = "টিম ওয়ালেট ব্যালেন্স", color = TextSecondary, fontSize = 12.sp)
              Text(text = "৳${team.balance.toInt()} টাকা", color = CyberGreen, fontWeight = FontWeight.Black, fontSize = 22.sp)
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isWithdrawAllowedTime) CyberGreen.copy(alpha = 0.15f) else CyberRed.copy(alpha = 0.15f)
            ) {
              Text(
                text = if (isWithdrawAllowedTime) "উইথড্র খোলা (৭টা-১১টা)" else "উইথড্র বন্ধ (৭টা-১১টা)",
                color = if (isWithdrawAllowedTime) CyberGreen else CyberRed,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = onDepositClick,
              colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(imageVector = Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("ডিপোজিট", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Button(
              onClick = onWithdrawClick,
              enabled = isCaptain,
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isWithdrawAllowedTime) CyberOrange else GamingDarkSurfaceVariant,
                contentColor = if (isWithdrawAllowedTime) Color.Black else TextMuted
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("উত্তোলন", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }
    }

    // 4. PENDING JOIN REQUESTS (VISIBLE TO CAPTAIN)
    if (isCaptain && team.joinRequests.isNotEmpty()) {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "টিম জয়েন রিকোয়েস্ট (${team.joinRequests.size})",
            color = CyberOrange,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )

          team.joinRequests.forEach { req ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = GamingDarkSurface,
              border = BorderStroke(1.dp, CyberOrange.copy(alpha = 0.5f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(text = req.userName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  Text(text = "IGN: ${req.userIgn} • ${req.timestamp}", color = TextSecondary, fontSize = 11.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Button(
                    onClick = { onAcceptJoinReq(req.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                  ) {
                    Text("Accept", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                  }

                  OutlinedButton(
                    onClick = { onRejectJoinReq(req.id) },
                    border = BorderStroke(1.dp, CyberRed),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                  ) {
                    Text("Reject", color = CyberRed, fontSize = 11.sp)
                  }
                }
              }
            }
          }
        }
      }
    }

    // 5. MEMBERS LIST WITH ROLES, STATUS & CONTROLS
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "টিম মেম্বারস (${team.members.size})",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
          Text(
            text = "অনলাইন: ${team.members.count { it.isOnline }} জন",
            color = CyberGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        Surface(
          shape = RoundedCornerShape(16.dp),
          color = GamingDarkSurface,
          border = BorderStroke(1.dp, GamingCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            team.members.forEach { member ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { onPlayerClick(member.id, member.name) }
                  .padding(8.dp),
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
                      .size(34.dp)
                      .clip(CircleShape)
                      .background(Brush.linearGradient(listOf(CyberCyan, CyberOrange))),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = member.name.take(1).uppercase(),
                      color = Color.Black,
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp
                    )
                  }

                  Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                      Text(text = member.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                      OnlineStatusBadge(isOnline = member.isOnline)
                    }
                    Text(
                      text = "Role: ${member.role} • IGN: ${member.ign}",
                      color = if (member.role.contains("Captain", true) || member.role.contains("লিডার")) CyberGold else TextSecondary,
                      fontSize = 11.sp
                    )
                  }
                }

                // Captain actions on member (if not self)
                if (isCaptain && member.id != currentUser.id) {
                  Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { onChangeRoleClick(member) }, modifier = Modifier.size(28.dp)) {
                      Icon(imageVector = Icons.Default.Edit, contentDescription = "রোল পরিবর্তন", tint = CyberCyan, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { onRemoveMemberClick(member) }, modifier = Modifier.size(28.dp)) {
                      Icon(imageVector = Icons.Default.PersonRemove, contentDescription = "রিমুভ", tint = CyberRed, modifier = Modifier.size(16.dp))
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
}

// -------------------------------------------------------------
// BROWSE OTHER TEAMS VIEW
// -------------------------------------------------------------
@Composable
private fun BrowseTeamsView(
  teams: List<Team>,
  currentUser: UserProfile,
  onJoinRequest: (String) -> Unit,
  onJoinByCodeClick: () -> Unit,
  onChallengeTeamClick: (() -> Unit)?
) {
  var searchQuery by remember { mutableStateOf("") }
  val filteredTeams = remember(teams, searchQuery) {
    if (searchQuery.isBlank()) teams
    else teams.filter { it.name.contains(searchQuery, ignoreCase = true) || it.tag.contains(searchQuery, ignoreCase = true) || it.publicId.contains(searchQuery, ignoreCase = true) }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Search & Direct Code Join Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("টিম নাম বা ট্যাগ খুঁজুন...", fontSize = 13.sp) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
          colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f)
        )

        Button(
          onClick = onJoinByCodeClick,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
          modifier = Modifier.height(52.dp)
        ) {
          Text("আইডি দিয়ে জয়েন", fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
      }
    }

    if (filteredTeams.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
          contentAlignment = Alignment.Center
        ) {
          Text("কোনো টিম পাওয়া যায়নি।", color = TextSecondary, fontSize = 14.sp)
        }
      }
    } else {
      items(filteredTeams) { team ->
        TeamBrowseCard(
          team = team,
          currentUserId = currentUser.id,
          onJoinClick = { onJoinRequest(team.id) },
          onChallengeClick = onChallengeTeamClick
        )
      }
    }
  }
}

@Composable
private fun TeamBrowseCard(
  team: Team,
  currentUserId: String,
  onJoinClick: () -> Unit,
  onChallengeClick: (() -> Unit)?
) {
  val hasPendingReq = team.joinRequests.any { it.userId == currentUserId }

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
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(Brush.linearGradient(listOf(CyberCyan, CyberOrange))),
            contentAlignment = Alignment.Center
          ) {
            Text(text = team.tag, color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
          }

          Column {
            Text(text = team.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = "আইডি: ${team.publicId} • লিডার: ${team.adminName}", color = TextSecondary, fontSize = 11.sp)
          }
        }

        Surface(shape = RoundedCornerShape(6.dp), color = GamingDarkSurfaceVariant) {
          Text(
            text = "★ ${team.rating}",
            color = CyberGold,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "মেম্বার: ${team.members.size} জন (অনলাইন: ${team.members.count { it.isOnline }}) • জয়: ${team.wins}",
          color = TextSecondary,
          fontSize = 12.sp
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          if (onChallengeClick != null) {
            OutlinedButton(
              onClick = onChallengeClick,
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, CyberOrange),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text("চ্যালেঞ্জ", color = CyberOrange, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
          }

          Button(
            onClick = onJoinClick,
            enabled = !hasPendingReq,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (hasPendingReq) GamingDarkSurfaceVariant else CyberGreen,
              contentColor = if (hasPendingReq) TextMuted else Color.Black
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Text(
              text = if (hasPendingReq) "Request Sent" else "Join Team",
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// TEAM CHALLENGES VIEW
// -------------------------------------------------------------
@Composable
private fun TeamChallengesView(
  challenges: List<TeamChallenge>,
  myTeamId: String,
  onSendChallengeClick: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "রানিং ও আপকামিং ম্যাচ", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Button(
          onClick = onSendChallengeClick,
          colors = ButtonDefaults.buttonColors(containerColor = CyberOrange, contentColor = Color.Black),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("চ্যালেঞ্জ দিন", fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
      }
    }

    if (challenges.isEmpty()) {
      item {
        Box(
          modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
          contentAlignment = Alignment.Center
        ) {
          Text("এখন কোনো টিম চ্যালেঞ্জ চলমান নেই।", color = TextSecondary, fontSize = 13.sp)
        }
      }
    } else {
      items(challenges) { ch ->
        Surface(
          shape = RoundedCornerShape(12.dp),
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
                text = "${ch.challengerTeamName} VS ${ch.opponentTeamName}",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              Text(text = ch.status.labelBn, color = CyberOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "${ch.game} • স্টেক: ৳${ch.amount.toInt()} টাকা", color = TextSecondary, fontSize = 12.sp)
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// INLINE CREATE TEAM FORM
// -------------------------------------------------------------
@Composable
private fun CreateTeamInlineView(
  currentUser: UserProfile,
  onCreate: (name: String, tag: String, slogan: String, game: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var tag by remember { mutableStateOf("") }
  var slogan by remember { mutableStateOf("No fear, only victory!") }
  var game by remember { mutableStateOf("Free Fire") }
  var error by remember { mutableStateOf<String?>(null) }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text(text = "নতুন টিম তৈরি করুন", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 20.sp)
      Text(
        text = "টিম তৈরি করলে আপনি স্বয়ংক্রিয়ভাবে টিম ক্যাপ্টেন হিসেবে গণ্য হবেন এবং সম্পূর্ণ টিম নিয়ন্ত্রণ করতে পারবেন।",
        color = TextSecondary,
        fontSize = 12.sp
      )
    }

    item {
      OutlinedTextField(
        value = name,
        onValueChange = { name = it; error = null },
        label = { Text("টিমের নাম (Team Name)") },
        placeholder = { Text("উদা: Cyber Hunters BD") },
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      OutlinedTextField(
        value = tag,
        onValueChange = { tag = it.take(5); error = null },
        label = { Text("টিম ট্যাগ (Tag: ২-৫ অক্ষর)") },
        placeholder = { Text("উদা: CHBD") },
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      OutlinedTextField(
        value = slogan,
        onValueChange = { slogan = it },
        label = { Text("টিম স্লোগান / ডেসক্রিপশন") },
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      OutlinedTextField(
        value = game,
        onValueChange = { game = it },
        label = { Text("প্রধান গেম (Game)") },
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
        modifier = Modifier.fillMaxWidth()
      )
    }

    error?.let {
      item {
        Text(text = it, color = CyberRed, fontSize = 12.sp)
      }
    }

    item {
      Button(
        onClick = {
          if (name.isBlank() || tag.isBlank()) {
            error = "টিমের নাম ও ট্যাগ অবশ্যই পূরণ করুন।"
          } else {
            onCreate(name, tag, slogan, game)
          }
        },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CyberOrange, contentColor = Color.Black),
        modifier = Modifier.fillMaxWidth().height(48.dp)
      ) {
        Text("Create Team (টিম তৈরি করুন)", fontWeight = FontWeight.Black, fontSize = 14.sp)
      }
    }
  }
}

// -------------------------------------------------------------
// HELPER DIALOGS
// -------------------------------------------------------------
@Composable
private fun CreateTeamDialog(
  onDismiss: () -> Unit,
  onSubmit: (String, String, String, String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var tag by remember { mutableStateOf("") }
  var slogan by remember { mutableStateOf("Fight to Win!") }
  var game by remember { mutableStateOf("Free Fire") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = GamingDarkSurface,
      border = BorderStroke(1.dp, GamingCardBorder),
      modifier = Modifier.padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("নতুন টিম তৈরি", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("টিমের নাম") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = tag, onValueChange = { tag = it.take(5) }, label = { Text("ট্যাগ (যেমন: VPR)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = slogan, onValueChange = { slogan = it }, label = { Text("স্লোগান") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল", color = TextSecondary) }
          Button(
            onClick = { if (name.isNotBlank() && tag.isNotBlank()) onSubmit(name, tag, slogan, game) },
            colors = ButtonDefaults.buttonColors(containerColor = CyberOrange, contentColor = Color.Black),
            modifier = Modifier.weight(1f)
          ) { Text("তৈরি করুন", fontWeight = FontWeight.Bold) }
        }
      }
    }
  }
}

@Composable
private fun JoinTeamByCodeDialog(
  onDismiss: () -> Unit,
  onSubmit: (String) -> Unit
) {
  var code by remember { mutableStateOf("") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = GamingDarkSurface,
      border = BorderStroke(1.dp, GamingCardBorder),
      modifier = Modifier.padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("টিম আইডি দিয়ে যোগ দিন", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text("টিম ক্যাপ্টেনের দেওয়া ইউনিক টিম আইডি (যেমন: CHBD-8841) প্রবেশ করান:", color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = code, onValueChange = { code = it.uppercase() }, placeholder = { Text("উদা: CHBD-8841") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল", color = TextSecondary) }
          Button(
            onClick = { if (code.isNotBlank()) onSubmit(code) },
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
            modifier = Modifier.weight(1f)
          ) { Text("যোগ দিন", fontWeight = FontWeight.Bold) }
        }
      }
    }
  }
}

@Composable
private fun InvitePlayerDialog(
  team: Team,
  onDismiss: () -> Unit,
  onSubmit: (String, String) -> Unit
) {
  val clipboardManager = LocalClipboardManager.current
  var playerIdInput by remember { mutableStateOf("") }
  var copiedToast by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = GamingDarkSurface,
      border = BorderStroke(1.dp, GamingCardBorder),
      modifier = Modifier.padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("টিমে মেম্বার যোগ / ইনভাইট", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        // Direct ID Sharing Box
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = GamingDarkSurfaceVariant,
          modifier = Modifier.fillMaxWidth().clickable {
            clipboardManager.setText(AnnotatedString(team.publicId))
            copiedToast = true
          }
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("আপনার টিম আইডি (কপি করুন):", color = TextSecondary, fontSize = 11.sp)
              Text(team.publicId, color = CyberCyan, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyberCyan)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("অথবা প্লেয়ার আইডি / নাম দিয়ে সরাসরি যুক্ত করুন:", color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = playerIdInput,
          onValueChange = { playerIdInput = it },
          placeholder = { Text("প্লেয়ারের নাম / গেম আইডি") },
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বন্ধ করুন", color = TextSecondary) }
          Button(
            onClick = {
              if (playerIdInput.isNotBlank()) {
                onSubmit("user_${System.currentTimeMillis() % 1000}", playerIdInput.trim())
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
            modifier = Modifier.weight(1f)
          ) { Text("যুক্ত করুন", fontWeight = FontWeight.Bold) }
        }
      }
    }
  }
}

@Composable
private fun RoleChangeDialog(
  member: TeamMember,
  onDismiss: () -> Unit,
  onSelectRole: (String) -> Unit
) {
  val roles = listOf("Captain", "Co-Captain", "Member", "Sniper", "Rusher")

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = GamingDarkSurface,
      border = BorderStroke(1.dp, GamingCardBorder),
      modifier = Modifier.padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("রোল পরিবর্তন: ${member.name}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(10.dp))
        roles.forEach { role ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectRole(role) }
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = member.role.equals(role, ignoreCase = true), onClick = { onSelectRole(role) })
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = role, color = TextPrimary, fontSize = 14.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun DepositDialog(
  currentBalance: Double,
  onDismiss: () -> Unit,
  onSubmit: (Double) -> Unit
) {
  var amountStr by remember { mutableStateOf("200") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = GamingDarkSurface,
      border = BorderStroke(1.dp, GamingCardBorder),
      modifier = Modifier.padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("টিম ওয়ালেটে ডিপোজিট", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("ব্যক্তিগত ব্যালেন্স: ৳${currentBalance.toInt()} টাকা", color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("টাকার পরিমাণ") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল", color = TextSecondary) }
          Button(
            onClick = { onSubmit(amountStr.toDoubleOrNull() ?: 0.0) },
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
            modifier = Modifier.weight(1f)
          ) { Text("ডিপোজিট", fontWeight = FontWeight.Bold) }
        }
      }
    }
  }
}

@Composable
private fun WithdrawDialog(
  teamBalance: Double,
  onDismiss: () -> Unit,
  onSubmit: (Double) -> Unit
) {
  var amountStr by remember { mutableStateOf("100") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = GamingDarkSurface,
      border = BorderStroke(1.dp, GamingCardBorder),
      modifier = Modifier.padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("টিম ওয়ালেট থেকে উত্তোলন (সন্ধ্যা ৭টা - ১১টা)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("টিম ফান্ড ব্যালেন্স: ৳${teamBalance.toInt()} টাকা", color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("উত্তোলনের পরিমাণ") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল", color = TextSecondary) }
          Button(
            onClick = { onSubmit(amountStr.toDoubleOrNull() ?: 0.0) },
            colors = ButtonDefaults.buttonColors(containerColor = CyberOrange, contentColor = Color.Black),
            modifier = Modifier.weight(1f)
          ) { Text("উত্তোলন", fontWeight = FontWeight.Bold) }
        }
      }
    }
  }
}

@Composable
private fun SendChallengeDialog(
  myTeam: Team,
  otherTeams: List<Team>,
  onDismiss: () -> Unit,
  onSubmit: (oppTeamId: String, game: String, amount: Double) -> Unit
) {
  var selectedOpponent by remember { mutableStateOf(otherTeams.firstOrNull()) }
  var challengeAmount by remember { mutableStateOf("100") }
  var selectedGame by remember { mutableStateOf("Free Fire") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = GamingDarkSurface,
      border = BorderStroke(1.dp, GamingCardBorder),
      modifier = Modifier.padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("প্রতিপক্ষ টিমকে চ্যালেঞ্জ দিন", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("প্রতিপক্ষ টিম নির্বাচন করুন:", color = TextSecondary, fontSize = 12.sp)

        otherTeams.forEach { opp ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { selectedOpponent = opp }
              .padding(vertical = 4.dp)
          ) {
            RadioButton(selected = selectedOpponent?.id == opp.id, onClick = { selectedOpponent = opp })
            Spacer(modifier = Modifier.width(4.dp))
            Text("${opp.name} (${opp.tag})", color = TextPrimary, fontSize = 13.sp)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = challengeAmount,
          onValueChange = { challengeAmount = it },
          label = { Text("স্টেক অ্যামাউন্ট (৫০ - ২০০ টাকা)") },
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল", color = TextSecondary) }
          Button(
            onClick = {
              selectedOpponent?.let { opp ->
                onSubmit(opp.id, selectedGame, challengeAmount.toDoubleOrNull() ?: 100.0)
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberOrange, contentColor = Color.Black),
            modifier = Modifier.weight(1f)
          ) { Text("চ্যালেঞ্জ পাঠান", fontWeight = FontWeight.Bold) }
        }
      }
    }
  }
}

@Composable
private fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = GamingDarkSurfaceVariant,
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = title, color = TextSecondary, fontSize = 10.sp)
      Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
  }
}
