package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.*
import com.example.data.repository.KheloBDRepository
import com.example.ui.components.AppHeader
import com.example.ui.components.BengaliConfirmDialog
import com.example.ui.components.PublicProfileDialog
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.challenges.ChallengeDetailPage
import com.example.ui.screens.challenges.UserChallengeScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.leaderboard.LeaderboardScreen
import com.example.ui.screens.notifications.NotificationsDialog
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.teams.TeamsScreen
import com.example.ui.screens.tournaments.TournamentDetailPage
import com.example.ui.screens.tournaments.TournamentsScreen
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class Screen {
  HOME,
  TOURNAMENTS,
  TOURNAMENT_DETAIL,
  CHALLENGES,
  CHALLENGE_DETAIL,
  TEAMS,
  LEADERBOARD,
  ACCOUNT
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        KheloBDApp()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KheloBDApp() {
  val repository = remember { KheloBDRepository.getInstance() }
  val scope = rememberCoroutineScope()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val snackbarHostState = remember { SnackbarHostState() }

  // State collections from Repository
  val currentUser by repository.currentUser.collectAsStateWithLifecycle()
  val tournaments by repository.tournaments.collectAsStateWithLifecycle()
  val challenges by repository.challenges.collectAsStateWithLifecycle()
  val teams by repository.teams.collectAsStateWithLifecycle()
  val teamChallenges by repository.teamChallenges.collectAsStateWithLifecycle()
  val transactions by repository.transactions.collectAsStateWithLifecycle()
  val notifications by repository.notifications.collectAsStateWithLifecycle()
  val banners by repository.banners.collectAsStateWithLifecycle()
  val gameCategories by repository.gameCategories.collectAsStateWithLifecycle()
  val topPlayers by repository.topPlayers.collectAsStateWithLifecycle()

  var currentScreen by remember { mutableStateOf(Screen.HOME) }
  var selectedTournamentId by remember { mutableStateOf<String?>(null) }
  var selectedChallengeId by remember { mutableStateOf<String?>(null) }
  var showNotificationsDialog by remember { mutableStateOf(false) }
  var publicProfileUser by remember { mutableStateOf<UserProfile?>(null) }
  var challengeForOpponentProfile by remember { mutableStateOf<PlayerChallenge?>(null) }
  var pendingTournamentToJoin by remember { mutableStateOf<Tournament?>(null) }

  val unreadNotifCount = remember(notifications) {
    notifications.count { !it.isRead }
  }

  // Handle hardware / gesture back navigation
  BackHandler(enabled = currentScreen != Screen.HOME || drawerState.isOpen) {
    if (drawerState.isOpen) {
      scope.launch { drawerState.close() }
    } else if (currentScreen == Screen.TOURNAMENT_DETAIL) {
      currentScreen = Screen.TOURNAMENTS
    } else if (currentScreen == Screen.CHALLENGE_DETAIL) {
      currentScreen = Screen.CHALLENGES
    } else {
      currentScreen = Screen.HOME
    }
  }

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet(
        drawerContainerColor = GamingDarkSurface,
        drawerContentColor = TextPrimary,
        modifier = Modifier.width(310.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(GamingDarkSurfaceVariant, GamingDarkSurface)))
            .padding(20.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_khelo_logo),
              contentDescription = "খেলো বিডি লোগো",
              modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.5.dp, CyberOrange, RoundedCornerShape(12.dp))
            )

            Column {
              Text(
                text = "খেলো বিডি",
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp
              )
              Text(
                text = "ESPORTS TOURNAMENT ARENA",
                color = CyberOrangeGlow,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                letterSpacing = 1.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = GamingDarkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, GamingCardBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(text = currentUser.fullName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "ব্যালেন্স: ৳${currentUser.walletBalance.toInt()} টাকা", color = CyberGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              }
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = CyberOrange.copy(alpha = 0.2f)
              ) {
                Text(
                  text = currentUser.userCode,
                  color = CyberOrange,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        }

        HorizontalDivider(color = GamingCardBorder)

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
        ) {
          NavigationDrawerItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null, tint = if (currentScreen == Screen.HOME) CyberOrange else TextSecondary) },
            label = { Text("হোম (Home)", fontWeight = FontWeight.SemiBold) },
            selected = currentScreen == Screen.HOME,
            onClick = {
              currentScreen = Screen.HOME
              scope.launch { drawerState.close() }
            },
            colors = NavigationDrawerItemDefaults.colors(
              selectedContainerColor = GamingDarkSurfaceVariant,
              selectedTextColor = CyberOrange,
              unselectedTextColor = TextPrimary
            ),
            modifier = Modifier.padding(horizontal = 12.dp)
          )

          NavigationDrawerItem(
            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = if (currentScreen == Screen.TOURNAMENTS) CyberOrange else TextSecondary) },
            label = { Text("টুর্নামেন্ট (Tournaments)", fontWeight = FontWeight.SemiBold) },
            selected = currentScreen == Screen.TOURNAMENTS,
            onClick = {
              currentScreen = Screen.TOURNAMENTS
              scope.launch { drawerState.close() }
            },
            colors = NavigationDrawerItemDefaults.colors(
              selectedContainerColor = GamingDarkSurfaceVariant,
              selectedTextColor = CyberOrange,
              unselectedTextColor = TextPrimary
            ),
            modifier = Modifier.padding(horizontal = 12.dp)
          )

          NavigationDrawerItem(
            icon = { Icon(Icons.Default.SportsKabaddi, contentDescription = null, tint = if (currentScreen == Screen.CHALLENGES) CyberCyan else TextSecondary) },
            label = { Text("১v১ চ্যালেঞ্জ (1v1 Challenge)", fontWeight = FontWeight.SemiBold) },
            selected = currentScreen == Screen.CHALLENGES,
            onClick = {
              currentScreen = Screen.CHALLENGES
              scope.launch { drawerState.close() }
            },
            colors = NavigationDrawerItemDefaults.colors(
              selectedContainerColor = GamingDarkSurfaceVariant,
              selectedTextColor = CyberCyan,
              unselectedTextColor = TextPrimary
            ),
            modifier = Modifier.padding(horizontal = 12.dp)
          )

          NavigationDrawerItem(
            icon = { Icon(Icons.Default.Groups, contentDescription = null, tint = if (currentScreen == Screen.TEAMS) CyberGreen else TextSecondary) },
            label = { Text("স্কোয়াড ও টিম (Teams)", fontWeight = FontWeight.SemiBold) },
            selected = currentScreen == Screen.TEAMS,
            onClick = {
              currentScreen = Screen.TEAMS
              scope.launch { drawerState.close() }
            },
            colors = NavigationDrawerItemDefaults.colors(
              selectedContainerColor = GamingDarkSurfaceVariant,
              selectedTextColor = CyberGreen,
              unselectedTextColor = TextPrimary
            ),
            modifier = Modifier.padding(horizontal = 12.dp)
          )

          NavigationDrawerItem(
            icon = { Icon(Icons.Default.Leaderboard, contentDescription = null, tint = if (currentScreen == Screen.LEADERBOARD) CyberGold else TextSecondary) },
            label = { Text("লিডারবোর্ড (Leaderboard)", fontWeight = FontWeight.SemiBold) },
            selected = currentScreen == Screen.LEADERBOARD,
            onClick = {
              currentScreen = Screen.LEADERBOARD
              scope.launch { drawerState.close() }
            },
            colors = NavigationDrawerItemDefaults.colors(
              selectedContainerColor = GamingDarkSurfaceVariant,
              selectedTextColor = CyberGold,
              unselectedTextColor = TextPrimary
            ),
            modifier = Modifier.padding(horizontal = 12.dp)
          )

          NavigationDrawerItem(
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = if (currentScreen == Screen.ACCOUNT) CyberOrange else TextSecondary) },
            label = { Text("আমার ওয়ালেট ও প্রোফাইল", fontWeight = FontWeight.SemiBold) },
            selected = currentScreen == Screen.ACCOUNT,
            onClick = {
              currentScreen = Screen.ACCOUNT
              scope.launch { drawerState.close() }
            },
            colors = NavigationDrawerItemDefaults.colors(
              selectedContainerColor = GamingDarkSurfaceVariant,
              selectedTextColor = CyberOrange,
              unselectedTextColor = TextPrimary
            ),
            modifier = Modifier.padding(horizontal = 12.dp)
          )

          NavigationDrawerItem(
            icon = {
              Box {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = TextSecondary)
                if (unreadNotifCount > 0) {
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .clip(CircleShape)
                      .background(CyberRed)
                      .align(Alignment.TopEnd)
                  )
                }
              }
            },
            label = { Text("নোটিফিকেশন ($unreadNotifCount)", fontWeight = FontWeight.SemiBold) },
            selected = false,
            onClick = {
              showNotificationsDialog = true
              scope.launch { drawerState.close() }
            },
            modifier = Modifier.padding(horizontal = 12.dp)
          )
        }
      }
    }
  ) {
    val isDetailPage = currentScreen == Screen.TOURNAMENT_DETAIL || currentScreen == Screen.CHALLENGE_DETAIL

    Scaffold(
      topBar = {
        if (!isDetailPage) {
          AppHeader(
            title = "খেলো বিডি",
            unreadNotificationCount = unreadNotifCount,
            userBalance = currentUser.walletBalance,
            onMenuClick = { scope.launch { drawerState.open() } },
            onNotificationClick = { showNotificationsDialog = true },
            onProfileClick = { currentScreen = Screen.ACCOUNT },
            onWalletClick = { currentScreen = Screen.ACCOUNT }
          )
        }
      },
      bottomBar = {
        if (!isDetailPage) {
          NavigationBar(
            containerColor = GamingDarkSurface,
            contentColor = TextPrimary,
            tonalElevation = 8.dp,
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
          ) {
            NavigationBarItem(
              selected = currentScreen == Screen.HOME,
              onClick = { currentScreen = Screen.HOME },
              icon = { Icon(Icons.Default.Home, contentDescription = "হোম") },
              label = { Text("হোম", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = CyberOrange,
                indicatorColor = CyberOrange,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
              )
            )

            NavigationBarItem(
              selected = currentScreen == Screen.TOURNAMENTS,
              onClick = { currentScreen = Screen.TOURNAMENTS },
              icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "টুর্নামেন্ট") },
              label = { Text("টুর্নামেন্ট", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = CyberOrange,
                indicatorColor = CyberOrange,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
              )
            )

            NavigationBarItem(
              selected = currentScreen == Screen.CHALLENGES,
              onClick = { currentScreen = Screen.CHALLENGES },
              icon = { Icon(Icons.Default.SportsKabaddi, contentDescription = "১v১") },
              label = { Text("১v১ চ্যালেঞ্জ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = CyberCyan,
                indicatorColor = CyberCyan,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
              )
            )

            NavigationBarItem(
              selected = currentScreen == Screen.TEAMS,
              onClick = { currentScreen = Screen.TEAMS },
              icon = { Icon(Icons.Default.Groups, contentDescription = "স্কোয়াড") },
              label = { Text("টিম", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = CyberGreen,
                indicatorColor = CyberGreen,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
              )
            )

            NavigationBarItem(
              selected = currentScreen == Screen.ACCOUNT,
              onClick = { currentScreen = Screen.ACCOUNT },
              icon = { Icon(Icons.Default.Person, contentDescription = "অ্যাকাউন্ট") },
              label = { Text("অ্যাকাউন্ট", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = CyberOrange,
                indicatorColor = CyberOrange,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
              )
            )
          }
        }
      },
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      containerColor = GamingDarkBackground,
      modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(if (isDetailPage) PaddingValues(0.dp) else innerPadding)
      ) {
        when (currentScreen) {
          Screen.HOME -> {
            HomeScreen(
              tournaments = tournaments,
              challenges = challenges,
              gameCategories = gameCategories,
              topPlayers = topPlayers,
              topTeams = teams,
              banners = banners,
              currentUserId = currentUser.id,
              onNavigateToTournaments = { currentScreen = Screen.TOURNAMENTS },
              onNavigateToChallenges = { currentScreen = Screen.CHALLENGES },
              onNavigateToTeams = { currentScreen = Screen.TEAMS },
              onNavigateToLeaderboard = { currentScreen = Screen.LEADERBOARD },
              onJoinTournamentClick = { tour ->
                selectedTournamentId = tour.id
                currentScreen = Screen.TOURNAMENT_DETAIL
              },
              onAcceptChallengeClick = { ch ->
                val result = repository.acceptChallenge(ch.id)
                scope.launch {
                  snackbarHostState.showSnackbar(result.getOrDefault("চ্যালেঞ্জ একসেপ্ট করা হয়েছে!"))
                }
              },
              onCancelChallengeClick = { ch ->
                val result = repository.cancelChallenge(ch.id)
                scope.launch {
                  snackbarHostState.showSnackbar(result.getOrDefault("চ্যালেঞ্জ বাতিল করা হয়েছে।"))
                }
              },
              onChallengeDetailClick = { ch ->
                selectedChallengeId = ch.id
                currentScreen = Screen.CHALLENGE_DETAIL
              },
              onPlayerClick = { userId, name ->
                publicProfileUser = UserProfile(
                  id = userId,
                  username = name,
                  fullName = name,
                  phone = "01XXXXXXXXX",
                  email = "$name@khelobd.com",
                  walletBalance = 300.0,
                  isOnline = true,
                  matchesPlayed = 42,
                  wins = 28,
                  totalEarnings = 3500.0,
                  freeFireUid = "54829${userId.takeLast(3)}",
                  pubgUid = "51482${userId.takeLast(4)}"
                )
              },
              onTeamClick = { team ->
                currentScreen = Screen.TEAMS
              },
              onSelectGameCategory = {
                currentScreen = Screen.TOURNAMENTS
              }
            )
          }

          Screen.TOURNAMENTS -> {
            TournamentsScreen(
              tournaments = tournaments,
              currentUser = currentUser,
              onJoinClick = { tour ->
                selectedTournamentId = tour.id
                currentScreen = Screen.TOURNAMENT_DETAIL
              },
              onOpenTournamentDetail = { tour ->
                selectedTournamentId = tour.id
                currentScreen = Screen.TOURNAMENT_DETAIL
              }
            )
          }

          Screen.TOURNAMENT_DETAIL -> {
            val tour = tournaments.find { it.id == selectedTournamentId } ?: tournaments.firstOrNull()
            if (tour != null) {
              TournamentDetailPage(
                tournament = tour,
                currentUser = currentUser,
                onBack = { currentScreen = Screen.TOURNAMENTS },
                onJoinSlot = { tournamentId, slotNumber ->
                  val result = repository.joinTournament(tournamentId, slotNumber)
                  scope.launch {
                    snackbarHostState.showSnackbar(result.getOrDefault("স্লটে সফলভাবে জয়েন সম্পন্ন হয়েছে!"))
                  }
                }
              )
            } else {
              currentScreen = Screen.TOURNAMENTS
            }
          }

          Screen.CHALLENGES -> {
            UserChallengeScreen(
              challenges = challenges,
              currentUser = currentUser,
              onCreateChallenge = { game, mode, map, rule, amount, note ->
                val result = repository.createChallenge(game, mode, map, rule, amount, note)
                scope.launch {
                  if (result.isSuccess) {
                    snackbarHostState.showSnackbar("চ্যালেঞ্জ পোস্ট সফল হয়েছে!")
                  } else {
                    snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "চ্যালেঞ্জ পোস্ট ব্যর্থ হয়েছে")
                  }
                }
              },
              onAcceptChallenge = { ch ->
                val result = repository.acceptChallenge(ch.id)
                scope.launch {
                  snackbarHostState.showSnackbar(result.getOrDefault("চ্যালেঞ্জ একসেপ্ট সফল হয়েছে!"))
                }
              },
              onCancelChallenge = { ch ->
                val result = repository.cancelChallenge(ch.id)
                scope.launch {
                  snackbarHostState.showSnackbar(result.getOrDefault("চ্যালেঞ্জ বাতিল ও রিফান্ড সম্পন্ন!"))
                }
              },
              onOpenChallengeDetail = { ch ->
                selectedChallengeId = ch.id
                currentScreen = Screen.CHALLENGE_DETAIL
              },
              onPlayerClick = { userId, name ->
                publicProfileUser = UserProfile(
                  id = userId,
                  username = name,
                  fullName = name,
                  phone = "01XXXXXXXXX",
                  email = "$name@khelobd.com",
                  walletBalance = 300.0,
                  isOnline = true,
                  matchesPlayed = 42,
                  wins = 28,
                  totalEarnings = 3500.0,
                  freeFireUid = "54829${userId.takeLast(3)}",
                  pubgUid = "51482${userId.takeLast(4)}"
                )
              }
            )
          }

          Screen.CHALLENGE_DETAIL -> {
            val ch = challenges.find { it.id == selectedChallengeId } ?: challenges.firstOrNull()
            if (ch != null) {
              ChallengeDetailPage(
                challenge = ch,
                currentUser = currentUser,
                onBack = { currentScreen = Screen.CHALLENGES },
                onAcceptChallenge = { targetCh ->
                  val result = repository.acceptChallenge(targetCh.id)
                  scope.launch {
                    snackbarHostState.showSnackbar(result.getOrDefault("চ্যালেঞ্জ একসেপ্ট সফল হয়েছে!"))
                  }
                },
                onCancelChallenge = { targetCh ->
                  val result = repository.cancelChallenge(targetCh.id)
                  scope.launch {
                    snackbarHostState.showSnackbar(result.getOrDefault("চ্যালেঞ্জ বাতিল ও রিফান্ড সম্পন্ন!"))
                  }
                },
                onSubmitRoom = { rId, rPass ->
                  val result = repository.submitRoomCredentials(ch.id, rId, rPass)
                  scope.launch {
                    snackbarHostState.showSnackbar(result.getOrDefault("রুম তথ্য সাবমিট হয়েছে!"))
                  }
                },
                onSubmitProof = { isChallenger ->
                  val result = repository.submitChallengeProof(ch.id, isChallenger)
                  scope.launch {
                    snackbarHostState.showSnackbar(result.getOrDefault("প্রুফ আপলোড সম্পন্ন!"))
                  }
                },
                onPlayerClick = { userId, name ->
                  publicProfileUser = UserProfile(
                    id = userId,
                    username = name,
                    fullName = name,
                    phone = "01XXXXXXXXX",
                    email = "$name@khelobd.com",
                    walletBalance = 300.0,
                    isOnline = true,
                    matchesPlayed = 42,
                    wins = 28,
                    totalEarnings = 3500.0,
                    freeFireUid = "54829${userId.takeLast(3)}",
                    pubgUid = "51482${userId.takeLast(4)}"
                  )
                }
              )
            } else {
              currentScreen = Screen.CHALLENGES
            }
          }

          Screen.TEAMS -> {
            TeamsScreen(
              teams = teams,
              teamChallenges = teamChallenges,
              currentUser = currentUser,
              isWithdrawAllowedTime = repository.isWithinWithdrawWindow(),
              onCreateTeam = { name, tag, slogan, game ->
                val result = repository.createTeam(name, tag, slogan, game)
                scope.launch {
                  if (result.isSuccess) {
                    snackbarHostState.showSnackbar("টিম '${result.getOrNull()?.name}' সফলভাবে তৈরি হয়েছে! আপনি টিম ক্যাপ্টেন।")
                  } else {
                    snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "টিম তৈরি ব্যর্থ হয়েছে")
                  }
                }
              },
              onSendJoinRequest = { teamId ->
                val result = repository.sendJoinTeamRequest(teamId)
                scope.launch {
                  snackbarHostState.showSnackbar(result.getOrDefault("টিমে জয়েন রিকোয়েস্ট পাঠানো হয়েছে!"))
                }
              },
              onAcceptJoinRequest = { teamId, reqId ->
                val result = repository.acceptJoinTeamRequest(teamId, reqId)
                scope.launch {
                  snackbarHostState.showSnackbar(result.getOrDefault("মেম্বারকে টিমে অন্তর্ভুক্ত করা হয়েছে!"))
                }
              },
              onRejectJoinRequest = { teamId, reqId ->
                val result = repository.rejectJoinTeamRequest(teamId, reqId)
                scope.launch {
                  snackbarHostState.showSnackbar(result.getOrDefault("অনুরোধ বাতিল করা হয়েছে।"))
                }
              },
              onJoinByCode = { code ->
                val result = repository.joinTeamByPublicId(code)
                scope.launch {
                  if (result.isSuccess) {
                    snackbarHostState.showSnackbar("অভিনন্দন! টিম '${result.getOrNull()?.name}'-এ জয়েন সম্পন্ন হয়েছে।")
                  } else {
                    snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "টিম আইডি সঠিক নয়!")
                  }
                }
              },
              onInvitePlayer = { teamId, pId, pName ->
                val result = repository.invitePlayerToTeam(teamId, pId, pName)
                scope.launch {
                  snackbarHostState.showSnackbar(result.getOrDefault("প্লেয়ারকে টিমে যুক্ত করা হয়েছে!"))
                }
              },
              onRemoveMember = { teamId, mId ->
                val result = repository.removeTeamMember(teamId, mId)
                scope.launch {
                  snackbarHostState.showSnackbar(result.getOrDefault("মেম্বারকে রিমুভ করা হয়েছে।"))
                }
              },
              onUpdateMemberRole = { teamId, mId, newRole ->
                val result = repository.updateTeamMemberRole(teamId, mId, newRole)
                scope.launch {
                  snackbarHostState.showSnackbar(result.getOrDefault("মেম্বার রোল আপডেট করা হয়েছে।"))
                }
              },
              onTeamDeposit = { teamId, amount ->
                val result = repository.teamDeposit(teamId, amount)
                scope.launch {
                  snackbarHostState.showSnackbar(result.getOrDefault("টিম ফান্ড ডিপোজিট সফল!"))
                }
              },
              onTeamWithdraw = { teamId, amount ->
                val result = repository.teamWithdraw(teamId, amount)
                scope.launch {
                  snackbarHostState.showSnackbar(result.getOrDefault("টিম ফান্ড উত্তোলন সফল!"))
                }
              },
              onSendTeamChallenge = { chTeamId, oppTeamId, game, amount ->
                val result = repository.sendTeamChallenge(chTeamId, oppTeamId, game, amount)
                scope.launch {
                  snackbarHostState.showSnackbar(result.getOrDefault("টিম চ্যালেঞ্জ পাঠানো হয়েছে!"))
                }
              },
              onNavigateToDeposit = { currentScreen = Screen.ACCOUNT },
              onPlayerClick = { userId, name ->
                publicProfileUser = UserProfile(
                  id = userId,
                  username = name,
                  fullName = name,
                  phone = "01XXXXXXXXX",
                  email = "$name@khelobd.com",
                  walletBalance = 300.0,
                  isOnline = true,
                  matchesPlayed = 42,
                  wins = 28,
                  totalEarnings = 3500.0,
                  freeFireUid = "54829${userId.takeLast(3)}",
                  pubgUid = "51482${userId.takeLast(4)}"
                )
              }
            )
          }

          Screen.LEADERBOARD -> {
            LeaderboardScreen(
              topPlayers = topPlayers,
              topTeams = teams,
              onPlayerClick = { userId, name ->
                publicProfileUser = UserProfile(
                  id = userId,
                  username = name,
                  fullName = name,
                  phone = "01XXXXXXXXX",
                  email = "$name@khelobd.com",
                  walletBalance = 300.0,
                  isOnline = true,
                  matchesPlayed = 42,
                  wins = 28,
                  totalEarnings = 3500.0,
                  freeFireUid = "54829${userId.takeLast(3)}",
                  pubgUid = "51482${userId.takeLast(4)}"
                )
              },
              onTeamClick = { currentScreen = Screen.TEAMS }
            )
          }

          Screen.ACCOUNT -> {
            ProfileScreen(
              currentUser = currentUser,
              transactions = transactions,
              isWithdrawAllowedTime = repository.isWithinWithdrawWindow(),
              onUpdateProfile = { name, phone, ffUid, pubgUid ->
                repository.updateProfile(name, phone, ffUid, pubgUid)
                scope.launch {
                  snackbarHostState.showSnackbar("প্রোফাইল তথ্য সফলভাবে সেভ হয়েছে।")
                }
              },
              onRequestDeposit = { method, amount, trxId ->
                val result = repository.requestDeposit(method, amount, trxId)
                scope.launch {
                  snackbarHostState.showSnackbar(result.getOrDefault("ডিপোজিট সফল!"))
                }
              },
              onRequestWithdraw = { method, number, amount ->
                val result = repository.requestWithdraw(method, number, amount)
                scope.launch {
                  snackbarHostState.showSnackbar(result.getOrDefault("উইথড্র আবেদন গ্রহণ করা হয়েছে।"))
                }
              },
              onNavigateToTournaments = { currentScreen = Screen.TOURNAMENTS },
              onNavigateToChallenges = { currentScreen = Screen.CHALLENGES },
              onNavigateToTeams = { currentScreen = Screen.TEAMS },
              onNavigateToNotifications = { showNotificationsDialog = true },
              onNavigateToDeposit = { currentScreen = Screen.ACCOUNT },
              onLogout = {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                currentScreen = Screen.HOME
              }
            )
          }
        }
      }
    }

  // Join Tournament Confirmation Dialog
  pendingTournamentToJoin?.let { tour ->
    BengaliConfirmDialog(
      title = "টুর্নামেন্টে জয়েন নিশ্চিতকরণ",
      message = "টুর্নামেন্ট: ${tour.title}\nমোড: ${tour.mode}\nএন্ট্রি ফি: ৳${tour.entryFee.toInt()} টাকা\n\nআপনার ওয়ালেট ব্যালেন্স থেকে ৳${tour.entryFee.toInt()} টাকা কেটে নেওয়া হবে। আপনি কি নিশ্চিত?",
      confirmButtonText = "হ্যাঁ, জয়েন করুন",
      cancelButtonText = "বাতিল",
      onConfirm = {
        pendingTournamentToJoin = null
        val result = repository.joinTournament(tour.id)
        scope.launch {
          snackbarHostState.showSnackbar(result.getOrDefault("টুর্নামেন্টে জয়েন সম্পন্ন!"))
        }
      },
      onDismiss = { pendingTournamentToJoin = null }
    )
  }

  // Public Profile Dialog (Available everywhere a player identity is clicked)
  publicProfileUser?.let { profileUser ->
    PublicProfileDialog(
      user = profileUser,
      onDismiss = { publicProfileUser = null },
      onChallengeClick = {
        publicProfileUser = null
        currentScreen = Screen.CHALLENGES
      }
    )
  }

  // Notifications Dialog
  if (showNotificationsDialog) {
    NotificationsDialog(
      notifications = notifications,
      onDismiss = { showNotificationsDialog = false },
      onNotificationClick = { notif ->
        repository.markNotificationRead(notif.id)
        showNotificationsDialog = false
        when (notif.targetType) {
          "CHALLENGE" -> currentScreen = Screen.CHALLENGES
          "TOURNAMENT" -> currentScreen = Screen.TOURNAMENTS
          "TEAM" -> currentScreen = Screen.TEAMS
          "WALLET" -> currentScreen = Screen.ACCOUNT
          else -> currentScreen = Screen.HOME
        }
      },
      onMarkAllRead = {
        repository.markAllNotificationsRead()
      }
    )
  }
  }
}
