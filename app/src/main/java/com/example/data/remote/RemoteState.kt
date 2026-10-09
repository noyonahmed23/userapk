package com.example.data.remote

import com.example.data.model.*

data class RemoteAppState(
    val currentUser: UserProfile,
    val tournaments: List<Tournament>,
    val challenges: List<PlayerChallenge>,
    val teams: List<Team>,
    val teamChallenges: List<TeamChallenge>,
    val transactions: List<WalletTransaction>,
    val notifications: List<NotificationItem>,
    val banners: List<BannerSlide>,
    val gameCategories: List<GameCategory>,
    val topPlayers: List<LeaderboardItem>,
    val paymentSettings: PaymentMethodSettings,
    val depositRequests: List<DepositRequest>
)
