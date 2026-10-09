package com.example

import com.example.data.model.ChallengeRule
import com.example.data.model.ChallengeStatus
import com.example.data.model.TournamentStatus
import com.example.data.repository.KheloBDRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class KheloBDTest {

  private lateinit var repository: KheloBDRepository

  @Before
  fun setUp() {
    repository = KheloBDRepository.getInstance()
  }

  @Test
  fun testJoinTournamentSucceedsAndDeductsFee() {
    val initialBalance = repository.currentUser.value.walletBalance
    val openTour = repository.tournaments.value.first { it.status == TournamentStatus.JOIN_OPEN }
    val initialSlots = openTour.joinedSlots

    val result = repository.joinTournament(openTour.id)
    assertTrue(result.isSuccess)

    val updatedTour = repository.tournaments.value.first { it.id == openTour.id }
    assertEquals(initialSlots + 1, updatedTour.joinedSlots)
    assertEquals(initialBalance - openTour.entryFee, repository.currentUser.value.walletBalance, 0.01)
  }

  @Test
  fun testCannotJoinFinishedTournament() {
    val finishedTour = repository.tournaments.value.first { it.status == TournamentStatus.FINISHED }
    val result = repository.joinTournament(finishedTour.id)
    assertTrue(result.isFailure)
  }

  @Test
  fun testChallengeCreationAndCancellationRefund() {
    val initialBalance = repository.currentUser.value.walletBalance
    val stakeAmount = 100.0

    // Create challenge
    val createResult = repository.createChallenge(
      game = "Free Fire",
      mode = "Lone Wolf",
      mapName = "Iron Cage",
      rule = ChallengeRule.HEADSHOT_ONLY,
      amount = stakeAmount,
      note = "Test Challenge"
    )
    assertTrue(createResult.isSuccess)
    assertEquals(initialBalance - stakeAmount, repository.currentUser.value.walletBalance, 0.01)

    // Locate created challenge
    val created = repository.challenges.value.first { it.challengerNote == "Test Challenge" }
    assertEquals(ChallengeStatus.OPEN, created.status)

    // Cancel challenge
    val cancelResult = repository.cancelChallenge(created.id)
    assertTrue(cancelResult.isSuccess)

    // Balance should be refunded
    assertEquals(initialBalance, repository.currentUser.value.walletBalance, 0.01)
  }

  @Test
  fun testChallengeAmountLimits() {
    // Under 50 BDT
    val underResult = repository.createChallenge(
      game = "Free Fire",
      mode = "Lone Wolf",
      mapName = "Iron Cage",
      rule = ChallengeRule.REGULAR,
      amount = 30.0,
      note = "Under min"
    )
    assertTrue(underResult.isFailure)

    // Over 200 BDT
    val overResult = repository.createChallenge(
      game = "Free Fire",
      mode = "Lone Wolf",
      mapName = "Iron Cage",
      rule = ChallengeRule.REGULAR,
      amount = 250.0,
      note = "Over max"
    )
    assertTrue(overResult.isFailure)
  }

  @Test
  fun testChallengeSettlementPayout80Percent() {
    // Create challenge
    repository.createChallenge(
      game = "Free Fire",
      mode = "Clash Squad",
      mapName = "Bermuda",
      rule = ChallengeRule.REGULAR,
      amount = 100.0,
      note = "Settlement Test"
    )
    val challenge = repository.challenges.value.first { it.challengerNote == "Settlement Test" }

    val initialEarnings = repository.currentUser.value.totalEarnings

    // Total pot = 100 + 100 = 200 BDT
    // 20% platform fee = 40 BDT
    // 80% winner payout = 160 BDT
    val settleResult = repository.settleChallenge(challenge.id, repository.currentUser.value.id)
    assertTrue(settleResult.isSuccess)

    assertEquals(initialEarnings + 160.0, repository.currentUser.value.totalEarnings, 0.01)
  }

  @Test
  fun testTeamDeposit() {
    val team = repository.teams.value.first()
    val initialTeamBalance = team.balance
    val userBalance = repository.currentUser.value.walletBalance

    val result = repository.teamDeposit(team.id, 100.0)
    assertTrue(result.isSuccess)

    val updatedTeam = repository.teams.value.first { it.id == team.id }
    assertEquals(initialTeamBalance + 100.0, updatedTeam.balance, 0.01)
    assertEquals(userBalance - 100.0, repository.currentUser.value.walletBalance, 0.01)
  }
}
